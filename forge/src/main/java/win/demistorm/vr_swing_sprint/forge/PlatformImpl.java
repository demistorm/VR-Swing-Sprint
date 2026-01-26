package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import win.demistorm.vr_swing_sprint.Platform;
import win.demistorm.vr_swing_sprint.PlatformHolder;

// Forge-specific platform implementation
public final class PlatformImpl implements Platform {

    private PlatformImpl() {}

    private static final Platform INSTANCE = new PlatformImpl();

    public static void init() {
        PlatformHolder.setPlatform(INSTANCE);
    }

    @Override
    public void sendToServer(FriendlyByteBuf packet) {
        VRSwingSprintForge.NETWORK.sendToServer(new BufferPacket(packet));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, FriendlyByteBuf packet) {
        VRSwingSprintForge.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new BufferPacket(packet));
    }
}
