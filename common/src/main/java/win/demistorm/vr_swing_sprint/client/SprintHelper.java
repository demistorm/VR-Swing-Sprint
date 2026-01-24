package win.demistorm.vr_swing_sprint.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import win.demistorm.vr_swing_sprint.VRSwingSprint;

// Manages sprint state and activation
public class SprintHelper {

    private static final Logger log = LogManager.getLogger(SprintHelper.class);

    // Tunables
    private static final int TIMEOUT_TICKS = 25;            // How long sprint lasts after last stroke (1.25 seconds)
    private static final int STROKE_TIMEOUT_TICKS = 60;     // How long before stroke tracking resets (3 seconds)
    private static final int HANDSHAKE_TIMEOUT_TICKS = 100; // Server handshake timeout (5 seconds)

    // Server detection
    private static boolean serverHasMod = false;            // Server has VR Swing Sprint installed
    private static int handshakeTimeoutCounter = -1;        // -1 = not started, 0+ = waiting for response

    // State
    private static boolean isSprinting = false;             // Currently sprinting
    private static boolean mainHandStroked = false;         // Main hand has contributed a stroke
    private static boolean offHandStroked = false;          // Off hand has contributed a stroke
    private static int timeoutCounter = 0;                  // Ticks until sprint deactivates
    private static int strokeTimeoutCounter = 0;            // Ticks until stroke count resets
    private static float currentSpeedMultiplier = 0.0f;     // Current speed multiplier (for debug display)
    private static double currentArmVelocity = 0.0;         // Current arm swing velocity (for debug display)
    private static double maxStrokeVelocity = 0.0;          // Max velocity from activating strokes

    // Prevent instantiation
    private SprintHelper() {}

    // Server management
    public static boolean hasServerCapability() {
        return serverHasMod;
    }

    // Check if currently sprinting (used by SwingTracker)
    public static boolean isSprintingActive() {
        return isSprinting;
    }

    // Update current speed multiplier and arm velocity (called by SwingTracker when sending to server)
    public static void setCurrentSpeedInfo(float multiplier, double velocity) {
        currentSpeedMultiplier = multiplier;
        currentArmVelocity = velocity;

        // Show speed update message if sprinting and debug mode is on
        if (isSprinting && serverHasMod && VRSwingSprint.debugMode) {
            LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                String speedMsg = String.format("Speed updated: velocity %.2f → +%d%% speed",
                    velocity, (int)(multiplier * 100));
                player.displayClientMessage(Component.literal(speedMsg), false);
            }
        }
    }

    // Start the server handshake (call when player joins world)
    public static void startHandshake() {
        serverHasMod = false;
        handshakeTimeoutCounter = HANDSHAKE_TIMEOUT_TICKS;
        log.debug("[VR Swing Sprint] Starting server capability handshake");
    }

    // Called when server responds
    public static void onServerCapabilityConfirmed() {
        serverHasMod = true;
        handshakeTimeoutCounter = -1;
        log.info("[VR Swing Sprint] Server has VR Swing Sprint - custom speed multiplier enabled!");
    }

    // Check if handshake timed out (called every tick)
    private static void checkHandshakeTimeout() {
        if (handshakeTimeoutCounter > 0) {
            handshakeTimeoutCounter--;
            if (handshakeTimeoutCounter <= 0) {
                // Timeout (server doesn't have the mod)
                serverHasMod = false;
                handshakeTimeoutCounter = -1;
                log.info("[VR Swing Sprint] Server capability handshake timed out - using vanilla sprint behavior");
            }
        }
    }

    // Called when a valid arm swing stroke is detected
    public static void strokeDetected(LocalPlayer player, boolean isMainHand, double velocity) {
        // Mark which hand stroked
        if (isMainHand) {
            mainHandStroked = true;
        } else {
            offHandStroked = true;
        }

        // Track max velocity from activating strokes
        if (!isSprinting && velocity > maxStrokeVelocity) {
            maxStrokeVelocity = velocity;
        }

        strokeTimeoutCounter = STROKE_TIMEOUT_TICKS;  // Reset stroke timeout

        if (!isSprinting) {
            // Need both hands to stroke before activating sprint
            if (mainHandStroked && offHandStroked) {
                activateSprint(player, maxStrokeVelocity);
                timeoutCounter = TIMEOUT_TICKS;  // Set initial timeout
                // Reset hand tracking for maintaining sprint
                mainHandStroked = false;
                offHandStroked = false;
                maxStrokeVelocity = 0.0;  // Reset velocity tracking
            }
        } else {
            // Already sprinting, need both hands to stroke to reset timeout
            if (mainHandStroked && offHandStroked) {
                timeoutCounter = TIMEOUT_TICKS;  // Reset sprint timeout
                // Reset hand tracking for next cycle
                mainHandStroked = false;
                offHandStroked = false;
                log.debug("[VR Swing Sprint] Sprint timeout reset by full arm cycle");
            }
        }
    }

    // Called every tick to update timeout and state
    public static void tick(LocalPlayer player) {
        // Check handshake timeout
        checkHandshakeTimeout();

        if (isSprinting) {
            timeoutCounter--;

            // Check if sprint should timeout
            if (timeoutCounter <= 0) {
                deactivateSprint(player);
            }
        } else if (mainHandStroked || offHandStroked) {
            // Not sprinting but have strokes counted, check stroke timeout
            strokeTimeoutCounter--;

            // Reset stroke tracking if too much time has passed
            if (strokeTimeoutCounter <= 0) {
                mainHandStroked = false;
                offHandStroked = false;
            }
        }
    }

    // Activate sprinting state
    private static void activateSprint(LocalPlayer player, double initialVelocity) {
        isSprinting = true;
        log.debug("[VR Swing Sprint] Sprint activated");

        // Send initial speed to server if it supports custom multipliers
        if (serverHasMod) {
            SwingTracker.sendInitialSpeed(initialVelocity);
        }

        // Enable sprinting
        if (player != null) {
            player.setSprinting(true);
            if (VRSwingSprint.debugMode) {
                String speedMsg;
                if (serverHasMod) {
                    speedMsg = String.format(" (velocity: %.2f → +%d%% speed)",
                        initialVelocity, (int)(currentSpeedMultiplier * 100));
                } else {
                    speedMsg = " (vanilla speed)";
                }
                player.displayClientMessage(Component.literal("Sprint activated" + speedMsg), false);
            }
        }
    }

    // Deactivate sprinting state
    private static void deactivateSprint(LocalPlayer player) {
        isSprinting = false;
        log.debug("[VR Swing Sprint] Sprint deactivated");

        // Clear speed sampling state
        SwingTracker.clearSpeedSamples();

        // Disable sprinting
        if (player != null) {
            player.setSprinting(false);
            if (VRSwingSprint.debugMode) {
                String speedMsg;
                if (serverHasMod && currentSpeedMultiplier > 0) {
                    speedMsg = String.format(" (was velocity: %.2f → +%d%% speed)",
                        currentArmVelocity, (int)(currentSpeedMultiplier * 100));
                } else {
                    speedMsg = "";
                }
                player.displayClientMessage(Component.literal("Sprint deactivated" + speedMsg), false);
            }
        }
    }

    // Reset all states (called when detection fails or player stops VR)
    public static void reset() {
        isSprinting = false;
        mainHandStroked = false;
        offHandStroked = false;
        timeoutCounter = 0;
        strokeTimeoutCounter = 0;
        maxStrokeVelocity = 0.0;
    }
}
