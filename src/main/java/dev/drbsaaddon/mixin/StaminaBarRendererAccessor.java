package dev.drbsaaddon.mixin;

import dev.muon.dynamic_resource_bars.render.StaminaBarRenderer;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StaminaBarRenderer.class, remap = false)
public interface StaminaBarRendererAccessor {
   @Accessor("lastStamina")
   static float drbsa$getLastStamina() {
      throw new AssertionError();
   }

   @Accessor("lastStamina")
   static void drbsa$setLastStamina(float value) {
      throw new AssertionError();
   }

   @Accessor("fullStaminaStartTime")
   static long drbsa$getFullStaminaStartTime() {
      throw new AssertionError();
   }

   @Accessor("fullStaminaStartTime")
   static void drbsa$setFullStaminaStartTime(long value) {
      throw new AssertionError();
   }

   @Accessor("staminaBarSetVisible")
   static boolean drbsa$getStaminaBarSetVisible() {
      throw new AssertionError();
   }

   @Accessor("staminaBarSetVisible")
   static void drbsa$setStaminaBarSetVisible(boolean value) {
      throw new AssertionError();
   }

   @Accessor("staminaBarDisabledStartTime")
   static long drbsa$getStaminaBarDisabledStartTime() {
      throw new AssertionError();
   }

   @Accessor("staminaBarDisabledStartTime")
   static void drbsa$setStaminaBarDisabledStartTime(long value) {
      throw new AssertionError();
   }

   @Accessor("lastMountHealth")
   static float drbsa$getLastMountHealth() {
      throw new AssertionError();
   }

   @Accessor("lastMountHealth")
   static void drbsa$setLastMountHealth(float value) {
      throw new AssertionError();
   }

   @Accessor("lastMountMaxHealth")
   static float drbsa$getLastMountMaxHealth() {
      throw new AssertionError();
   }

   @Accessor("lastMountMaxHealth")
   static void drbsa$setLastMountMaxHealth(float value) {
      throw new AssertionError();
   }

   @Accessor("fullMountHealthStartTime")
   static long drbsa$getFullMountHealthStartTime() {
      throw new AssertionError();
   }

   @Accessor("fullMountHealthStartTime")
   static void drbsa$setFullMountHealthStartTime(long value) {
      throw new AssertionError();
   }

   @Accessor("previousValue")
   static float drbsa$getPreviousValue() {
      throw new AssertionError();
   }

   @Accessor("previousValue")
   static void drbsa$setPreviousValue(float value) {
      throw new AssertionError();
   }

   @Accessor("previousMax")
   static float drbsa$getPreviousMax() {
      throw new AssertionError();
   }

   @Accessor("previousMax")
   static void drbsa$setPreviousMax(float value) {
      throw new AssertionError();
   }

   @Accessor("wasMounted")
   static boolean drbsa$getWasMounted() {
      throw new AssertionError();
   }

   @Accessor("wasMounted")
   static void drbsa$setWasMounted(boolean value) {
      throw new AssertionError();
   }

   @Accessor("fadingChunks")
   static List<Object> drbsa$getFadingChunks() {
      throw new AssertionError();
   }
}
