package win.demistorm.vr_swing_sprint.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.network.Network;

// Fabric mod entry point
public final class VRSwingSprintFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        VRSwingSprint.LOGGER.info("VR Swing Sprint (FABRIC) starting!");

        // Handle incoming packets from client
        ServerPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (server, player, handler, buf, responseSender) -> {
            buf.retain();
            server.execute(() -> {
                try {
                    Network.INSTANCE.handlePacket(player, buf);
                } finally {
                    buf.release();
                }
            });
        });

        // Initialize platform networking
        PlatformImpl.init();

        // Run common setup
        VRSwingSprint.initialize();

        VRSwingSprint.LOGGER.info("VR Swing Sprint (FABRIC) initialization complete!");
    }
}
