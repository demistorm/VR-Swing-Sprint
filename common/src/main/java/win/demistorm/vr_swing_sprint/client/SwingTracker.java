package win.demistorm.vr_swing_sprint.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPoseHistory;

// Detects arm swing motions and triggers sprinting
public class SwingTracker implements Tracker {

    private static final Logger log = LogManager.getLogger(SwingTracker.class);

    // Tunables
    private static final double MIN_SWING_SPEED = 0.06;           // Min speed (blocks/tick) for arm movement
    private static final int LOOKBACK_TICKS = 5;                  // How many ticks to look back for movement
    // Min distance between hands (blocks)
    private static final double POSITION_THRESHOLD = 0.05;        // Min Z movement to count as forward/back

    // Tracking state (not persistent across sessions, which is fine)
    private int mainHandCooldown = 0;                            // Cooldown for main hand strokes
    private int offHandCooldown = 0;                             // Cooldown for off hand strokes

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

        // Increment cooldowns
        mainHandCooldown++;
        offHandCooldown++;

        // Get historical pose data for movement analysis
        VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
        if (history == null) {
            return;
        }

        try {
            // Check main hand (if cooldown allows)
            if (mainHandCooldown >= 8) {
                Vec3 mainHandMovement = history.netMovement(VRBodyPart.MAIN_HAND, LOOKBACK_TICKS, true);
                if (mainHandMovement != null && mainHandMovement.z > POSITION_THRESHOLD) {
                    double mainHandSpeed = history.averageSpeed(VRBodyPart.MAIN_HAND, LOOKBACK_TICKS, true);
                    if (mainHandSpeed >= MIN_SWING_SPEED) {
                        mainHandCooldown = 0;
                        SprintHelper.strokeDetected(player, true);
                        log.debug("[VR Swing Sprint] Forward stroke detected from main hand");
                    }
                }
            }

            // Check off hand (if cooldown allows)
            if (offHandCooldown >= 8) {
                Vec3 offHandMovement = history.netMovement(VRBodyPart.OFF_HAND, LOOKBACK_TICKS, true);
                if (offHandMovement != null && offHandMovement.z > POSITION_THRESHOLD) {
                    double offHandSpeed = history.averageSpeed(VRBodyPart.OFF_HAND, LOOKBACK_TICKS, true);
                    if (offHandSpeed >= MIN_SWING_SPEED) {
                        offHandCooldown = 0;
                        SprintHelper.strokeDetected(player, false);
                        log.debug("[VR Swing Sprint] Forward stroke detected from off hand");
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Not enough pose history yet (common when first loading into world)
        }
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        // Reset state when tracker becomes inactive
        mainHandCooldown = 0;
        offHandCooldown = 0;
        SprintHelper.reset();
    }
}
