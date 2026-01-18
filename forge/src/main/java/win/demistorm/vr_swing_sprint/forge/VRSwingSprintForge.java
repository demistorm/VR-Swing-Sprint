package win.demistorm.vr_swing_sprint.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import win.demistorm.vr_swing_sprint.VRSwingSprint;

// Forge mod entry point
@Mod("vr_swing_sprint")
public class VRSwingSprintForge {

    public VRSwingSprintForge(FMLJavaModLoadingContext context) {
        VRSwingSprint.LOGGER.info("VR Swing Sprint (FORGE) starting!");

        // Get Forge event bus
        IEventBus modEventBus = context.getModEventBus();

        // Make sure Vivecraft is installed
        try {
            Class.forName("org.vivecraft.api.VRAPI");
            VRSwingSprint.LOGGER.info("Vivecraft detected! VR swing sprint enabled.");
        } catch (ClassNotFoundException e) {
            VRSwingSprint.LOGGER.error("Vivecraft not found! VR Swing Sprint requires Vivecraft to function.");
            throw new RuntimeException("Vivecraft is required for VR Swing Sprint");
        }

        // Run common setup
        VRSwingSprint.initialize();

        // Set up client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup();
        }

        VRSwingSprint.LOGGER.info("VR Swing Sprint (FORGE) initialization complete!");
    }
}
