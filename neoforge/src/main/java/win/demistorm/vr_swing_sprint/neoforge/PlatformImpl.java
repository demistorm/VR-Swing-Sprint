package win.demistorm.vr_swing_sprint.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import win.demistorm.vr_swing_sprint.Platform;
import win.demistorm.vr_swing_sprint.PlatformHolder;
import win.demistorm.vr_swing_sprint.network.Network;

// NeoForge-specific platform implementation
public final class PlatformImpl implements Platform {

    private PlatformImpl() {}

    private static final Platform INSTANCE = new PlatformImpl();

    public static void init() {
        PlatformHolder.setPlatform(INSTANCE);
    }

    @Override
    public void sendToServer(RegistryFriendlyByteBuf packet) {
        // Wrap in BufferPacket and send
        PacketDistributor.sendToServer(new BufferPacket(packet));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf packet) {
        // Wrap in BufferPacket and send
        PacketDistributor.sendToPlayer(player, new BufferPacket(packet));
    }

    // Handle incoming packets from client
    public static void handleClientPacket(RegistryFriendlyByteBuf buffer, ServerPlayer player) {
        // Forward to NetworkChannel
        Network.INSTANCE.handlePacket(player, buffer);
    }
}
