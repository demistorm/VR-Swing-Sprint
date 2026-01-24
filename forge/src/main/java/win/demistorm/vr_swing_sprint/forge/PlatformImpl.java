package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import win.demistorm.vr_swing_sprint.Platform;
import win.demistorm.vr_swing_sprint.PlatformHolder;
import win.demistorm.vr_swing_sprint.network.Network;

// Forge-specific platform implementation
public final class PlatformImpl implements Platform {

    private PlatformImpl() {}

    private static final Platform INSTANCE = new PlatformImpl();

    // Keep reference to network channel
    static net.minecraftforge.network.EventNetworkChannel NETWORK;

    public static void init(net.minecraftforge.network.EventNetworkChannel channel) {
        NETWORK = channel;
        PlatformHolder.setPlatform(INSTANCE);
    }

    @Override
    public void sendToServer(RegistryFriendlyByteBuf packet) {
        // Convert to FriendlyByteBuf and send
        FriendlyByteBuf forgeBuf = new FriendlyByteBuf(packet);
        NETWORK.send(forgeBuf, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf packet) {
        // Convert to FriendlyByteBuf and send
        FriendlyByteBuf forgeBuf = new FriendlyByteBuf(packet);
        NETWORK.send(forgeBuf, PacketDistributor.PLAYER.with(player));
    }

    // Handle incoming packets from client
    public static void handleClientPacket(FriendlyByteBuf payload, ServerPlayer sender) {
        // Add registry access and forward to NetworkChannel
        RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(payload, sender.level().registryAccess());
        Network.INSTANCE.handlePacket(sender, registryBuf);
    }

    // Handle incoming packets from server (called from clientside)
    public static void handleServerPacket(FriendlyByteBuf payload) {
        // Clientside handler in ForgeClient
    }
}
