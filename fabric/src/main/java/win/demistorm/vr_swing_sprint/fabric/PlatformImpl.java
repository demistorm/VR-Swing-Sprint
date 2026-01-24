package win.demistorm.vr_swing_sprint.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
    public void sendToServer(RegistryFriendlyByteBuf packet) {
        // Wrap in BufferPacket and send
        packet.retain();
        try {
            ClientPlayNetworking.send(new BufferPacket(packet));
        } finally {
            packet.release();
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf packet) {
        // Wrap in BufferPacket and send
        packet.retain();
        try {
            ServerPlayNetworking.send(player, new BufferPacket(packet));
        } finally {
            packet.release();
        }
    }
}