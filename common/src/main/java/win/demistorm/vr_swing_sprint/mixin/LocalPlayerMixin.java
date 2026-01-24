package win.demistorm.vr_swing_sprint.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.demistorm.vr_swing_sprint.client.SprintHelper;

// Mixin to allow sprinting in tight spaces for VR players on modded servers
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    // Inject into shouldStopRunSprinting to allow sprinting even with minor collisions
    // ONLY if server has VR Swing Sprint installed (prevents anti-cheat issues on vanilla servers)
    @Inject(
        method = "shouldStopRunSprinting",
        at = @At("HEAD"),
        cancellable = true
    )
    private void allowSprintInTightSpaces(CallbackInfoReturnable<Boolean> cir) {
        // Only modify sprint behavior if server confirmed it has our mod
        if (!SprintHelper.hasServerCapability()) {
            // Vanilla server - use default behavior (may stop sprinting with collisions)
            return;
        }

        // Server has mod - allow sprinting even with minor horizontal collisions
        // This makes VR sprinting feel much more natural
        cir.setReturnValue(false); // Don't stop sprinting
    }
}
