package win.demistorm.vr_swing_sprint.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.client.SprintHelper;
import win.demistorm.vr_swing_sprint.client.ClientNetworkHelper;
import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;
import win.demistorm.vr_swing_sprint.fabric.BufferPacket;
import win.demistorm.vr_swing_sprint.network.Network;

public final class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Initialize clientside systems
        VRSwingSprintClient.initializeClient();

        // Register clientside packet receiver
        ClientPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (payload, context) -> {
            // Packets from server to client
            payload.buffer().retain();
            context.client().execute(() -> {
                try {
                    LocalPlayer player = context.client().player;
                    if (player != null) {
                        Network.INSTANCE.handlePacket(player, payload.buffer());
                    }
                } finally {
                    payload.buffer().release();
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
