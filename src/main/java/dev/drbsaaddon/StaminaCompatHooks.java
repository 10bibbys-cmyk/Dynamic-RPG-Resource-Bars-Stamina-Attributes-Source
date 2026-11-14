package dev.drbsaaddon;

import dev.muon.dynamic_resource_bars.config.ClientConfig;
import dev.muon.dynamic_resource_bars.config.ModConfigManager;
import dev.muon.dynamic_resource_bars.config.gui.ContextMenu;
import dev.muon.dynamic_resource_bars.config.gui.ContextMenuItem;
import dev.muon.dynamic_resource_bars.config.gui.ResizeElementScreen;
import dev.muon.dynamic_resource_bars.provider.StaminaBarBehavior;
import dev.muon.dynamic_resource_bars.render.StaminaBarRenderer;
import dev.muon.dynamic_resource_bars.util.DraggableElement;
import dev.muon.dynamic_resource_bars.util.EditModeManager;
import dev.muon.dynamic_resource_bars.util.FillDirection;
import dev.muon.dynamic_resource_bars.util.HorizontalAlignment;
import dev.muon.dynamic_resource_bars.util.ScreenRect;
import dev.muon.dynamic_resource_bars.util.SubElementType;
import dev.muon.dynamic_resource_bars.util.TextBehavior;
import dev.muon.dynamic_resource_bars.util.HUDPositioning.BarPlacement;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import dev.drbsaaddon.mixin.StaminaBarRendererAccessor;

public final class StaminaCompatHooks {
   private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.dir"), "config", "dynamic_resource_bars_stamina_attributes.properties");
   private static final Properties PROPS = new Properties();
   private static final ClientConfigSnapshot BASE_CONFIG_SNAPSHOT = new ClientConfigSnapshot();
   private static final ClientConfigSnapshot NATIVE_RESIZE_SNAPSHOT = new ClientConfigSnapshot();
   private static final RendererState BASE_RENDERER_STATE = new RendererState();
   private static final RendererState EXTRA_RENDERER_STATE = new RendererState();
   private static WeakReference<Object> lastRenderedWorld = new WeakReference<>(null);
   private static WeakReference<Object> lastRenderedPlayer = new WeakReference<>(null);
   private static boolean loaded;
   private static StaminaCompatHooks.Settings settings;
   private static StaminaCompatHooks.Settings defaults;
   private static boolean keyboardSelected;
   private static boolean focused;
   private static SubElementType selectedSubElement;
   private static boolean draggingMain;
   private static boolean draggingSub;
   private static boolean resizing;
   private static boolean resizingWhole;
   private static boolean resizeWidth;
   private static boolean resizeHeight;
   private static double dragStartX;
   private static double dragStartY;
   private static StaminaCompatHooks.Settings dragStartSettings;
   private static ContextMenu activeMenu;
   private static boolean nativeResizeSession;
   private static boolean nativeResizeSnapshotValid;
   private static Object activeEditorScreen;
   private static boolean renderingSeparateStamina;
   private static long lastErrorTime;

   private StaminaCompatHooks() {
   }

   public static boolean shouldDisableHeldItemTooltip() {
      ensureLoaded();
      return settings != null && settings.disableHeldItemTooltip;
   }

   public static boolean isRenderingSeparateStamina() {
      return renderingSeparateStamina;
   }

   public static boolean shouldDrainRightToLeft() {
      ensureLoaded();
      return settings == null || settings.drainRightToLeft;
   }

   public static void renderSeparateStamina(DrawContext var0) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1 != null && var1.player != null) {
         ensureLoaded();
         if (settings.enabled || EditModeManager.isEditModeEnabled()) {
            ClientConfig var2 = ModConfigManager.getClient();
            if (var2 != null) {
               ensureRendererStateOwner(var1);
               BASE_CONFIG_SNAPSHOT.capture(var2);
               BASE_RENDERER_STATE.capture();

               try {
                  applySettings(var2, settings);
                  EXTRA_RENDERER_STATE.apply();
                  renderingSeparateStamina = true;
                  StaminaBarRenderer.render(var0, var1.player, var1.getTickDelta());
               } catch (Throwable var7) {
                  rateLimitedError("render", var7);
               } finally {
                  renderingSeparateStamina = false;
                  EXTRA_RENDERER_STATE.capture();
                  BASE_RENDERER_STATE.apply();
                  BASE_CONFIG_SNAPSHOT.apply(var2);
               }
            }
         }
      }
   }

   public static void editorRender(DrawContext var0, int var1, int var2, float var3) {
      if (EditModeManager.isEditModeEnabled()) {
         ensureLoaded();

         try {
            if (focused || keyboardSelected || draggingMain) {
               ScreenRect var4 = getExtraScreenRect();
               if (var4 != null) {
                  drawOutline(var0, var4, -1604265920);
               }

               if (focused && selectedSubElement == null) {
                  ScreenRect var5 = getExtraSubElementRect(SubElementType.BACKGROUND);
                  if (var5 != null && var5.width() > 0 && var5.height() > 0) {
                     drawResizeHandles(var0, var5, var1, var2);
                  }
               }
            }

            if (focused && selectedSubElement != null) {
               ScreenRect var7 = getExtraSubElementRect(selectedSubElement);
               if (var7 != null && var7.width() > 0 && var7.height() > 0) {
                  drawOutline(var0, var7, colorFor(selectedSubElement));
                  if (isResizable(selectedSubElement)) {
                     drawResizeHandles(var0, var7, var1, var2);
                  }
               }
            }

            if (activeMenu != null && activeMenu.isVisible()) {
               activeMenu.render(var0, var1, var2, var3);
            }
         } catch (Throwable var6) {
            rateLimitedError("editor-render", var6);
         }
      }
   }

   public static boolean editorMouseClicked(Object var0, double var1, double var3, int var5) {
      if (!EditModeManager.isEditModeEnabled()) {
         return false;
      }

      ensureLoaded();
      activeEditorScreen = var0;
      if (activeMenu != null && activeMenu.isVisible()) {
         if (activeMenu.mouseClicked(var1, var3, var5)) {
            return true;
         }

         activeMenu.close();
         activeMenu = null;
      }

      ScreenRect var6 = getExtraScreenRect();
      boolean var7 = var6 != null && var6.contains((int)var1, (int)var3);
      if (var5 == 1 && var7) {
         clearNativeEditorSelection();
         keyboardSelected = false;
         SubElementType var12 = focused ? hitTestSubElement(var1, var3) : null;
         activeMenu = var12 != null ? createSubMenu((int)var1, (int)var3, var12) : createMainMenu((int)var1, (int)var3);
         return true;
      }

      if (var5 != 0) {
         return false;
      }

      if (focused && selectedSubElement == null) {
         ScreenRect var8 = getExtraSubElementRect(SubElementType.BACKGROUND);
         int var9 = getResizeHandle(var8, (int)var1, (int)var3);
         if (var9 != 0) {
            clearNativeEditorSelection();
            keyboardSelected = false;
            resizing = true;
            resizingWhole = true;
            resizeWidth = (var9 & 1) != 0;
            resizeHeight = (var9 & 2) != 0;
            dragStartX = var1;
            dragStartY = var3;
            dragStartSettings = settings.copy();
            return true;
         }
      }

      if (focused && selectedSubElement != null && isResizable(selectedSubElement)) {
         ScreenRect var10 = getExtraSubElementRect(selectedSubElement);
         int var13 = getResizeHandle(var10, (int)var1, (int)var3);
         if (var13 != 0) {
            clearNativeEditorSelection();
            keyboardSelected = false;
            resizing = true;
            resizingWhole = false;
            resizeWidth = (var13 & 1) != 0;
            resizeHeight = (var13 & 2) != 0;
            dragStartX = var1;
            dragStartY = var3;
            dragStartSettings = settings.copy();
            return true;
         }
      }

      if (var7) {
         clearNativeEditorSelection();
         keyboardSelected = false;
         if (focused) {
            SubElementType var11 = hitTestSubElement(var1, var3);
            if (var11 != null) {
               selectedSubElement = var11;
               draggingSub = true;
               dragStartX = var1;
               dragStartY = var3;
               dragStartSettings = settings.copy();
               return true;
            }
         }

         focused = true;
         selectedSubElement = null;
         draggingMain = true;
         dragStartX = var1;
         dragStartY = var3;
         dragStartSettings = settings.copy();
         return true;
      } else {
         clearCompatSelection(true);
         return false;
      }
   }

   public static boolean editorMouseDragged(double var0, double var2, int var4) {
      if (var4 == 0 && dragStartSettings != null) {
         int var5 = (int)Math.round(var0 - dragStartX);
         int var6 = (int)Math.round(var2 - dragStartY);
         if (draggingMain) {
            settings.totalX = dragStartSettings.totalX + var5;
            settings.totalY = dragStartSettings.totalY + var6;
            return true;
         }

         if (draggingSub && selectedSubElement != null) {
            applySubMoveFromSnapshot(selectedSubElement, dragStartSettings, var5, var6);
            return true;
         }

         if (resizing) {
            int var7 = resizeWidth ? var5 : 0;
            int var8 = resizeHeight ? var6 : 0;
            if (resizingWhole) {
               applyWholeResizeFromSnapshot(dragStartSettings, var7, var8);
            } else if (selectedSubElement != null) {
               applySubResizeFromSnapshot(selectedSubElement, dragStartSettings, var7, var8);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean editorMouseReleased(double var0, double var2, int var4) {
      if (var4 != 0) {
         return false;
      }

      if (!draggingMain && !draggingSub && !resizing) {
         return false;
      }

      draggingMain = false;
      draggingSub = false;
      resizing = false;
      resizingWhole = false;
      resizeWidth = false;
      resizeHeight = false;
      dragStartSettings = null;
      save();
      return true;
   }

   public static boolean editorKeyPressed(int var0, int var1, int var2) {
      if (!EditModeManager.isEditModeEnabled()) {
         return false;
      }

      ensureLoaded();
      if (var0 == 258) {
         if (focused) {
            cycleSelectedSubElement();
            return true;
         } else if (keyboardSelected) {
            keyboardSelected = false;
            EditModeManager.setKeyboardSelectedElement(DraggableElement.HEALTH_BAR);
            return true;
         } else if (EditModeManager.getFocusedElement() == null && EditModeManager.getKeyboardSelectedElement() == DraggableElement.AIR_BAR) {
            EditModeManager.clearKeyboardSelectedElement();
            EditModeManager.clearKeyboardSelectedSubElement();
            keyboardSelected = true;
            return true;
         } else {
            return false;
         }
      } else {
         if (var0 < 262 || var0 > 265) {
            return false;
         }

         if (!focused && !keyboardSelected) {
            return false;
         }

         byte var3 = 0;
         byte var4 = 0;
         if (var0 == 262) {
            var3 = 1;
         }

         if (var0 == 263) {
            var3 = -1;
         }

         if (var0 == 264) {
            var4 = 1;
         }

         if (var0 == 265) {
            var4 = -1;
         }

         boolean var5 = (var2 & 1) != 0;
         if (focused && selectedSubElement != null) {
            if (var5 && isResizable(selectedSubElement)) {
               resizeSub(selectedSubElement, var3, var4);
            } else {
               moveSub(selectedSubElement, var3, var4);
            }
         } else if (var5) {
            resizeWhole(var3, var4);
         } else {
            settings.totalX += var3;
            settings.totalY += var4;
         }

         save();
         return true;
      }
   }

   public static void editorClosed() {
      if (loaded) {
         save();
      }

      activeMenu = null;
      clearCompatSelection(true);
   }

   private static ContextMenu createMainMenu(int var0, int var1) {
      ArrayList var2 = new ArrayList();
      var2.add(ContextMenuItem.title(text("Stamina Attributes")));
      var2.add(new ContextMenuItem(text("Enabled: " + onOff(settings.enabled)), var0x -> {
         settings.enabled = !settings.enabled;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Anchor: " + settings.anchor.name()), var0x -> {
         settings.anchor = settings.anchor.getNext();
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Fill: " + settings.fillDirection.name()), var0x -> {
         settings.fillDirection = settings.fillDirection == FillDirection.HORIZONTAL ? FillDirection.VERTICAL : FillDirection.HORIZONTAL;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Drain: " + (settings.drainRightToLeft ? "RIGHT -> LEFT" : "LEFT -> RIGHT")), var0x -> {
         settings.drainRightToLeft = !settings.drainRightToLeft;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Edit Size..."), var0x -> {
         activeMenu = null;
         beginNativeResizeScreen();
      }));
      var2.add(new ContextMenuItem(text("Text: " + settings.showText.name()), var0x -> {
         settings.showText = nextText(settings.showText);
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Text Align: " + settings.textAlign.name()), var0x -> {
         settings.textAlign = nextAlign(settings.textAlign);
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Background: " + onOff(settings.enableBackground)), var0x -> {
         settings.enableBackground = !settings.enableBackground;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Foreground: " + onOff(settings.enableForeground)), var0x -> {
         settings.enableForeground = !settings.enableForeground;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Fade When Full: " + onOff(settings.fadeWhenFull)), var0x -> {
         settings.fadeWhenFull = !settings.fadeWhenFull;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Hide Held Item Tooltip: " + onOff(settings.disableHeldItemTooltip)), var0x -> {
         settings.disableHeldItemTooltip = !settings.disableHeldItemTooltip;
         save();
         activeMenu = null;
      }));
      var2.add(ContextMenuItem.separator());
      var2.add(new ContextMenuItem(text("Reset Position"), var0x -> {
         settings.totalX = defaults.totalX;
         settings.totalY = defaults.totalY;
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Reset Sizes"), var0x -> {
         copySizes(defaults, settings);
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Reset Visual Settings"), var0x -> {
         copyVisuals(defaults, settings);
         save();
         activeMenu = null;
      }));
      var2.add(new ContextMenuItem(text("Reset Stamina Bar"), var0x -> {
         settings = defaults.copy();
         save();
         activeMenu = null;
      }));
      return new ContextMenu(var0, var1, var2);
   }

   private static ContextMenu createSubMenu(int var0, int var1, SubElementType var2) {
      ArrayList var3 = new ArrayList();
      var3.add(ContextMenuItem.title(text("Stamina " + friendlySubName(var2))));
      if (var2 == SubElementType.BACKGROUND) {
         var3.add(new ContextMenuItem(text("Visible: " + onOff(settings.enableBackground)), var0x -> {
            settings.enableBackground = !settings.enableBackground;
            save();
            activeMenu = null;
         }));
      } else if (var2 == SubElementType.FOREGROUND) {
         var3.add(new ContextMenuItem(text("Visible: " + onOff(settings.enableForeground)), var0x -> {
            settings.enableForeground = !settings.enableForeground;
            save();
            activeMenu = null;
         }));
      } else if (var2 == SubElementType.TEXT) {
         var3.add(new ContextMenuItem(text("Display: " + settings.showText.name()), var0x -> {
            settings.showText = nextText(settings.showText);
            save();
            activeMenu = null;
         }));
         var3.add(new ContextMenuItem(text("Alignment: " + settings.textAlign.name()), var0x -> {
            settings.textAlign = nextAlign(settings.textAlign);
            save();
            activeMenu = null;
         }));
         var3.add(new ContextMenuItem(text("Text Size: " + settings.textSize), var0x -> {
            settings.textSize += 0.25F;
            if (settings.textSize > 2.0F) {
               settings.textSize = 0.5F;
            }

            save();
            activeMenu = null;
         }));
      }

      var3.add(ContextMenuItem.separator());
      var3.add(new ContextMenuItem(text("Reset Layer Position"), var1x -> {
         resetSubPosition(var2);
         save();
         activeMenu = null;
      }));
      if (isResizable(var2)) {
         var3.add(new ContextMenuItem(text("Reset Layer Size"), var1x -> {
            resetSubSize(var2);
            save();
            activeMenu = null;
         }));
      }

      return new ContextMenu(var0, var1, var3);
   }

   private static Text text(String var0) {
      return Text.literal(var0);
   }

   private static String onOff(boolean var0) {
      return var0 ? "ON" : "OFF";
   }

   private static ScreenRect getExtraScreenRect() {
      return withExtraConfig(() -> {
         MinecraftClient var0 = MinecraftClient.getInstance();
         return var0 != null && var0.player != null ? StaminaBarRenderer.getScreenRect(var0.player) : null;
      });
   }

   private static ScreenRect getExtraSubElementRect(SubElementType var0) {
      return withExtraConfig(() -> {
         MinecraftClient var1 = MinecraftClient.getInstance();
         return var1 != null && var1.player != null ? StaminaBarRenderer.getSubElementRect(var0, var1.player) : null;
      });
   }

   private static SubElementType hitTestSubElement(double var0, double var2) {
      SubElementType[] var4 = new SubElementType[]{SubElementType.TEXT, SubElementType.FOREGROUND, SubElementType.BAR_MAIN, SubElementType.BACKGROUND};

      for (SubElementType var8 : var4) {
         if (isSubVisible(var8)) {
            ScreenRect var9 = getExtraSubElementRect(var8);
            if (var9 != null && var9.width() > 0 && var9.height() > 0 && var9.contains((int)var0, (int)var2)) {
               return var8;
            }
         }
      }

      return null;
   }

   private static boolean isSubVisible(SubElementType var0) {
      if (var0 == SubElementType.BACKGROUND) {
         return settings.enableBackground;
      } else if (var0 == SubElementType.FOREGROUND) {
         return settings.enableForeground;
      } else {
         return var0 == SubElementType.TEXT ? settings.showText != TextBehavior.NEVER : var0 == SubElementType.BAR_MAIN;
      }
   }

   private static void cycleSelectedSubElement() {
      ArrayList var0 = new ArrayList();
      var0.add(SubElementType.BACKGROUND);
      var0.add(SubElementType.BAR_MAIN);
      var0.add(SubElementType.FOREGROUND);
      var0.add(SubElementType.TEXT);
      int var1 = selectedSubElement == null ? -1 : var0.indexOf(selectedSubElement);
      selectedSubElement = (SubElementType)var0.get((var1 + 1) % var0.size());
   }

   private static boolean isResizable(SubElementType var0) {
      return var0 == SubElementType.BACKGROUND || var0 == SubElementType.BAR_MAIN || var0 == SubElementType.FOREGROUND;
   }

   private static int getResizeHandle(ScreenRect var0, int var1, int var2) {
      if (var0 != null && var0.width() > 0 && var0.height() > 0) {
         int var3 = var0.y() + var0.height() / 2;
         int var4 = var0.x() + var0.width() / 2;
         ScreenRect var5 = new ScreenRect(var0.x() + var0.width() - 2, var3 - 2, 5, 5);
         ScreenRect var6 = new ScreenRect(var4 - 2, var0.y() + var0.height() - 2, 5, 5);
         if (var5.contains(var1, var2)) {
            return 1;
         } else {
            return var6.contains(var1, var2) ? 2 : 0;
         }
      } else {
         return 0;
      }
   }

   private static void drawResizeHandles(DrawContext var0, ScreenRect var1, int var2, int var3) {
      int var4 = var1.y() + var1.height() / 2;
      int var5 = var1.x() + var1.width() / 2;
      ScreenRect var6 = new ScreenRect(var1.x() + var1.width() - 2, var4 - 2, 5, 5);
      ScreenRect var7 = new ScreenRect(var5 - 2, var1.y() + var1.height() - 2, 5, 5);
      int var8 = -1604265920;
      int var9 = -1426063361;
      fill(var0, var6, var6.contains(var2, var3) ? var9 : var8);
      fill(var0, var7, var7.contains(var2, var3) ? var9 : var8);
   }

   private static void drawOutline(DrawContext var0, ScreenRect var1, int var2) {
      if (var1 != null && var1.width() > 0 && var1.height() > 0) {
         var0.fill(var1.x(), var1.y(), var1.x() + var1.width(), var1.y() + 1, var2);
         var0.fill(var1.x(), var1.y() + var1.height() - 1, var1.x() + var1.width(), var1.y() + var1.height(), var2);
         var0.fill(var1.x(), var1.y(), var1.x() + 1, var1.y() + var1.height(), var2);
         var0.fill(var1.x() + var1.width() - 1, var1.y(), var1.x() + var1.width(), var1.y() + var1.height(), var2);
      }
   }

   private static void fill(DrawContext var0, ScreenRect var1, int var2) {
      var0.fill(var1.x(), var1.y(), var1.x() + var1.width(), var1.y() + var1.height(), var2);
   }

   private static int colorFor(SubElementType var0) {
      if (var0 == SubElementType.BACKGROUND) {
         return -1610586369;
      } else if (var0 == SubElementType.BAR_MAIN) {
         return -1604265920;
      } else if (var0 == SubElementType.FOREGROUND) {
         return -1593874689;
      } else {
         return var0 == SubElementType.TEXT ? -1593835521 : -1604265920;
      }
   }

   private static void moveSub(SubElementType var0, int var1, int var2) {
      if (var0 == SubElementType.BACKGROUND) {
         settings.backgroundX += var1;
         settings.backgroundY += var2;
      } else if (var0 == SubElementType.BAR_MAIN) {
         settings.barX += var1;
         settings.barY += var2;
      } else if (var0 == SubElementType.FOREGROUND) {
         settings.overlayX += var1;
         settings.overlayY += var2;
      } else if (var0 == SubElementType.TEXT) {
         settings.textX += var1;
         settings.textY += var2;
      }
   }

   private static void resizeSub(SubElementType var0, int var1, int var2) {
      if (var0 == SubElementType.BACKGROUND) {
         settings.backgroundWidth = Math.max(10, settings.backgroundWidth + var1);
         settings.backgroundHeight = Math.max(4, settings.backgroundHeight + var2);
      } else if (var0 == SubElementType.BAR_MAIN) {
         settings.barWidth = clamp(settings.barWidth + var1, 4, 256);
         settings.barHeight = clamp(settings.barHeight + var2, 1, 32);
      } else if (var0 == SubElementType.FOREGROUND) {
         settings.overlayWidth = clamp(settings.overlayWidth + var1, 10, 256);
         settings.overlayHeight = clamp(settings.overlayHeight + var2, 4, 256);
      }
   }

   private static void resizeWhole(int var0, int var1) {
      if (var0 != 0) {
         settings.backgroundWidth = clamp(settings.backgroundWidth + var0, 10, 256);
         settings.barWidth = clamp(settings.barWidth + var0, 4, 256);
         settings.overlayWidth = clamp(settings.overlayWidth + var0, 10, 256);
      }

      if (var1 != 0) {
         settings.backgroundHeight = clamp(settings.backgroundHeight + var1, 4, 256);
         settings.barHeight = clamp(settings.barHeight + var1, 1, 32);
         settings.overlayHeight = clamp(settings.overlayHeight + var1, 4, 256);
      }
   }

   private static void applyWholeResizeFromSnapshot(StaminaCompatHooks.Settings var0, int var1, int var2) {
      if (var1 != 0) {
         settings.backgroundWidth = clamp(var0.backgroundWidth + var1, 10, 256);
         settings.barWidth = clamp(var0.barWidth + var1, 4, 256);
         settings.overlayWidth = clamp(var0.overlayWidth + var1, 10, 256);
      }

      if (var2 != 0) {
         settings.backgroundHeight = clamp(var0.backgroundHeight + var2, 4, 256);
         settings.barHeight = clamp(var0.barHeight + var2, 1, 32);
         settings.overlayHeight = clamp(var0.overlayHeight + var2, 4, 256);
      }
   }

   private static void applySubMoveFromSnapshot(SubElementType var0, StaminaCompatHooks.Settings var1, int var2, int var3) {
      if (var0 == SubElementType.BACKGROUND) {
         settings.backgroundX = var1.backgroundX + var2;
         settings.backgroundY = var1.backgroundY + var3;
      } else if (var0 == SubElementType.BAR_MAIN) {
         settings.barX = var1.barX + var2;
         settings.barY = var1.barY + var3;
      } else if (var0 == SubElementType.FOREGROUND) {
         settings.overlayX = var1.overlayX + var2;
         settings.overlayY = var1.overlayY + var3;
      } else if (var0 == SubElementType.TEXT) {
         settings.textX = var1.textX + var2;
         settings.textY = var1.textY + var3;
      }
   }

   private static void applySubResizeFromSnapshot(SubElementType var0, StaminaCompatHooks.Settings var1, int var2, int var3) {
      if (var0 == SubElementType.BACKGROUND) {
         settings.backgroundWidth = Math.max(10, var1.backgroundWidth + var2);
         settings.backgroundHeight = Math.max(4, var1.backgroundHeight + var3);
      } else if (var0 == SubElementType.BAR_MAIN) {
         settings.barWidth = clamp(var1.barWidth + var2, 4, 256);
         settings.barHeight = clamp(var1.barHeight + var3, 1, 32);
      } else if (var0 == SubElementType.FOREGROUND) {
         settings.overlayWidth = clamp(var1.overlayWidth + var2, 10, 256);
         settings.overlayHeight = clamp(var1.overlayHeight + var3, 4, 256);
      }
   }

   private static void resetSubPosition(SubElementType var0) {
      if (var0 == SubElementType.BACKGROUND) {
         settings.backgroundX = defaults.backgroundX;
         settings.backgroundY = defaults.backgroundY;
      } else if (var0 == SubElementType.BAR_MAIN) {
         settings.barX = defaults.barX;
         settings.barY = defaults.barY;
      } else if (var0 == SubElementType.FOREGROUND) {
         settings.overlayX = defaults.overlayX;
         settings.overlayY = defaults.overlayY;
      } else if (var0 == SubElementType.TEXT) {
         settings.textX = defaults.textX;
         settings.textY = defaults.textY;
      }
   }

   private static void resetSubSize(SubElementType var0) {
      if (var0 == SubElementType.BACKGROUND) {
         settings.backgroundWidth = defaults.backgroundWidth;
         settings.backgroundHeight = defaults.backgroundHeight;
      } else if (var0 == SubElementType.BAR_MAIN) {
         settings.barWidth = defaults.barWidth;
         settings.barHeight = defaults.barHeight;
      } else if (var0 == SubElementType.FOREGROUND) {
         settings.overlayWidth = defaults.overlayWidth;
         settings.overlayHeight = defaults.overlayHeight;
      }
   }

   private static String friendlySubName(SubElementType var0) {
      if (var0 == SubElementType.BACKGROUND) {
         return "Background";
      } else if (var0 == SubElementType.BAR_MAIN) {
         return "Fill";
      } else if (var0 == SubElementType.FOREGROUND) {
         return "Foreground";
      } else {
         return var0 == SubElementType.TEXT ? "Text" : var0.name();
      }
   }

   private static TextBehavior nextText(TextBehavior var0) {
      if (var0 == TextBehavior.ALWAYS) {
         return TextBehavior.WHEN_NOT_FULL;
      } else {
         return var0 == TextBehavior.WHEN_NOT_FULL ? TextBehavior.NEVER : TextBehavior.ALWAYS;
      }
   }

   private static HorizontalAlignment nextAlign(HorizontalAlignment var0) {
      if (var0 == HorizontalAlignment.LEFT) {
         return HorizontalAlignment.CENTER;
      } else {
         return var0 == HorizontalAlignment.CENTER ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT;
      }
   }

   private static void clearNativeEditorSelection() {
      EditModeManager.clearFocusedElement();
      EditModeManager.clearKeyboardSelectedElement();
      EditModeManager.clearKeyboardSelectedSubElement();
      EditModeManager.clearDraggedElement();
      EditModeManager.clearDraggedSubElement();
   }

   private static void clearCompatSelection(boolean var0) {
      focused = false;
      selectedSubElement = null;
      draggingMain = false;
      draggingSub = false;
      resizing = false;
      resizingWhole = false;
      dragStartSettings = null;
      if (var0) {
         keyboardSelected = false;
      }
   }

   private static <T> T withExtraConfig(StaminaCompatHooks.RectSupplier<T> var0) {
      ensureLoaded();
      ClientConfig var1 = ModConfigManager.getClient();
      if (var1 == null) {
         return null;
      }

      BASE_CONFIG_SNAPSHOT.capture(var1);

      try {
         applySettings(var1, settings);
         return (T)var0.get();
      } catch (Throwable var5) {
         rateLimitedError("geometry", var5);
         return null;
      } finally {
         BASE_CONFIG_SNAPSHOT.apply(var1);
      }
   }

   private static void applySettings(ClientConfig var0, StaminaCompatHooks.Settings var1) {
      var0.staminaBarBehavior = StaminaBarBehavior.STAMINA_ATTRIBUTES;
      var0.mergeMountHealth = false;
      var0.enableMountHealth = false;
      var0.staminaBarAnchor = var1.anchor;
      var0.fadeStaminaWhenFull = var1.fadeWhenFull;
      var0.showStaminaText = var1.showText;
      var0.staminaTextAlign = var1.textAlign;
      var0.enableStaminaForeground = var1.enableForeground;
      var0.enableStaminaBackground = var1.enableBackground;
      var0.staminaFillDirection = var1.fillDirection;
      var0.staminaBackgroundWidth = var1.backgroundWidth;
      var0.staminaBackgroundHeight = var1.backgroundHeight;
      var0.staminaBarWidth = var1.barWidth;
      var0.staminaBarHeight = var1.barHeight;
      var0.staminaOverlayWidth = var1.overlayWidth;
      var0.staminaOverlayHeight = var1.overlayHeight;
      var0.staminaOverlayXOffset = var1.overlayX;
      var0.staminaOverlayYOffset = var1.overlayY;
      var0.staminaBarXOffset = var1.barX;
      var0.staminaBarYOffset = var1.barY;
      var0.staminaTotalXOffset = var1.totalX;
      var0.staminaTotalYOffset = var1.totalY;
      var0.staminaBackgroundXOffset = var1.backgroundX;
      var0.staminaBackgroundYOffset = var1.backgroundY;
      var0.staminaTextXOffset = var1.textX;
      var0.staminaTextYOffset = var1.textY;
      var0.staminaTextColor = var1.textColor;
      var0.staminaTextOpacity = var1.textOpacity;
      var0.staminaTextSize = var1.textSize;
   }

   private static void ensureRendererStateOwner(MinecraftClient client) {
      Object world = client.world;
      Object player = client.player;
      if (lastRenderedWorld.get() != world || lastRenderedPlayer.get() != player) {
         EXTRA_RENDERER_STATE.reset();
         lastRenderedWorld = new WeakReference<>(world);
         lastRenderedPlayer = new WeakReference<>(player);
      }
   }

   private static void beginNativeResizeScreen() {
      ensureLoaded();
      if (!nativeResizeSession && activeEditorScreen instanceof Screen) {
         ClientConfig var0 = ModConfigManager.getClient();
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (var0 != null && var1 != null) {
            NATIVE_RESIZE_SNAPSHOT.capture(var0);
            nativeResizeSnapshotValid = true;

            try {
               applySettings(var0, settings);
               nativeResizeSession = true;
               var1.setScreen(new ResizeElementScreen((Screen)activeEditorScreen, DraggableElement.STAMINA_BAR));
            } catch (Throwable var4) {
               nativeResizeSession = false;
               if (nativeResizeSnapshotValid) {
                  NATIVE_RESIZE_SNAPSHOT.apply(var0);
               }

               nativeResizeSnapshotValid = false;
               rateLimitedError("open-resize-screen", var4);
            }
         }
      }
   }

   public static void finishNativeResizeScreen() {
      if (nativeResizeSession) {
         ClientConfig var0 = ModConfigManager.getClient();

         try {
            if (var0 != null) {
               captureSizesAndBackgroundOffsets(var0, settings);
            }

            save();
         } catch (Throwable var10) {
            rateLimitedError("finish-resize-screen", var10);
         } finally {
            try {
               if (var0 != null && nativeResizeSnapshotValid) {
                  NATIVE_RESIZE_SNAPSHOT.apply(var0);
                  var0.save();
               }
            } catch (Throwable var9) {
               rateLimitedError("restore-hunger-after-resize", var9);
            }

            nativeResizeSession = false;
            nativeResizeSnapshotValid = false;
         }
      }
   }

   private static void captureSizesAndBackgroundOffsets(ClientConfig var0, StaminaCompatHooks.Settings var1) {
      var1.backgroundWidth = var0.staminaBackgroundWidth;
      var1.backgroundHeight = var0.staminaBackgroundHeight;
      var1.barWidth = var0.staminaBarWidth;
      var1.barHeight = var0.staminaBarHeight;
      var1.overlayWidth = var0.staminaOverlayWidth;
      var1.overlayHeight = var0.staminaOverlayHeight;
      var1.backgroundX = var0.staminaBackgroundXOffset;
      var1.backgroundY = var0.staminaBackgroundYOffset;
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;
         ClientConfig var0 = ModConfigManager.getClient();
         settings = StaminaCompatHooks.Settings.fromDrb(var0);
         settings.totalY -= 12;
         defaults = settings.copy();
         if (Files.exists(CONFIG_PATH)) {
            try (InputStream var1 = Files.newInputStream(CONFIG_PATH)) {
               PROPS.clear();
               PROPS.load(var1);
               settings.read(PROPS);
            } catch (Exception var6) {
               System.err.println("[DRB Stamina Attributes Addon] Could not read config, using defaults: " + var6.getMessage());
            }
         }
      }
   }

   private static void save() {
      if (loaded && settings != null) {
         try {
            Files.createDirectories(CONFIG_PATH.getParent());
            PROPS.clear();
            settings.write(PROPS);

            try (OutputStream var0 = Files.newOutputStream(CONFIG_PATH)) {
               PROPS.store(var0, "Dynamic RPG Resource Bars - Stamina Attributes Addon");
            }
         } catch (IOException var5) {
            System.err.println("[DRB Stamina Attributes Addon] Could not save config: " + var5.getMessage());
         }
      }
   }

   private static void copySizes(StaminaCompatHooks.Settings var0, StaminaCompatHooks.Settings var1) {
      var1.backgroundWidth = var0.backgroundWidth;
      var1.backgroundHeight = var0.backgroundHeight;
      var1.barWidth = var0.barWidth;
      var1.barHeight = var0.barHeight;
      var1.overlayWidth = var0.overlayWidth;
      var1.overlayHeight = var0.overlayHeight;
   }

   private static void copyVisuals(StaminaCompatHooks.Settings var0, StaminaCompatHooks.Settings var1) {
      var1.fadeWhenFull = var0.fadeWhenFull;
      var1.showText = var0.showText;
      var1.textAlign = var0.textAlign;
      var1.enableForeground = var0.enableForeground;
      var1.enableBackground = var0.enableBackground;
      var1.fillDirection = var0.fillDirection;
      var1.drainRightToLeft = var0.drainRightToLeft;
      var1.textColor = var0.textColor;
      var1.textOpacity = var0.textOpacity;
      var1.textSize = var0.textSize;
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static void rateLimitedError(String var0, Throwable var1) {
      long var2 = System.currentTimeMillis();
      if (var2 - lastErrorTime > 5000L) {
         lastErrorTime = var2;
         System.err.println("[DRB Stamina Attributes Addon] Error during " + var0 + ": " + var1);
      }
   }

   private static final class ClientConfigSnapshot {
      StaminaBarBehavior staminaBarBehavior;
      boolean mergeMountHealth;
      boolean enableMountHealth;
      BarPlacement staminaBarAnchor;
      boolean fadeStaminaWhenFull;
      TextBehavior showStaminaText;
      HorizontalAlignment staminaTextAlign;
      boolean enableStaminaForeground;
      boolean enableStaminaBackground;
      FillDirection staminaFillDirection;
      int staminaBackgroundWidth;
      int staminaBackgroundHeight;
      int staminaBarWidth;
      int staminaBarHeight;
      int staminaOverlayWidth;
      int staminaOverlayHeight;
      int staminaOverlayXOffset;
      int staminaOverlayYOffset;
      int staminaBarXOffset;
      int staminaBarYOffset;
      int staminaTotalXOffset;
      int staminaTotalYOffset;
      int staminaBackgroundXOffset;
      int staminaBackgroundYOffset;
      int staminaTextXOffset;
      int staminaTextYOffset;
      int staminaTextColor;
      int staminaTextOpacity;
      float staminaTextSize;

      void capture(ClientConfig config) {
         this.staminaBarBehavior = config.staminaBarBehavior;
         this.mergeMountHealth = config.mergeMountHealth;
         this.enableMountHealth = config.enableMountHealth;
         this.staminaBarAnchor = config.staminaBarAnchor;
         this.fadeStaminaWhenFull = config.fadeStaminaWhenFull;
         this.showStaminaText = config.showStaminaText;
         this.staminaTextAlign = config.staminaTextAlign;
         this.enableStaminaForeground = config.enableStaminaForeground;
         this.enableStaminaBackground = config.enableStaminaBackground;
         this.staminaFillDirection = config.staminaFillDirection;
         this.staminaBackgroundWidth = config.staminaBackgroundWidth;
         this.staminaBackgroundHeight = config.staminaBackgroundHeight;
         this.staminaBarWidth = config.staminaBarWidth;
         this.staminaBarHeight = config.staminaBarHeight;
         this.staminaOverlayWidth = config.staminaOverlayWidth;
         this.staminaOverlayHeight = config.staminaOverlayHeight;
         this.staminaOverlayXOffset = config.staminaOverlayXOffset;
         this.staminaOverlayYOffset = config.staminaOverlayYOffset;
         this.staminaBarXOffset = config.staminaBarXOffset;
         this.staminaBarYOffset = config.staminaBarYOffset;
         this.staminaTotalXOffset = config.staminaTotalXOffset;
         this.staminaTotalYOffset = config.staminaTotalYOffset;
         this.staminaBackgroundXOffset = config.staminaBackgroundXOffset;
         this.staminaBackgroundYOffset = config.staminaBackgroundYOffset;
         this.staminaTextXOffset = config.staminaTextXOffset;
         this.staminaTextYOffset = config.staminaTextYOffset;
         this.staminaTextColor = config.staminaTextColor;
         this.staminaTextOpacity = config.staminaTextOpacity;
         this.staminaTextSize = config.staminaTextSize;
      }

      void apply(ClientConfig config) {
         config.staminaBarBehavior = this.staminaBarBehavior;
         config.mergeMountHealth = this.mergeMountHealth;
         config.enableMountHealth = this.enableMountHealth;
         config.staminaBarAnchor = this.staminaBarAnchor;
         config.fadeStaminaWhenFull = this.fadeStaminaWhenFull;
         config.showStaminaText = this.showStaminaText;
         config.staminaTextAlign = this.staminaTextAlign;
         config.enableStaminaForeground = this.enableStaminaForeground;
         config.enableStaminaBackground = this.enableStaminaBackground;
         config.staminaFillDirection = this.staminaFillDirection;
         config.staminaBackgroundWidth = this.staminaBackgroundWidth;
         config.staminaBackgroundHeight = this.staminaBackgroundHeight;
         config.staminaBarWidth = this.staminaBarWidth;
         config.staminaBarHeight = this.staminaBarHeight;
         config.staminaOverlayWidth = this.staminaOverlayWidth;
         config.staminaOverlayHeight = this.staminaOverlayHeight;
         config.staminaOverlayXOffset = this.staminaOverlayXOffset;
         config.staminaOverlayYOffset = this.staminaOverlayYOffset;
         config.staminaBarXOffset = this.staminaBarXOffset;
         config.staminaBarYOffset = this.staminaBarYOffset;
         config.staminaTotalXOffset = this.staminaTotalXOffset;
         config.staminaTotalYOffset = this.staminaTotalYOffset;
         config.staminaBackgroundXOffset = this.staminaBackgroundXOffset;
         config.staminaBackgroundYOffset = this.staminaBackgroundYOffset;
         config.staminaTextXOffset = this.staminaTextXOffset;
         config.staminaTextYOffset = this.staminaTextYOffset;
         config.staminaTextColor = this.staminaTextColor;
         config.staminaTextOpacity = this.staminaTextOpacity;
         config.staminaTextSize = this.staminaTextSize;
      }
   }

   private static final class RendererState {
      float lastStamina;
      long fullStaminaStartTime;
      boolean staminaBarSetVisible;
      long staminaBarDisabledStartTime;
      float lastMountHealth;
      float lastMountMaxHealth;
      long fullMountHealthStartTime;
      float previousValue;
      float previousMax;
      boolean wasMounted;
      final List<Object> fadingChunks = new ArrayList<>();

      RendererState() {
         reset();
      }

      void reset() {
         this.lastStamina = -1.0F;
         this.fullStaminaStartTime = 0L;
         this.staminaBarSetVisible = true;
         this.staminaBarDisabledStartTime = 0L;
         this.lastMountHealth = -1.0F;
         this.lastMountMaxHealth = -1.0F;
         this.fullMountHealthStartTime = 0L;
         this.previousValue = -1.0F;
         this.previousMax = -1.0F;
         this.wasMounted = false;
         this.fadingChunks.clear();
      }

      void capture() {
         this.lastStamina = StaminaBarRendererAccessor.drbsa$getLastStamina();
         this.fullStaminaStartTime = StaminaBarRendererAccessor.drbsa$getFullStaminaStartTime();
         this.staminaBarSetVisible = StaminaBarRendererAccessor.drbsa$getStaminaBarSetVisible();
         this.staminaBarDisabledStartTime = StaminaBarRendererAccessor.drbsa$getStaminaBarDisabledStartTime();
         this.lastMountHealth = StaminaBarRendererAccessor.drbsa$getLastMountHealth();
         this.lastMountMaxHealth = StaminaBarRendererAccessor.drbsa$getLastMountMaxHealth();
         this.fullMountHealthStartTime = StaminaBarRendererAccessor.drbsa$getFullMountHealthStartTime();
         this.previousValue = StaminaBarRendererAccessor.drbsa$getPreviousValue();
         this.previousMax = StaminaBarRendererAccessor.drbsa$getPreviousMax();
         this.wasMounted = StaminaBarRendererAccessor.drbsa$getWasMounted();
         this.fadingChunks.clear();
         this.fadingChunks.addAll(StaminaBarRendererAccessor.drbsa$getFadingChunks());
      }

      void apply() {
         StaminaBarRendererAccessor.drbsa$setLastStamina(this.lastStamina);
         StaminaBarRendererAccessor.drbsa$setFullStaminaStartTime(this.fullStaminaStartTime);
         StaminaBarRendererAccessor.drbsa$setStaminaBarSetVisible(this.staminaBarSetVisible);
         StaminaBarRendererAccessor.drbsa$setStaminaBarDisabledStartTime(this.staminaBarDisabledStartTime);
         StaminaBarRendererAccessor.drbsa$setLastMountHealth(this.lastMountHealth);
         StaminaBarRendererAccessor.drbsa$setLastMountMaxHealth(this.lastMountMaxHealth);
         StaminaBarRendererAccessor.drbsa$setFullMountHealthStartTime(this.fullMountHealthStartTime);
         StaminaBarRendererAccessor.drbsa$setPreviousValue(this.previousValue);
         StaminaBarRendererAccessor.drbsa$setPreviousMax(this.previousMax);
         StaminaBarRendererAccessor.drbsa$setWasMounted(this.wasMounted);
         List<Object> rendererChunks = StaminaBarRendererAccessor.drbsa$getFadingChunks();
         rendererChunks.clear();
         rendererChunks.addAll(this.fadingChunks);
      }
   }

   private interface RectSupplier<T> {
      T get() throws Exception;
   }

   private static final class Settings {
      boolean enabled = true;
      boolean disableHeldItemTooltip = false;
      boolean drainRightToLeft = true;
      BarPlacement anchor;
      boolean fadeWhenFull;
      TextBehavior showText;
      HorizontalAlignment textAlign;
      boolean enableForeground;
      boolean enableBackground;
      FillDirection fillDirection;
      int backgroundWidth;
      int backgroundHeight;
      int barWidth;
      int barHeight;
      int overlayWidth;
      int overlayHeight;
      int overlayX;
      int overlayY;
      int barX;
      int barY;
      int totalX;
      int totalY;
      int backgroundX;
      int backgroundY;
      int textX;
      int textY;
      int textColor;
      int textOpacity;
      float textSize;

      static StaminaCompatHooks.Settings fromDrb(ClientConfig var0) {
         StaminaCompatHooks.Settings var1 = new StaminaCompatHooks.Settings();
         if (var0 == null) {
            var1.anchor = BarPlacement.HUNGER;
            var1.showText = TextBehavior.ALWAYS;
            var1.textAlign = HorizontalAlignment.CENTER;
            var1.fillDirection = FillDirection.HORIZONTAL;
            var1.enableBackground = true;
            var1.enableForeground = false;
            var1.backgroundWidth = 80;
            var1.backgroundHeight = 10;
            var1.barWidth = 74;
            var1.barHeight = 4;
            var1.overlayWidth = 80;
            var1.overlayHeight = 10;
            var1.barX = 3;
            var1.barY = 3;
            var1.textX = 3;
            var1.textY = 3;
            var1.textColor = 16777215;
            var1.textOpacity = 200;
            var1.textSize = 1.0F;
            return var1;
         } else {
            var1.anchor = var0.staminaBarAnchor;
            var1.fadeWhenFull = var0.fadeStaminaWhenFull;
            var1.showText = var0.showStaminaText;
            var1.textAlign = var0.staminaTextAlign;
            var1.enableForeground = var0.enableStaminaForeground;
            var1.enableBackground = var0.enableStaminaBackground;
            var1.fillDirection = var0.staminaFillDirection;
            var1.backgroundWidth = var0.staminaBackgroundWidth;
            var1.backgroundHeight = var0.staminaBackgroundHeight;
            var1.barWidth = var0.staminaBarWidth;
            var1.barHeight = var0.staminaBarHeight;
            var1.overlayWidth = var0.staminaOverlayWidth;
            var1.overlayHeight = var0.staminaOverlayHeight;
            var1.overlayX = var0.staminaOverlayXOffset;
            var1.overlayY = var0.staminaOverlayYOffset;
            var1.barX = var0.staminaBarXOffset;
            var1.barY = var0.staminaBarYOffset;
            var1.totalX = var0.staminaTotalXOffset;
            var1.totalY = var0.staminaTotalYOffset;
            var1.backgroundX = var0.staminaBackgroundXOffset;
            var1.backgroundY = var0.staminaBackgroundYOffset;
            var1.textX = var0.staminaTextXOffset;
            var1.textY = var0.staminaTextYOffset;
            var1.textColor = var0.staminaTextColor;
            var1.textOpacity = var0.staminaTextOpacity;
            var1.textSize = var0.staminaTextSize;
            return var1;
         }
      }

      StaminaCompatHooks.Settings copy() {
         StaminaCompatHooks.Settings var1 = new StaminaCompatHooks.Settings();
         var1.enabled = this.enabled;
         var1.disableHeldItemTooltip = this.disableHeldItemTooltip;
         var1.drainRightToLeft = this.drainRightToLeft;
         var1.anchor = this.anchor;
         var1.fadeWhenFull = this.fadeWhenFull;
         var1.showText = this.showText;
         var1.textAlign = this.textAlign;
         var1.enableForeground = this.enableForeground;
         var1.enableBackground = this.enableBackground;
         var1.fillDirection = this.fillDirection;
         var1.backgroundWidth = this.backgroundWidth;
         var1.backgroundHeight = this.backgroundHeight;
         var1.barWidth = this.barWidth;
         var1.barHeight = this.barHeight;
         var1.overlayWidth = this.overlayWidth;
         var1.overlayHeight = this.overlayHeight;
         var1.overlayX = this.overlayX;
         var1.overlayY = this.overlayY;
         var1.barX = this.barX;
         var1.barY = this.barY;
         var1.totalX = this.totalX;
         var1.totalY = this.totalY;
         var1.backgroundX = this.backgroundX;
         var1.backgroundY = this.backgroundY;
         var1.textX = this.textX;
         var1.textY = this.textY;
         var1.textColor = this.textColor;
         var1.textOpacity = this.textOpacity;
         var1.textSize = this.textSize;
         return var1;
      }

      void read(Properties var1) {
         this.enabled = bool(var1, "enabled", this.enabled);
         this.disableHeldItemTooltip = bool(var1, "disableHeldItemTooltip", this.disableHeldItemTooltip);
         this.drainRightToLeft = bool(var1, "drainRightToLeft", this.drainRightToLeft);
         this.anchor = enumVal(var1, "anchor", this.anchor, BarPlacement.class);
         this.fadeWhenFull = bool(var1, "fadeWhenFull", this.fadeWhenFull);
         this.showText = enumVal(var1, "showText", this.showText, TextBehavior.class);
         this.textAlign = enumVal(var1, "textAlign", this.textAlign, HorizontalAlignment.class);
         this.enableForeground = bool(var1, "enableForeground", this.enableForeground);
         this.enableBackground = bool(var1, "enableBackground", this.enableBackground);
         this.fillDirection = enumVal(var1, "fillDirection", this.fillDirection, FillDirection.class);
         this.backgroundWidth = integer(var1, "backgroundWidth", this.backgroundWidth);
         this.backgroundHeight = integer(var1, "backgroundHeight", this.backgroundHeight);
         this.barWidth = integer(var1, "barWidth", this.barWidth);
         this.barHeight = integer(var1, "barHeight", this.barHeight);
         this.overlayWidth = integer(var1, "overlayWidth", this.overlayWidth);
         this.overlayHeight = integer(var1, "overlayHeight", this.overlayHeight);
         this.overlayX = integer(var1, "overlayXOffset", this.overlayX);
         this.overlayY = integer(var1, "overlayYOffset", this.overlayY);
         this.barX = integer(var1, "barXOffset", this.barX);
         this.barY = integer(var1, "barYOffset", this.barY);
         this.totalX = integer(var1, "totalXOffset", integer(var1, "xOffset", this.totalX));
         this.totalY = integer(var1, "totalYOffset", integer(var1, "yOffset", this.totalY));
         this.backgroundX = integer(var1, "backgroundXOffset", this.backgroundX);
         this.backgroundY = integer(var1, "backgroundYOffset", this.backgroundY);
         this.textX = integer(var1, "textXOffset", this.textX);
         this.textY = integer(var1, "textYOffset", this.textY);
         this.textColor = integer(var1, "textColor", this.textColor);
         this.textOpacity = integer(var1, "textOpacity", this.textOpacity);
         this.textSize = floating(var1, "textSize", this.textSize);
      }

      void write(Properties var1) {
         put(var1, "configVersion", 4);
         put(var1, "enabled", this.enabled);
         put(var1, "disableHeldItemTooltip", this.disableHeldItemTooltip);
         put(var1, "drainRightToLeft", this.drainRightToLeft);
         put(var1, "anchor", this.anchor.name());
         put(var1, "fadeWhenFull", this.fadeWhenFull);
         put(var1, "showText", this.showText.name());
         put(var1, "textAlign", this.textAlign.name());
         put(var1, "enableForeground", this.enableForeground);
         put(var1, "enableBackground", this.enableBackground);
         put(var1, "fillDirection", this.fillDirection.name());
         put(var1, "backgroundWidth", this.backgroundWidth);
         put(var1, "backgroundHeight", this.backgroundHeight);
         put(var1, "barWidth", this.barWidth);
         put(var1, "barHeight", this.barHeight);
         put(var1, "overlayWidth", this.overlayWidth);
         put(var1, "overlayHeight", this.overlayHeight);
         put(var1, "overlayXOffset", this.overlayX);
         put(var1, "overlayYOffset", this.overlayY);
         put(var1, "barXOffset", this.barX);
         put(var1, "barYOffset", this.barY);
         put(var1, "totalXOffset", this.totalX);
         put(var1, "totalYOffset", this.totalY);
         put(var1, "backgroundXOffset", this.backgroundX);
         put(var1, "backgroundYOffset", this.backgroundY);
         put(var1, "textXOffset", this.textX);
         put(var1, "textYOffset", this.textY);
         put(var1, "textColor", this.textColor);
         put(var1, "textOpacity", this.textOpacity);
         put(var1, "textSize", this.textSize);
      }

      private static void put(Properties var0, String var1, Object var2) {
         var0.setProperty(var1, String.valueOf(var2));
      }

      private static boolean bool(Properties var0, String var1, boolean var2) {
         try {
            return Boolean.parseBoolean(var0.getProperty(var1, String.valueOf(var2)));
         } catch (Exception var4) {
            return var2;
         }
      }

      private static int integer(Properties var0, String var1, int var2) {
         try {
            return Integer.parseInt(var0.getProperty(var1, String.valueOf(var2)));
         } catch (Exception var4) {
            return var2;
         }
      }

      private static float floating(Properties var0, String var1, float var2) {
         try {
            return Float.parseFloat(var0.getProperty(var1, String.valueOf(var2)));
         } catch (Exception var4) {
            return var2;
         }
      }

      private static <E extends Enum<E>> E enumVal(Properties var0, String var1, E var2, Class<E> var3) {
         try {
            return Enum.valueOf(var3, var0.getProperty(var1, var2.name()));
         } catch (Exception var5) {
            return (E)var2;
         }
      }
   }
}
