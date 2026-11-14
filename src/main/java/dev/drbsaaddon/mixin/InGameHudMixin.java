package dev.drbsaaddon.mixin;

import dev.drbsaaddon.StaminaCompatHooks;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_329", remap = false)
public abstract class InGameHudMixin {
   @Inject(method = "method_1760(Lnet/minecraft/class_332;)V", at = @At("TAIL"), remap = false)
   private void drbsa$renderSeparateStamina(DrawContext var1, CallbackInfo var2) {
      StaminaCompatHooks.renderSeparateStamina(var1);
   }

   @Inject(method = "method_1749(Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$hideHeldItemTooltip(DrawContext var1, CallbackInfo var2) {
      if (StaminaCompatHooks.shouldDisableHeldItemTooltip()) {
         var2.cancel();
      }
   }
}
