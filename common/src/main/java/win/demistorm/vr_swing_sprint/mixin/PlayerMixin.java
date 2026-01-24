package win.demistorm.vr_swing_sprint.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.VRAPI;
import win.demistorm.vr_swing_sprint.network.PlayerSpeedStorage;

// Mixin to apply custom sprint speed multiplier serverside
@Mixin(Player.class)
public class PlayerMixin {

    // Unique ID for custom sprint speed modifier
    @Unique
    private static final ResourceLocation SPRINTING_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath("vr_swing_sprint", "custom_speed");

    // Inject at start of aiStep to apply custom speed modifier
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void applyCustomSprintSpeed(CallbackInfo ci) {
        Player player = (Player)(Object)this;

        // Serverside only
        if (!(player instanceof ServerPlayer)) return;
        if (player.level().isClientSide) return;

        // Only for VR players
        try {
            if (!VRAPI.instance().isVRPlayer(player)) {
                return;
            }
        } catch (Exception e) {
            // Vivecraft API not available
            return;
        }

        // Player must be sprinting
        if (!player.isSprinting()) {
            return;
        }

        // Get custom speed multiplier from storage
        float customMultiplier = PlayerSpeedStorage.getMultiplier((ServerPlayer) player);

        // Skip if no custom multiplier (none sent?)
        if (customMultiplier <= 0.0f) {
            return;
        }

        // Apply custom speed modifier
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Remove old modifier if present
            speedAttr.removeModifier(SPRINTING_MODIFIER_ID);

            // Add custom modifier
            speedAttr.addTransientModifier(new AttributeModifier(
                SPRINTING_MODIFIER_ID,
                customMultiplier,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }
}
