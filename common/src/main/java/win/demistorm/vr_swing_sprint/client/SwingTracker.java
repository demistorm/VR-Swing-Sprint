package win.demistorm.vr_swing_sprint.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

// Detects arm swing motions and triggers sprinting
public class SwingTracker implements Tracker {

    private static final Logger log = LogManager.getLogger(SwingTracker.class);

    // Tunables
    private static final double MIN_SWING_SPEED = 0.04;    // Min speed (blocks/tick) for arm movement
    private static final int LOOKBACK_TICKS = 7;           // How many ticks to look back for movement
    private static final double POSITION_THRESHOLD = 0.03; // Min Z (forward) movement to count as a stroke
    private static final double UPWARD_THRESHOLD = 0.03;   // Min Y (upward) movement to count as a stroke

    // Speed curve tunables (quadratic interpolation)
    private static final double WEAK_VEL_THRESHOLD = 0.05;   // Velocity below this = min boost
    private static final double STRONG_VEL_THRESHOLD = 0.25; // Velocity above this = max boost
    private static final float MIN_BOOST = 0.15f;            // Speed boost at light jog
    private static final float MAX_BOOST = 0.60f;            // Speed boost at full sprint

    // Speed smoothing tunables
    private static final int SPEED_SAMPLE_INTERVAL = 20;    // Ticks between speed updates
    private static final float SMOOTHING_FACTOR = 0.6f;     // Weight towards new speed (0.0-1.0, higher = more responsive)

    // Tracking state (not persistent across sessions, which is fine)
    private int mainHandCooldown = 0;                      // Cooldown for main hand strokes
    private int offHandCooldown = 0;                       // Cooldown for off hand strokes
    private int syncCooldown = 0;                          // Cooldown for network sync

    // Speed sampling state
    private static int sampleTickCounter = 0;              // Ticks since last speed update
    private static final List<Float> speedSamples = new ArrayList<>(); // Collected speed samples for averaging
    private static float lastSmoothedSpeed = 0.0f;         // Last sent smoothed speed
    private static boolean initialSpeedSent = false;       // Track if sent initial speed

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
        if (syncCooldown > 0) syncCooldown--;

        // Get historical poses for movement checking
        VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
        if (history == null) {
            return;
        }

        try {
            // Check main hand (if cooldown allows)
            if (mainHandCooldown >= 5) {
                Vec3 mainHandMovement = calculatePositiveMovement(history, VRBodyPart.MAIN_HAND);
                if (mainHandMovement != null
                    && mainHandMovement.z > POSITION_THRESHOLD
                    && mainHandMovement.y > UPWARD_THRESHOLD) {
                    double mainHandSpeed = history.averageSpeed(VRBodyPart.MAIN_HAND, LOOKBACK_TICKS, true);
                    if (mainHandSpeed >= MIN_SWING_SPEED) {
                        mainHandCooldown = 0;
                        SprintHelper.strokeDetected(player, true, mainHandSpeed);
                        log.debug("[VR Swing Sprint] Forward stroke detected from main hand");
                    }
                }
            }

            // Check off hand (if cooldown allows)
            if (offHandCooldown >= 5) {
                Vec3 offHandMovement = calculatePositiveMovement(history, VRBodyPart.OFF_HAND);
                if (offHandMovement != null
                    && offHandMovement.z > POSITION_THRESHOLD
                    && offHandMovement.y > UPWARD_THRESHOLD) {
                    double offHandSpeed = history.averageSpeed(VRBodyPart.OFF_HAND, LOOKBACK_TICKS, true);
                    if (offHandSpeed >= MIN_SWING_SPEED) {
                        offHandCooldown = 0;
                        SprintHelper.strokeDetected(player, false, offHandSpeed);
                        log.debug("[VR Swing Sprint] Forward stroke detected from off hand");
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Not enough pose history yet (usually when first loading into world)
        }

        // Calculate and send speed multiplier if server supports it
        updateSpeedMultiplier(player);
    }

    // Calculate positive-only forward and upward movement (ignores return/backward motion)
    private Vec3 calculatePositiveMovement(VRPoseHistory history, VRBodyPart bodyPart) {
        double totalForward = 0.0;
        double totalUpward = 0.0;

        try {
            // Iterate through the pose history (using player-relative positions)
            for (int i = 0; i < SwingTracker.LOOKBACK_TICKS; i++) {
                VRPose currentPose = history.getHistoricalData(i, true);
                VRPose prevPose = history.getHistoricalData(i + 1, true);

                if (currentPose == null || prevPose == null) {
                    return null;
                }

                Vec3 currentPos = getPositionFromBodyPart(currentPose, bodyPart);
                Vec3 prevPos = getPositionFromBodyPart(prevPose, bodyPart);

                if (currentPos == null || prevPos == null) {
                    return null;
                }

                // Calculate movement this tick
                double dz = currentPos.z - prevPos.z;  // Forward movement
                double dy = currentPos.y - prevPos.y;  // Upward movement

                // Only add positive movements
                if (dz > 0) totalForward += dz;
                if (dy > 0) totalUpward += dy;
            }
        } catch (Exception e) {
            // Not enough pose history or other error
            return null;
        }

        return new Vec3(0, totalUpward, totalForward);
    }

    // Get position from body part (handles both hands)
    private Vec3 getPositionFromBodyPart(VRPose pose, VRBodyPart bodyPart) {
        if (bodyPart == VRBodyPart.MAIN_HAND) {
            var hand = pose.getHand(net.minecraft.world.InteractionHand.MAIN_HAND);
            return hand != null ? hand.getPos() : null;
        } else if (bodyPart == VRBodyPart.OFF_HAND) {
            var hand = pose.getHand(net.minecraft.world.InteractionHand.OFF_HAND);
            return hand != null ? hand.getPos() : null;
        }
        return null;
    }

    // Calculate and send speed multiplier to server
    private void updateSpeedMultiplier(LocalPlayer player) {
        // Calculate if actively sprinting and initial speed was sent
        if (!SprintHelper.isSprintingActive() || !initialSpeedSent) {
            return;
        }

        // Only send if server has the mod
        if (!SprintHelper.hasServerCapability()) {
            return; // Vanilla server (don't send custom speed packets)
        }

        // Get combined arm speed
        VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
        if (history == null) return;

        try {
            double mainHandSpeed = 0.0;
            double offHandSpeed = 0.0;

            // Get main hand speed
            Vec3 mainHandMovement = calculatePositiveMovement(history, VRBodyPart.MAIN_HAND);
            if (mainHandMovement != null
                && mainHandMovement.z > POSITION_THRESHOLD
                && mainHandMovement.y > UPWARD_THRESHOLD) {
                mainHandSpeed = history.averageSpeed(VRBodyPart.MAIN_HAND, LOOKBACK_TICKS, true);
            }

            // Get off hand speed
            Vec3 offHandMovement = calculatePositiveMovement(history, VRBodyPart.OFF_HAND);
            if (offHandMovement != null
                && offHandMovement.z > POSITION_THRESHOLD
                && offHandMovement.y > UPWARD_THRESHOLD) {
                offHandSpeed = history.averageSpeed(VRBodyPart.OFF_HAND, LOOKBACK_TICKS, true);
            }

            // Use the faster of the two hands for speed calculation
            double combinedSpeed = Math.max(mainHandSpeed, offHandSpeed);

            // Calculate speed multiplier from combined arm speed
            float wouldBeMultiplier = calculateSpeedMultiplier(combinedSpeed);

            // Add to samples (only if there is some movement)
            if (combinedSpeed > 0) {
                speedSamples.add(wouldBeMultiplier);
            }

            // Increment sample counter
            sampleTickCounter++;

            // Check if it's time to process samples
            if (sampleTickCounter >= SPEED_SAMPLE_INTERVAL) {
                processSpeedSamples();
            }
        } catch (IndexOutOfBoundsException e) {
            // Not enough pose history yet
        }
    }

    // Calculate speed multiplier from arm swing velocity
    private static float calculateSpeedMultiplier(double velocity) {
        // Below weak threshold = always min boost
        if (velocity <= WEAK_VEL_THRESHOLD) {
            return MIN_BOOST;
        }

        // Above strong threshold = always max boost
        if (velocity >= STRONG_VEL_THRESHOLD) {
            return MAX_BOOST;
        }

        // Interpolate between min and max with quadratic curve
        double t = (velocity - WEAK_VEL_THRESHOLD) / (STRONG_VEL_THRESHOLD - WEAK_VEL_THRESHOLD);
        t = t * t; // Quadratic easing for natural feel

        return (float) (MIN_BOOST + t * (MAX_BOOST - MIN_BOOST));
    }

    // Send initial speed when sprint starts
    public static void sendInitialSpeed(double velocity) {
        // Calculate initial speed multiplier from velocity
        float initialMultiplier = calculateSpeedMultiplier(velocity);

        // Send immediately to server
        ClientNetworkHelper.sendSprintSpeed(initialMultiplier);

        // Store as last smoothed speed for future updates
        lastSmoothedSpeed = initialMultiplier;
        initialSpeedSent = true;

        // Update debug
        SprintHelper.setCurrentSpeedInfo(initialMultiplier, velocity);

        log.debug("[VR Swing Sprint] Initial speed sent: {} (velocity: {})",
                String.format("%.2f", initialMultiplier), String.format("%.3f", velocity));
    }

    // Process speed samples and send smoothed speed to server
    private static void processSpeedSamples() {
        // Skip if no samples collected
        if (speedSamples.isEmpty()) {
            sampleTickCounter = 0;
            return;
        }

        // Calculate average of all samples
        float sum = 0.0f;
        for (Float sample : speedSamples) {
            sum += sample;
        }
        float averageSpeed = sum / speedSamples.size();

        // Apply smoothing between last sent speed and new average
        float smoothedSpeed = lastSmoothedSpeed * (1.0f - SMOOTHING_FACTOR) + averageSpeed * SMOOTHING_FACTOR;

        // Send to server
        ClientNetworkHelper.sendSprintSpeed(smoothedSpeed);

        // Update last smoothed speed
        lastSmoothedSpeed = smoothedSpeed;

        // Update debug display
        double estimatedVelocity = (smoothedSpeed - MIN_BOOST) / (MAX_BOOST - MIN_BOOST)
                                   * (STRONG_VEL_THRESHOLD - WEAK_VEL_THRESHOLD) + WEAK_VEL_THRESHOLD;
        SprintHelper.setCurrentSpeedInfo(smoothedSpeed, estimatedVelocity);

        log.debug("[VR Swing Sprint] Smoothed speed update: {} (avg: {}, samples: {})",
                String.format("%.2f", smoothedSpeed), String.format("%.2f", averageSpeed), speedSamples.size());

        // Clear samples and reset counter
        speedSamples.clear();
        sampleTickCounter = 0;
    }

    // Clear all speed sampling state (call when sprint deactivates)
    public static void clearSpeedSamples() {
        speedSamples.clear();
        sampleTickCounter = 0;
        lastSmoothedSpeed = 0.0f;
        initialSpeedSent = false;

        log.debug("[VR Swing Sprint] Speed sampling state cleared");
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        // Reset state when tracker becomes inactive
        mainHandCooldown = 0;
        offHandCooldown = 0;
        SprintHelper.reset();
        clearSpeedSamples();
    }
}
