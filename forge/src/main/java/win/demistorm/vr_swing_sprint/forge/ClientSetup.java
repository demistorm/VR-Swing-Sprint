package win.demistorm.vr_swing_sprint.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

// Client setup for Forge
public class ClientSetup {

    // Initialize client-side code
    public static void doClientSetup() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Client-specific initialization goes here
            PlatformClientImpl.initialize();
        }
    }
}
