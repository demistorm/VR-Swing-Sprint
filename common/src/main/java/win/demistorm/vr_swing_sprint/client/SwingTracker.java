package win.demistorm.vr_swing_sprint.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

// Detects arm swing motions and triggers sprinting
public class SwingTracker implements Tracker {

    private static final Logger log = LogManager.getLogger(SwingTracker.class);

    // Tunables
    private static final double MIN_SWING_SPEED = 0.08;           // Min speed (blocks/tick) for arm movement
    private static final int LOOKBACK_TICKS = 4;                  // How many ticks to look back for movement
    private static final double MIN_HAND_DISTANCE = 0.3;          // Min distance between hands (blocks)
    private static final double POSITION_THRESHOLD = 0.05;        // Min Z movement to count as forward/back

    // Tracking state (not persistent across sessions, which is fine)
    private VRBodyPart lastDominantHand = null;                  // Which hand was dominant in last stroke

    @Override
    public ProcessType processType() {
        return ProcessType.PER_TICK;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        return player != null && VRAPI.instance().isVRPlayer(player);
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        if (player == null || !VRAPI.instance().isVRPlayer(player)) {
            return;
        }

        // Update sprint timeout state
        SprintHelper.tick(player);

        // Get historical pose data for movement analysis
        VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
        if (history == null) {
            return;
        }

        // Check speed of both hands (player-relative so player movement doesn't affect this)
        double mainHandSpeed = history.averageSpeed(VRBodyPart.MAIN_HAND, LOOKBACK_TICKS, true);
        double offHandSpeed = history.averageSpeed(VRBodyPart.OFF_HAND, LOOKBACK_TICKS, true);

        // Both hands must be moving fast enough
        if (mainHandSpeed < MIN_SWING_SPEED || offHandSpeed < MIN_SWING_SPEED) {
            return;
        }

        // Get net movement of both hands (player-relative)
        Vec3 mainHandMovement = history.netMovement(VRBodyPart.MAIN_HAND, LOOKBACK_TICKS, true);
        Vec3 offHandMovement = history.netMovement(VRBodyPart.OFF_HAND, LOOKBACK_TICKS, true);

        if (mainHandMovement == null || offHandMovement == null) {
            return;
        }

        // Check if hands are moving in opposite directions along Z axis (forward/back)
        // In local space, positive Z is forward, negative Z is back
        boolean mainHandForward = mainHandMovement.z > POSITION_THRESHOLD;
        boolean offHandForward = offHandMovement.z > POSITION_THRESHOLD;
        boolean mainHandBack = mainHandMovement.z < -POSITION_THRESHOLD;
        boolean offHandBack = offHandMovement.z < -POSITION_THRESHOLD;

        // One hand must be moving forward, the other back
        boolean oppositeDirection = (mainHandForward && offHandBack) || (mainHandBack && offHandForward);

        if (!oppositeDirection) {
            return;
        }

        // Get current hand positions to check distance between them
        VRPose currentPose = VRClientAPI.instance().getPreTickWorldPose();
        if (currentPose == null) {
            return;
        }

        Vec3 mainHandPos = getHandPosition(currentPose, VRBodyPart.MAIN_HAND);
        Vec3 offHandPos = getHandPosition(currentPose, VRBodyPart.OFF_HAND);

        if (mainHandPos == null || offHandPos == null) {
            return;
        }

        // Check that hands are far enough apart (not just tiny wiggles)
        double handDistance = mainHandPos.distanceTo(offHandPos);
        if (handDistance < MIN_HAND_DISTANCE) {
            return;
        }

        // Determine which hand is dominant (the one that moved more forward)
        VRBodyPart dominantHand;
        if (mainHandMovement.z > offHandMovement.z) {
            dominantHand = VRBodyPart.MAIN_HAND;
        } else {
            dominantHand = VRBodyPart.OFF_HAND;
        }

        // Check if this stroke alternates from the previous one
        if (lastDominantHand != null && dominantHand == lastDominantHand) {
            // Same hand dominant twice in a row, not a valid running pattern
            // Don't update lastDominantHand, just wait for alternating pattern
            return;
        }

        // Valid stroke detected with alternating pattern
        lastDominantHand = dominantHand;
        SprintHelper.strokeDetected(player);
        log.debug("[VR Swing Sprint] Valid stroke detected, dominant hand: {}", dominantHand);
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        // Reset state when tracker becomes inactive
        lastDominantHand = null;
        SprintHelper.reset();
    }

    // Helper to get hand position from VRPose
    private Vec3 getHandPosition(VRPose pose, VRBodyPart bodyPart) {
        try {
            // Map VRBodyPart to InteractionHand
            InteractionHand hand;
            if (bodyPart == VRBodyPart.MAIN_HAND) {
                hand = InteractionHand.MAIN_HAND;
            } else if (bodyPart == VRBodyPart.OFF_HAND) {
                hand = InteractionHand.OFF_HAND;
            } else {
                return null;
            }

            // Get hand data and extract position
            VRBodyPartData handData = pose.getHand(hand);
            return handData != null ? handData.getPos() : null;
        } catch (Exception e) {
            log.warn("[VR Swing Sprint] Error getting hand position: {}", e.getMessage());
        }
        return null;
    }
}
