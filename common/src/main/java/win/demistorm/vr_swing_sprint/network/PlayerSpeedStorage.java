package win.demistorm.vr_swing_sprint.network;

import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Simple storage for sprint speed multiplier per player
public class PlayerSpeedStorage {

    private static final Logger log = LogManager.getLogger(PlayerSpeedStorage.class);

    // Store speed multiplier per player UUID
    private static final java.util.Map<java.util.UUID, Float> playerSpeeds = new java.util.concurrent.ConcurrentHashMap<>();

    private PlayerSpeedStorage() {
        // Static utility class
    }

    // Get current multiplier for player (returns 0.0f if not set)
    public static float getMultiplier(ServerPlayer player) {
        return playerSpeeds.getOrDefault(player.getUUID(), 0.0f);
    }

    // Set multiplier for player
    public static void setMultiplier(ServerPlayer player, float multiplier) {
        playerSpeeds.put(player.getUUID(), multiplier);

        if (log.isDebugEnabled()) {
            log.debug("[VR Swing Sprint] Set speed multiplier for {} to {}",
                    player.getName().getString(), multiplier);
        }
    }
}
