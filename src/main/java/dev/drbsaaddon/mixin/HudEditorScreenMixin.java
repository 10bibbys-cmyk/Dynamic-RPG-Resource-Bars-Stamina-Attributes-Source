package dev.drbsaaddon.mixin;

import dev.drbsaaddon.StaminaCompatHooks;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.muon.dynamic_resource_bars.config.gui.HudEditorScreen", remap = false)
public abstract class HudEditorScreenMixin {
   @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("TAIL"), remap = false)
   private void drbsa$renderStaminaEditor(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      StaminaCompatHooks.editorRender(var1, var2, var3, var4);
   }

   @Inject(method = "method_25402(DDI)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$staminaMouseClicked(double var1, double var3, int var5, CallbackInfoReturnable<Boolean> var6) {
      if (StaminaCompatHooks.editorMouseClicked(this, var1, var3, var5)) {
         var6.setReturnValue(true);
      }
   }

   @Inject(method = "method_25403(DDIDD)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$staminaMouseDragged(double var1, double var3, int var5, double var6, double var8, CallbackInfoReturnable<Boolean> var10) {
      if (StaminaCompatHooks.editorMouseDragged(var1, var3, var5)) {
         var10.setReturnValue(true);
      }
   }

   @Inject(method = "method_25406(DDI)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$staminaMouseReleased(double var1, double var3, int var5, CallbackInfoReturnable<Boolean> var6) {
      if (StaminaCompatHooks.editorMouseReleased(var1, var3, var5)) {
         var6.setReturnValue(true);
      }
   }

   @Inject(method = "method_25404(III)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void drbsa$staminaKeyPressed(int var1, int var2, int var3, CallbackInfoReturnable<Boolean> var4) {
      if (StaminaCompatHooks.editorKeyPressed(var1, var2, var3)) {
         var4.setReturnValue(true);
      }
   }

   @Inject(method = "method_25419()V", at = @At("HEAD"), remap = false)
   private void drbsa$staminaEditorClosed(CallbackInfo var1) {
      StaminaCompatHooks.editorClosed();
   }
}
