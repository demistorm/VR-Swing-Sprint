package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.network.Network;

// Forge mod entry point
@Mod("vr_swing_sprint")
public class VRSwingSprintForge {

    public static final EventNetworkChannel NETWORK = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(VRSwingSprint.MOD_ID, "network"))
            .acceptedVersions((status, version) -> true)
            .optional()
            .networkProtocolVersion(0)
            .eventNetworkChannel();

    public VRSwingSprintForge() {
        VRSwingSprint.LOGGER.info("VR Swing Sprint (FORGE) starting!");

        // Make sure Vivecraft is installed
        try {
            Class.forName("org.vivecraft.api.VRAPI");
            VRSwingSprint.LOGGER.info("Vivecraft detected! VR swing sprint enabled.");
        } catch (ClassNotFoundException e) {
            VRSwingSprint.LOGGER.error("Vivecraft not found! VR Swing Sprint requires Vivecraft to function.");
            throw new RuntimeException("Vivecraft is required for VR Swing Sprint");
        }

        // Set up packet handling for bidirectional communication
        NETWORK.addListener(event -> {
            FriendlyByteBuf payload = event.getPayload();
            if (payload == null) return;

            if (event.getSource().isServerSide()) {
                // Client to server packet
                var sender = event.getSource().getSender();
                if (sender != null) {
                    PlatformImpl.handleClientPacket(payload, sender);
                }
            } else {
                // Server to client packet
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    ForgeClient.handleNetworkPacket(payload);
                }
            }
            event.getSource().setPacketHandled(true);
        });

        // Initialize platform with registered channel
        PlatformImpl.init(NETWORK);

        // Run common setup
        VRSwingSprint.initialize();

        // Set up client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.initialize();
        }

        VRSwingSprint.LOGGER.info("VR Swing Sprint (FORGE) initialization complete!");
    }
}
