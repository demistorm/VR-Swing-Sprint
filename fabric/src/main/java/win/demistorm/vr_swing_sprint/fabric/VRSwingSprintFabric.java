package win.demistorm.vr_swing_sprint.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.network.Network;

public final class VRSwingSprintFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register packet types with Fabric
        PayloadTypeRegistry.playC2S().register(BufferPacket.ID, BufferPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BufferPacket.ID, BufferPacket.CODEC);

        // Handle incoming packets from client
        ServerPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (payload, context) -> {
            payload.buffer().retain();
            context.server().execute(() -> {
                try {
                    Network.INSTANCE.handlePacket(context.player(), payload.buffer());
                } finally {
                    payload.buffer().release();
                }
            });
        });

        // Initialize platform networking
        win.demistorm.vr_swing_sprint.fabric.PlatformImpl.init();

        // Run our common setup (includes Network.initialize())
        VRSwingSprint.initialize();
    }
}
