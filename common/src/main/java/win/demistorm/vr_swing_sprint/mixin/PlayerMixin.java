package win.demistorm.vr_swing_sprint.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.VRAPI;
import win.demistorm.vr_swing_sprint.VRSwingSprint;
import win.demistorm.vr_swing_sprint.network.PlayerSpeedAttachment;

// Mixin to apply custom sprint speed multiplier on server-side
@Mixin(Player.class)
public class PlayerMixin {

    // Unique ID for our custom sprint speed modifier
    private static final ResourceLocation SPRINTING_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath("vr_swing_sprint", "custom_speed");

    // Inject at the start of aiStep to apply custom speed modifier
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void applyCustomSprintSpeed(CallbackInfo ci) {
        Player player = (Player)(Object)this;

        // Safety check 1: Server-side only
        if (!(player instanceof ServerPlayer)) return;
        if (player.level().isClientSide) return;

        // Safety check 2: Only for VR players
        try {
            if (!VRAPI.instance().isVRPlayer(player)) {
                return;
            }
        } catch (Exception e) {
            // Vivecraft API not available or error checking
            return;
        }

        // Safety check 3: Player must be sprinting
        if (!player.isSprinting()) {
            return;
        }

        // Get custom speed multiplier from attachment
        PlayerSpeedAttachment speedData = PlayerSpeedAttachment.get((ServerPlayer) player);
        float customMultiplier = speedData.getMultiplier();

        // Skip if no custom multiplier (client hasn't sent one yet, or server doesn't support)
        if (customMultiplier <= 0.0f) {
            return;
        }

        // Apply custom speed modifier
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Remove our old modifier if present
            speedAttr.removeModifier(SPRINTING_MODIFIER_ID);

            // Add our custom modifier
            // Note: multiplier is a percentage boost (e.g., 0.15 = 15% increase)
            speedAttr.addTransientModifier(new AttributeModifier(
                SPRINTING_MODIFIER_ID,
                customMultiplier,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }
}
