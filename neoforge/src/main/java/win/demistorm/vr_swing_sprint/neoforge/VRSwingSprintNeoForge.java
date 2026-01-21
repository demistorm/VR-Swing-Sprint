package win.demistorm.vr_swing_sprint.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import win.demistorm.vr_swing_sprint.VRSwingSprint;

@Mod(VRSwingSprint.MOD_ID)
public final class VRSwingSprintNeoForge {
    public VRSwingSprintNeoForge() {
        // Run our common setup.
        VRSwingSprint.initialize();

        // Set up client side
        if (FMLEnvironment.getDist().isClient()) {
            NeoClient.initialize();
        }
    }
}
