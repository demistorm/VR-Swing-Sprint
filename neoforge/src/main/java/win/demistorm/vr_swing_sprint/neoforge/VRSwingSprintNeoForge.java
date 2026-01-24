package win.demistorm.vr_swing_sprint.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import win.demistorm.vr_swing_sprint.VRSwingSprint;

@Mod(VRSwingSprint.MOD_ID)
public final class VRSwingSprintNeoForge {

    public VRSwingSprintNeoForge(IEventBus modEventBus) {
        VRSwingSprint.LOGGER.info("VR Swing Sprint (NEOFORGE) starting!");

        // Set up networking
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            final PayloadRegistrar registrar = event.registrar(VRSwingSprint.MOD_ID).optional();

            // Register bidirectional packet handler
            registrar.playBidirectional(BufferPacket.TYPE, BufferPacket.STREAM_CODEC,
                (packet, context) -> {
                    if (context.flow().isClientbound()) {
                        // Server to client packets
                        if (FMLEnvironment.dist.isClient()) {
                            NeoClient.handleNetworkPacket(packet.buffer());
                        }
                    } else {
                        // Client to server packets
                        handleServerPacket(packet.buffer(), context);
                    }
                });

            VRSwingSprint.LOGGER.info("Registered NeoForge network handlers");
        });

        PlatformImpl.init();

        VRSwingSprint.initialize();

        // Set up client side
        if (FMLEnvironment.getDist().isClient()) {
            NeoClient.initialize();
        }

        VRSwingSprint.LOGGER.info("VR Swing Sprint (NEOFORGE) initialization complete!");
    }

    // Process client packets on server
    private void handleServerPacket(RegistryFriendlyByteBuf buffer, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PlatformImpl.handleClientPacket(buffer, player);
        });
    }
}
