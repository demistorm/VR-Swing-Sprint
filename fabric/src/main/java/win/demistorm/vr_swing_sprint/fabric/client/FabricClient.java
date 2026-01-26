package win.demistorm.vr_swing_sprint.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import win.demistorm.vr_swing_sprint.client.SprintHelper;
import win.demistorm.vr_swing_sprint.client.ClientNetworkHelper;
import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;
import win.demistorm.vr_swing_sprint.fabric.BufferPacket;
import win.demistorm.vr_swing_sprint.network.Network;

// Fabric client setup and networking
public final class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Initialize clientside systems
        VRSwingSprintClient.initializeClient();

        // Register clientside packet receiver using 1.20.1 API
        ClientPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (client, handler, buf, responseSender) -> {
            buf.retain();
            client.execute(() -> {
                try {
                    if (client.player != null) {
                        Network.INSTANCE.handlePacket(client.player, buf);
                    }
                } finally {
                    buf.release();
                }
            });
        });

        // Trigger handshake when player joins a world
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SprintHelper.startHandshake();
            ClientNetworkHelper.sendCapabilityQuery();
        });
    }
}
