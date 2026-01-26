package win.demistorm.vr_swing_sprint;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

// Holder for platform implementation (set by each loader)
public final class PlatformHolder {

    private static Platform platform;

    private PlatformHolder() {}

    public static void setPlatform(Platform platform) {
        PlatformHolder.platform = platform;
    }

    public static void sendToServer(FriendlyByteBuf packet) {
        if (platform == null) {
            throw new IllegalStateException("Platform not initialized! Call setPlatform() first.");
        }
        platform.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, FriendlyByteBuf packet) {
        if (platform == null) {
            throw new IllegalStateException("Platform not initialized! Call setPlatform() first.");
        }
        platform.sendToPlayer(player, packet);
    }
}
