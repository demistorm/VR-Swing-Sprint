package win.demistorm.vr_swing_sprint.neoforge;

import win.demistorm.vr_swing_sprint.VRSwingSprint;
import net.neoforged.fml.common.Mod;

@Mod(VRSwingSprint.MOD_ID)
public final class VRSwingSprintNeoForge {
    public VRSwingSprintNeoForge() {
        // Run our common setup.
        VRSwingSprint.init();
    }
}
