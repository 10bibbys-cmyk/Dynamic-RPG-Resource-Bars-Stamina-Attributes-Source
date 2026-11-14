package dev.drbsaaddon.mixin;

import dev.drbsaaddon.StaminaCompatHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.muon.dynamic_resource_bars.config.gui.ResizeElementScreen", remap = false)
public abstract class ResizeElementScreenMixin {
   @Inject(method = "method_25419()V", at = @At("TAIL"), remap = false)
   private void drbsa$finishIndependentStaminaResize(CallbackInfo var1) {
      StaminaCompatHooks.finishNativeResizeScreen();
   }
}
