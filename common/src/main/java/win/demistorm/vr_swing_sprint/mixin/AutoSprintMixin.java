package win.demistorm.vr_swing_sprint.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.settings.VRSettings;

// Mixin to disable Vivecraft's autosprint so that this mod is the trigger
@Mixin(value = MCVR.class, remap = false)
public class AutoSprintMixin {

    // Intercepts reads to VRSettings.autoSprint and always returns false
    // This prevents Vivecraft's autosprint from activating without modifying config files
    @Redirect(
        method = "processBindings",
        at = @At(
            value = "FIELD",
            target = "Lorg/vivecraft/client_vr/settings/VRSettings;autoSprint:Z",
            ordinal = 0
        ),
        remap = false
    )
    private boolean disableAutoSprint(VRSettings instance) {
        return false; // Always disable autosprint so swinging is the only trigger
    }
}
