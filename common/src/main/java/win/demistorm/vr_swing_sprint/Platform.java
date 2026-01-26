package win.demistorm.vr_swing_sprint;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

// Platform abstraction for loader-specific code
public interface Platform {

    // Send packet from client to server
    void sendToServer(FriendlyByteBuf packet);

    // Send packet from server to specific client
    void sendToPlayer(ServerPlayer player, FriendlyByteBuf packet);
}
