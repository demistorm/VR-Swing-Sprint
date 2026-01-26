package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import win.demistorm.vr_swing_sprint.network.Network;

import java.util.function.Supplier;

// Simple packet wrapper for Forge networking
public class BufferPacket implements Packet<PacketListener> {
    private final FriendlyByteBuf buffer;

    public BufferPacket(FriendlyByteBuf buffer) {
        this.buffer = buffer;
    }

    public static void encode(BufferPacket packet, FriendlyByteBuf buf) {
        buf.writeBytes(packet.buffer);
    }

    public static BufferPacket decode(FriendlyByteBuf buf) {
        FriendlyByteBuf newBuf = new FriendlyByteBuf(buf.copy());
        return new BufferPacket(newBuf);
    }

    public static void handle(BufferPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                // Server to client packet
                ForgeClient.handleNetworkPacket(packet.buffer);
            } else {
                // Client to server packet
                ServerPlayer sender = ctx.get().getSender();
                Network.INSTANCE.handlePacket(sender, packet.buffer);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void handle(@NotNull PacketListener listener) {
        // Handled by Forge network system
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBytes(buffer);
    }
}
