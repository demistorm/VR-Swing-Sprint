package win.demistorm.vr_swing_sprint.network;

import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Simple attachment for storing sprint speed multiplier per player
// Note: This is a basic implementation. For Forge/NeoForge, you'd want to use
// the proper AttachCapabilitiesEvent system, but this works for our purposes.
public class PlayerSpeedAttachment {

    private static final Logger log = LogManager.getLogger(PlayerSpeedAttachment.class);

    // Speed multiplier storage (using a simple map for now)
    // In production, you'd use Forge's capability system
    private static final java.util.Map<java.util.UUID, Float> playerSpeeds = new java.util.concurrent.ConcurrentHashMap<>();

    private final ServerPlayer player;
    private float multiplier = 0.0f;

    private PlayerSpeedAttachment(ServerPlayer player) {
        this.player = player;
    }

    // Get or create attachment for player
    public static PlayerSpeedAttachment get(ServerPlayer player) {
        // For now, create a new instance each time (the multiplier is stored statically)
        // In a full implementation, this would use Forge's capability system
        return new PlayerSpeedAttachment(player);
    }

    // Get current multiplier
    public float getMultiplier() {
        // Try to load from static storage
        Float stored = playerSpeeds.get(player.getUUID());
        return stored != null ? stored : 0.0f;
    }

    // Set multiplier
    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
        playerSpeeds.put(player.getUUID(), multiplier);

        if (log.isDebugEnabled()) {
            log.debug("[VR Swing Sprint] Set speed multiplier for {} to {}",
                    player.getName().getString(), multiplier);
        }
    }

    // Clean up when player disconnects
    public static void onPlayerDisconnect(ServerPlayer player) {
        playerSpeeds.remove(player.getUUID());
        log.debug("[VR Swing Sprint] Cleaned up speed data for {}", player.getName().getString());
    }
}
