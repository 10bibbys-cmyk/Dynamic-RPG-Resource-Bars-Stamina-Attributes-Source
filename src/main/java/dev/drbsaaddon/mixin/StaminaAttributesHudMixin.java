package dev.drbsaaddon.mixin;

import com.github.theredbrain.staminaattributes.registry.ClientEventsRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ClientEventsRegistry.class, remap = false)
public abstract class StaminaAttributesHudMixin {
   @ModifyArg(
      method = "initializeClientEvents()V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V",
         ordinal = 0
      ),
      index = 0,
      remap = false
   )
   private static Object drbsa$disableNativeStaminaHud(Object originalListener) {
      return (HudRenderCallback)(drawContext, tickDelta) -> {
      };
   }
}
