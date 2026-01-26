package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.client.ClientNetworkHelper;
import win.demistorm.vr_swing_sprint.client.SprintHelper;
import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;
import win.demistorm.vr_swing_sprint.network.Network;

// Forge client initialization
@Mod.EventBusSubscriber(modid = "vr_swing_sprint", value = Dist.CLIENT)
public class ForgeClient {

    // Initialize clientside platform code
    public static void initialize() {
        // Initialize clientside systems
        VRSwingSprintClient.initializeClient();
    }

    // Handle player joining event
    @SubscribeEvent
    public static void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        VRSwingSprint.LOGGER.info("Player joining game, starting handshake");
        SprintHelper.startHandshake();
        ClientNetworkHelper.sendCapabilityQuery();
    }

    // Handle incoming packets from server
    public static void handleNetworkPacket(FriendlyByteBuf payload) {
        // Convert to RegistryFriendlyByteBuf with registry access
        Minecraft client = Minecraft.getInstance();
        var connection = client.getConnection();
        if (connection != null) {
            RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(payload, connection.registryAccess());
            Network.INSTANCE.handlePacket(client.player, registryBuf);
        }
    }
}
