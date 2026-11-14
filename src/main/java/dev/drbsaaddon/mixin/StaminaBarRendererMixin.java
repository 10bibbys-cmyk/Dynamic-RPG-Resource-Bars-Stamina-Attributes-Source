package dev.drbsaaddon.mixin;

import dev.drbsaaddon.StaminaCompatHooks;
import dev.muon.dynamic_resource_bars.util.HUDPositioning.AnchorSide;
import dev.muon.dynamic_resource_bars.util.HUDPositioning.BarPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "dev.muon.dynamic_resource_bars.render.StaminaBarRenderer", remap = false)
public abstract class StaminaBarRendererMixin {
   @Redirect(
      method = "render(Lnet/minecraft/class_332;Lnet/minecraft/class_1657;F)V",
      at = @At(
         value = "INVOKE",
         target = "Ldev/muon/dynamic_resource_bars/util/HUDPositioning$BarPlacement;getSide()Ldev/muon/dynamic_resource_bars/util/HUDPositioning$AnchorSide;"
      ),
      remap = false
   )
   private static AnchorSide drbsa$independentStaminaDrainOrigin(BarPlacement var0) {
      if (StaminaCompatHooks.isRenderingSeparateStamina()) {
         return StaminaCompatHooks.shouldDrainRightToLeft() ? AnchorSide.LEFT : AnchorSide.RIGHT;
      } else {
         return var0.getSide();
      }
   }
}
