package win.demistorm.vr_swing_sprint.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import win.demistorm.vr_swing_sprint.Platform;
import win.demistorm.vr_swing_sprint.PlatformHolder;

// Fabric-specific platform implementation
public final class PlatformImpl implements Platform {

    private PlatformImpl() {}

    private static final Platform INSTANCE = new PlatformImpl();

    public static void init() {
        PlatformHolder.setPlatform(INSTANCE);
    }

    @Override
    public void sendToServer(FriendlyByteBuf packet) {
        // Wrap in BufferPacket and send
        ClientPlayNetworking.send(BufferPacket.ID, packet);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, FriendlyByteBuf packet) {
        // Wrap in BufferPacket and send
        ServerPlayNetworking.send(player, BufferPacket.ID, packet);
    }
}