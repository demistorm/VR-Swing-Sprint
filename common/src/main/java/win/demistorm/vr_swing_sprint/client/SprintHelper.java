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
    // Haptic feedback intensity when sprint activates

    // State
    private static boolean isSprinting = false;             // Currently sprinting
    private static boolean mainHandStroked = false;         // Main hand has contributed a stroke
    private static boolean offHandStroked = false;          // Off hand has contributed a stroke
    private static int timeoutCounter = 0;                  // Ticks until sprint deactivates
    private static int strokeTimeoutCounter = 0;            // Ticks until stroke count resets

    // Prevent instantiation
    private SprintHelper() {}

    // Called when a valid arm swing stroke is detected
    public static void strokeDetected(LocalPlayer player, boolean isMainHand) {
        // Mark which hand stroked
        if (isMainHand) {
            mainHandStroked = true;
        } else {
            offHandStroked = true;
        }

        strokeTimeoutCounter = STROKE_TIMEOUT_TICKS;  // Reset stroke timeout

        if (!isSprinting) {
            // Need both hands to stroke before activating sprint
            if (mainHandStroked && offHandStroked) {
                activateSprint(player);
                timeoutCounter = TIMEOUT_TICKS;  // Set initial timeout
                // Reset hand tracking for maintaining sprint
                mainHandStroked = false;
                offHandStroked = false;
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
    private static void activateSprint(LocalPlayer player) {
        isSprinting = true;
        log.debug("[VR Swing Sprint] Sprint activated");

        // Enable sprinting
        if (player != null) {
            player.setSprinting(true);
            if (VRSwingSprint.debugMode) {
                player.displayClientMessage(Component.literal("Sprint activated"), true);
            }
        }
    }

    // Deactivate sprinting state
    private static void deactivateSprint(LocalPlayer player) {
        isSprinting = false;
        log.debug("[VR Swing Sprint] Sprint deactivated");

        // Disable sprinting
        if (player != null) {
            player.setSprinting(false);
            if (VRSwingSprint.debugMode) {
                player.displayClientMessage(Component.literal("Sprint deactivated"), true);
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
    }
}
