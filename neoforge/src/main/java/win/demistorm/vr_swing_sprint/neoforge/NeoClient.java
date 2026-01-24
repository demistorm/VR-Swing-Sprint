package win.demistorm.vr_swing_sprint.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.client.ClientNetworkHelper;
import win.demistorm.vr_swing_sprint.client.SprintHelper;
import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;
import win.demistorm.vr_swing_sprint.network.Network;

// NeoForge client initialization
public class NeoClient {

    // Initialize clientside code
    public static void initialize() {
        VRSwingSprintClient.initializeClient();

        // Register clientside join event
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            VRSwingSprint.LOGGER.info("Player joining game, starting handshake");
            SprintHelper.startHandshake();
            ClientNetworkHelper.sendCapabilityQuery();
        });
    }

    // Handle incoming packets from server
    public static void handleNetworkPacket(RegistryFriendlyByteBuf buffer) {
        // Forward to NetworkChannel
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener connection = client.getConnection();
        if (connection != null && client.player != null) {
            Network.INSTANCE.handlePacket(client.player, buffer);
        }
    }
}
