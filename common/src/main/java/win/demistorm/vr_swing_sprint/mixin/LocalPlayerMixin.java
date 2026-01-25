package win.demistorm.vr_swing_sprint.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.VRAPI;
import win.demistorm.vr_swing_sprint.client.SprintHelper;

// Mixin to modify sprint behavior for VR players on servers with mod
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    // Invokers for private methods in LocalPlayer
    @Invoker("hasEnoughFoodToStartSprinting")
    protected abstract boolean invokeHasEnoughFoodToStartSprinting();

    @Invoker("isRidingCamel")
    protected abstract boolean invokeIsRidingCamel();

    // Inject into aiStep to modify inline sprint stopping
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

        // Check if player is sprinting and not swimming
        if (!player.isSprinting() || player.isSwimming()) {
            return;
        }

        // Only keep food and water checks (removes collision and forward impulse checks)
        boolean shouldStop = !this.invokeHasEnoughFoodToStartSprinting() ||
                player.isInWater() && !player.isUnderWater();

        if (!shouldStop) {
            // Cancel the vanilla stop sprinting logic
            ci.cancel();
        }
    }

    // Inject into shouldStopSprinting to remove blindness and moving slowly checks
    @Inject(
            method = "shouldStopSprinting",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyShouldStopSprinting(CallbackInfoReturnable<Boolean> cir) {
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

        // Only keep some checks (removes blindness and moving slowly checks)
        boolean shouldStop =
                player.isFallFlying() ||
                        player.isPassenger() && !this.invokeIsRidingCamel() ||
                        player.isUsingItem() && !player.isPassenger() && !player.isUnderWater();

        cir.setReturnValue(shouldStop);
    }
}
