package win.demistorm.vr_swing_sprint.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.client.VRClientAPI;

// Manages sprint state and activation logic
public class SprintHelper {

    private static final Logger log = LogManager.getLogger(SprintHelper.class);

    // Tunables
    private static final int STROKES_TO_ACTIVATE = 2;       // Number of alternating swings needed to activate sprint
    private static final int TIMEOUT_TICKS = 20;            // How long sprint lasts after last stroke (1 second)
    private static final float HAPTIC_STRENGTH = 0.3f;      // Haptic feedback intensity when sprint activates

    // State
    private static boolean isSprinting = false;             // Currently sprinting
    private static int strokeCount = 0;                     // Number of valid strokes detected
    private static int timeoutCounter = 0;                  // Ticks until sprint deactivates

    // Prevent instantiation
    private SprintHelper() {}

    // Called when a valid arm swing stroke is detected
    public static void strokeDetected(LocalPlayer player) {
        strokeCount++;

        // Check if we've reached the stroke requirement to activate sprint
        if (strokeCount >= STROKES_TO_ACTIVATE && !isSprinting) {
            activateSprint(player);
        }

        // Always reset timeout when a stroke is detected (keeps sprint going or starts it)
        timeoutCounter = TIMEOUT_TICKS;
    }

    // Called every tick to update timeout and state
    public static void tick(LocalPlayer player) {
        if (isSprinting) {
            timeoutCounter--;

            // Check if sprint should timeout
            if (timeoutCounter <= 0) {
                deactivateSprint(player);
            }
        }
    }

    // Activate sprinting state
    private static void activateSprint(LocalPlayer player) {
        isSprinting = true;
        log.info("[VR Swing Sprint] Would-be sprint activated");

        // Send message to player
        if (player != null) {
            player.displayClientMessage(Component.literal("Would-be sprint activated"), true);
        }

        // Haptic feedback for satisfying feel
        VRClientAPI.instance().triggerHapticPulse(VRBodyPart.MAIN_HAND, HAPTIC_STRENGTH);
        VRClientAPI.instance().triggerHapticPulse(VRBodyPart.OFF_HAND, HAPTIC_STRENGTH);
    }

    // Deactivate sprinting state
    private static void deactivateSprint(LocalPlayer player) {
        isSprinting = false;
        strokeCount = 0;
        log.info("[VR Swing Sprint] Would-be sprint deactivated");

        // Send message to player
        if (player != null) {
            player.displayClientMessage(Component.literal("Would-be sprint deactivated"), true);
        }
    }

    // Reset all state (called when detection clearly fails or player stops VR)
    public static void reset() {
        isSprinting = false;
        strokeCount = 0;
        timeoutCounter = 0;
    }

    // Check if currently sprinting
    public static boolean isSprinting() {
        return isSprinting;
    }
}
