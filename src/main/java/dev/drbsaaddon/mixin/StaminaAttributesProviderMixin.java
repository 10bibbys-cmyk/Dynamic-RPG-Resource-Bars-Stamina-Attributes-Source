package dev.drbsaaddon.mixin;

import com.github.theredbrain.staminaattributes.entity.StaminaUsingEntity;
import dev.drbsaaddon.StaminaCompatHooks;
import dev.muon.dynamic_resource_bars.compat.StaminaAttributesProvider;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StaminaAttributesProvider.class, remap = false)
public abstract class StaminaAttributesProviderMixin {
   @Inject(method = "getCurrentStamina", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$getCurrentStamina(PlayerEntity player, CallbackInfoReturnable<Float> cir) {
      if (player instanceof StaminaUsingEntity staminaUser) {
         cir.setReturnValue(staminaUser.staminaattributes$getStamina());
      }
   }

   @Inject(method = "getMaxStamina", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$getMaxStamina(PlayerEntity player, CallbackInfoReturnable<Float> cir) {
      if (player instanceof StaminaUsingEntity staminaUser) {
         cir.setReturnValue(staminaUser.staminaattributes$getMaxStamina());
      }
   }

   @Inject(method = "getBarTexture", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$getSeparateBarTexture(PlayerEntity player, float currentStamina, CallbackInfoReturnable<String> cir) {
      if (StaminaCompatHooks.isRenderingSeparateStamina()) {
         cir.setReturnValue("stamina_bar_cyan");
      }
   }
}
