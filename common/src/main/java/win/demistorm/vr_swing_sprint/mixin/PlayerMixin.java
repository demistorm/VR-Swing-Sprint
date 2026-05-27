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

import java.util.UUID;

// Mixin to apply custom sprint speed multiplier serverside
@Mixin(Player.class)
public class PlayerMixin {

    // Unique ID for custom sprint speed modifier
    @Unique
    private static final UUID SPRINTING_MODIFIER_UUID =
        UUID.fromString("1f3e5d7a-9b2c-4f6a-8e3d-1a2b3c4d5e6f");

    @Unique
    private static final ResourceLocation SPRINTING_MODIFIER_ID =
        new ResourceLocation("vr_swing_sprint", "custom_speed");

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

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        // Clean up any stale modifier from a previous sprint
        if (!player.isSprinting()) {
            if (speedAttr.getModifier(SPRINTING_MODIFIER_UUID) != null) {
                speedAttr.removeModifier(SPRINTING_MODIFIER_UUID);
            }
            return;
        }

        // Get custom speed multiplier from storage
        float customMultiplier = PlayerSpeedStorage.getMultiplier((ServerPlayer) player);

        // Skip if no custom multiplier (none sent?)
        if (customMultiplier <= 0.0f) {
            return;
        }

        // Remove old modifier and apply updated one
        speedAttr.removeModifier(SPRINTING_MODIFIER_UUID);
        speedAttr.addTransientModifier(new AttributeModifier(
            SPRINTING_MODIFIER_UUID,
            SPRINTING_MODIFIER_ID.toString(),
            customMultiplier,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }
}
