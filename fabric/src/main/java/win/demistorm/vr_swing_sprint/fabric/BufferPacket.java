package win.demistorm.vr_swing_sprint.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import win.demistorm.vr_swing_sprint.VRSwingSprint;

// Fabric packet wrapper for cross-platform networking
public record BufferPacket(FriendlyByteBuf buffer) {

    public static final ResourceLocation ID =
        new ResourceLocation(VRSwingSprint.MOD_ID, "network");

    public static BufferPacket read(FriendlyByteBuf buffer) {
        return new BufferPacket(new FriendlyByteBuf(buffer.readBytes(buffer.readInt())));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.buffer.readableBytes());
        buffer.writeBytes(this.buffer);
        this.buffer.resetReaderIndex();
    }
}
