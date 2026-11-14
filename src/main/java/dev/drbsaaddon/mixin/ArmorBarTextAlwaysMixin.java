package dev.drbsaaddon.mixin;

import dev.muon.dynamic_resource_bars.config.ClientConfig;
import dev.muon.dynamic_resource_bars.config.ModConfigManager;
import dev.muon.dynamic_resource_bars.util.TextBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.muon.dynamic_resource_bars.render.ArmorBarRenderer", remap = false)
public abstract class ArmorBarTextAlwaysMixin {
   @Inject(method = "getTextColor()I", at = @At("HEAD"), cancellable = true, remap = false)
   private static void pota$keepArmorTextVisible(CallbackInfoReturnable<Integer> var0) {
      ClientConfig var1 = ModConfigManager.getClient();
      if (var1.showArmorText == TextBehavior.ALWAYS) {
         int var2 = var1.armorTextColor & 16777215;
         int var3 = Math.max(10, Math.min(255, var1.armorTextOpacity));
         var0.setReturnValue(var3 << 24 | var2);
      }
   }
}
