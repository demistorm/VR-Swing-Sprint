package win.demistorm.vr_swing_sprint.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.api.VRAPI;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.client.SprintHelper;
import win.demistorm.vr_swing_sprint.network.data.ServerCapabilityResponse;
import win.demistorm.vr_swing_sprint.network.data.SprintSpeedData;

// Processes incoming network packets
public final class NetworkHandlers {

    private static final Logger log = LogManager.getLogger(NetworkHandlers.class);

    private NetworkHandlers() {}

    // Client asked if server has mod installed
    public static void handleCapabilityQuery(Player player) {
        if (player == null || !player.isAlive()) return;

        log.debug("[VR Swing Sprint] Received query from {}", player.getName().getString());

        // Server confirms it has the mod
        if (player instanceof ServerPlayer serverPlayer) {
            Network.INSTANCE.sendToPlayer(serverPlayer, new ServerCapabilityResponse(true));

            if (VRSwingSprint.debugMode) {
                player.displayClientMessage(Component.literal("VR Swing Sprint: Serverside enabled!"), true);
            }
        }
    }

    // Server confirmed it has the mod (clientside only)
    public static void handleCapabilityResponse(Player player, ServerCapabilityResponse data) {
        if (player == null) return;

        if (data.supportsCustomSpeeds()) {
            log.info("[VR Swing Sprint] Server confirmed VR Swing Sprint is installed");
            SprintHelper.onServerCapabilityConfirmed();

            if (VRSwingSprint.debugMode) {
                player.displayClientMessage(Component.literal("VR Swing Sprint: Custom speed multiplier enabled!"), true);
            }
        }
    }

    // Client sent speed multiplier to server
    public static void handleSprintSpeed(Player player, SprintSpeedData data) {
        if (player == null || !player.isAlive()) return;

        // Validate this is a VR player
        try {
            if (!VRAPI.instance().isVRPlayer(player)) {
                log.warn("[VR Swing Sprint] Non-VR player {} tried to send sprint speed packet!",
                        player.getName().getString());
                return; // Reject the packet
            }
        } catch (Exception e) {
            log.warn("[VR Swing Sprint] Failed to check VR status for player {}: {}",
                    player.getName().getString(), e.getMessage());
            return;
        }

        // Store the speed multiplier for player
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerSpeedStorage.setMultiplier(serverPlayer, data.speedMultiplier());

            if (VRSwingSprint.debugMode) {
                log.debug("[VR Swing Sprint] Updated speed multiplier for {} to {}",
                        player.getName().getString(), data.speedMultiplier());
            }
        }
    }
}
