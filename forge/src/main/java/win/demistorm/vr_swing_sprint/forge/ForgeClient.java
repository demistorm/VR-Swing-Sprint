package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.client.ClientNetworkHelper;
import win.demistorm.vr_swing_sprint.client.SprintHelper;
import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;
import win.demistorm.vr_swing_sprint.network.Network;

// Forge client initialization
public class ForgeClient {

    // Initialize clientside platform code
    public static void initialize() {
        // Initialize clientside systems
        VRSwingSprintClient.initializeClient();

        // Register clientside join event
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            VRSwingSprint.LOGGER.info("Player joining game, starting handshake");
            SprintHelper.startHandshake();
            ClientNetworkHelper.sendCapabilityQuery();
        });
    }

    // Handle incoming packets from server
    public static void handleNetworkPacket(FriendlyByteBuf payload) {
        // Forward to network handler
        Network.INSTANCE.handlePacket(Minecraft.getInstance().player, payload);
    }
}
