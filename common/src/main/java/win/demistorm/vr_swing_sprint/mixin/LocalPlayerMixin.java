package win.demistorm.vr_swing_sprint.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.VRAPI;
import win.demistorm.vr_swing_sprint.client.SprintHelper;

// Mixin to modify sprint behavior for VR players on servers with mod
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    // Inject into aiStep to modify the non-swimming sprint stop logic
    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V",
                    ordinal = 1
            ),
            cancellable = true
    )
    private void modifyAiStepSprintLogic(CallbackInfo ci) {
        // Only modify sprint behavior if server confirmed it has mod
        if (!SprintHelper.hasServerCapability()) {
            return;
        }

        LocalPlayer player = (LocalPlayer)(Object)this;

        // Only modify behavior for VR players
        try {
            if (!VRAPI.instance().isVRPlayer(player)) {
                return;
            }
        } catch (Exception e) {
            // Vivecraft API not available
            return;
        }

        // Skip if swimming
        if (player.isSwimming()) {
            return;
        }

        // Remove forward impulse and collision checks for VR players
        boolean shouldStop =
            player.getFoodData().getFoodLevel() < 6 ||
            player.isInWater() && !player.isUnderWater();

        if (!shouldStop) {
            // Cancel the vanilla stop sprinting logic
            ci.cancel();
        }
    }
}
