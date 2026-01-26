package win.demistorm.vr_swing_sprint.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import win.demistorm.vr_swing_sprint.VRSwingSprint;

// Forge mod entry point
@Mod("vr_swing_sprint")
public class VRSwingSprintForge {

    @SuppressWarnings("removal")
    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(VRSwingSprint.MOD_ID, "network"))
            .networkProtocolVersion(() -> "1.0")
            .serverAcceptedVersions("1.0"::equals)
            .clientAcceptedVersions("1.0"::equals)
            .simpleChannel();

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

        // Initialize platform with network channel
        PlatformImpl.init();

        // Run common setup
        VRSwingSprint.initialize();

        // Register packet handler using 1.20.1 SimpleChannel pattern
        NETWORK.registerMessage(0, BufferPacket.class,
                BufferPacket::encode,
                BufferPacket::decode,
                BufferPacket::handle);

        // Set up client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.initialize();
        }

        VRSwingSprint.LOGGER.info("VR Swing Sprint (FORGE) initialization complete!");
    }
}
