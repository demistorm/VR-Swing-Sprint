package win.demistorm.vr_swing_sprint.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.VRAPI;
import win.demistorm.vr_swing_sprint.client.SprintHelper;

// Mixin to modify sprint behavior for VR players on servers with mod
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    // Invokers for private methods in LocalPlayer
    @Invoker("hasEnoughFoodToSprint")
    protected abstract boolean invokeHasEnoughFoodToSprint();

    @Invoker("vehicleCanSprint")
    protected abstract boolean invokeVehicleCanSprint(Entity vehicle);

    // Inject into shouldStopRunSprinting to disable checks
    @Inject(
            method = "shouldStopRunSprinting",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifySprintBehavior(CallbackInfoReturnable<Boolean> cir) {
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

        // Only keep some checks (removes collision and blindness checks)
        boolean shouldStop =
                !this.invokeHasEnoughFoodToSprint() ||
                        player.isPassenger() && !this.invokeVehicleCanSprint(player.getVehicle()) ||
                        player.isInWater() && !player.isUnderWater();

        cir.setReturnValue(shouldStop);
    }
}