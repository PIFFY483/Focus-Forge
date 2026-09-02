package com.lockon.client;

import com.lockon.config.CameraViewConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = "lockon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrosshairHandler {

    private static float currentVisualOffset = 0f;
    private static float targetOffset = 0f;

    public static float getCurrentVisualOffset() {
        return currentVisualOffset;
    }

    @SubscribeEvent
    public static void onRenderCrosshair(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int mode = CameraViewConfig.CROSSHAIR_MODE.get();
        if (mode == 0) return;
        if (mode == 1 && mc.options.getCameraType().isFirstPerson()) return;

        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            event.setCanceled(true);
            updateTargetOffset(mc);
            drawPremiumCrosshair(event.getGuiGraphics(), currentVisualOffset);
        }
    }

    private static void updateTargetOffset(Minecraft mc) {
        boolean hasThrowable = isThrowable(mc.player.getMainHandItem().getItem()) ||
                isThrowable(mc.player.getOffhandItem().getItem());

        if (!mc.options.getCameraType().isFirstPerson() && hasThrowable) {
            targetOffset = -22.0f;
        } else {
            targetOffset = 0f;
        }
        currentVisualOffset = Mth.lerp(0.15f, currentVisualOffset, targetOffset);
    }

    private static boolean isThrowable(net.minecraft.world.item.Item item) {
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem;
    }

    /**
     * Oyuncu şu an ana elinde veya boş elinde nişan alınabilen (yay/arbalet/mızrak
     * gibi) bir silah tutuyor mu? Parallax assist crosshair kayması (bkz.
     * updateTargetOffset) ile New Camera'nın kamera-kaydırması (bkz.
     * VirtualCameraHandler#calculateShoulderPosition) aynı koşulu paylaşsın diye
     * public'e açıldı.
     */
    public static boolean isAimableHeld(LivingEntity player) {
        return isThrowable(player.getMainHandItem().getItem()) ||
                isThrowable(player.getOffhandItem().getItem());
    }

    private static void drawPremiumCrosshair(GuiGraphics graphics, float offsetX) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = (int) (mc.getWindow().getGuiScaledWidth() / 2 + offsetX);
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;

        float pull = 0f;
        if (mc.player.isUsingItem()) {
            pull = Math.min(mc.player.getTicksUsingItem() / 20.0f, 1.0f);
        }

        boolean isLookingAtMob = mc.crosshairPickEntity instanceof LivingEntity;
        RenderSystem.enableBlend();

        int colorIdx = CameraViewConfig.CROSSHAIR_COLOR_INDEX.get();
        int selectedColor = switch (colorIdx) {
            case 1 -> 0xFF00FF00;
            case 2 -> 0xFF0022FF;
            case 3 -> 0xFFFF0000;
            case 4 -> 0xFFFFFFFF;
            default -> 0xFF00FFFF;
        };

        int baseColor = isLookingAtMob ? 0xFFFF0000 : selectedColor;
        if (pull >= 1.0f) baseColor = 0xFFFFFFFF;
        int alpha = (int) (80 + (pull * 190));
        int finalColor = (alpha << 24) | (baseColor & 0x00FFFFFF);

        float gap = 8.0f * (1.0f - pull);
        int thick = pull > 0.8f ? 2 : 1;

        int style = CameraViewConfig.CROSSHAIR_STYLE.get();

        switch (style) {
            case 0 -> { // KÜÇÜK ARROW
                float smallGap = 5.0f * (1.0f - pull); // Gap 8'den 5'e düştü
                graphics.fill(centerX - (int)smallGap - 2, centerY, centerX - (int)smallGap, centerY + 1, finalColor); // Sol parça
                graphics.fill(centerX + (int)smallGap + 1, centerY, centerX + (int)smallGap + 3, centerY + 1, finalColor); // Sağ parça
            }
            case 1 -> { // KÜÇÜK CLASSIC
                int g = (int)gap + 1;
                graphics.fill(centerX, centerY - g - 2, centerX + 1, centerY - g, finalColor); // Üst
                graphics.fill(centerX, centerY + g, centerX + 1, centerY + g + 2, finalColor); // Alt
                graphics.fill(centerX - g - 2, centerY, centerX - g, centerY + 1, finalColor); // Sol
                graphics.fill(centerX + g, centerY, centerX + g + 2, centerY + 1, finalColor); // Sağ
            }
            case 2 -> { // DYNAMIC DOT (MINIMAL)
                if (pull < 1.0f) {
                    // Yay gerilirken nokta 1 pikselden 2 piksele çıkar (3 piksel çok büyüktü)
                    int size = pull > 0.6f ? 2 : 1;
                    int off = size / 2;
                    graphics.fill(centerX - off, centerY - off, centerX - off + size, centerY - off + size, finalColor);
                }

            }
            case 3 -> { // KÜÇÜK CIRCLE
                float radius = 3.5f * (1.0f - pull) + 1.5f; // Radius 5'ten 3.5'e düştü
                for (int i = 0; i < 360; i += 60) { // Daha az nokta (6 nokta) daha temiz durur
                    double angle = Math.toRadians(i);
                    int px = (int) (centerX + Math.cos(angle) * radius);
                    int py = (int) (centerY + Math.sin(angle) * radius);
                    graphics.fill(px, py, px + 1, py + 1, finalColor);
                }
            }
            case 4 -> { // KÜÇÜK T-SHAPE
                int g = (int)gap + 1;
                graphics.fill(centerX, centerY + g, centerX + 1, centerY + g + 3, finalColor);
                graphics.fill(centerX - g - 3, centerY, centerX - g, centerY + 1, finalColor);
                graphics.fill(centerX + g, centerY, centerX + g + 3, centerY + 1, finalColor);
            }

            case 5 -> { // DOUBLE RING (Odaklanan Halkalar)
                // Birinci halka (Dış): Yay gerildikçe 7'den 3'e düşer
                float r1 = 4.0f * (1.0f - pull) + 3.0f;
                // İkinci halka (İç): Yay gerildikçe 4'ten 2'ye düşer
                float r2 = 2.0f * (1.0f - pull) + 2.0f;

                drawCircle(graphics, centerX, centerY, r1, finalColor);
                drawCircle(graphics, centerX, centerY, r2, finalColor);
            }

            case 6 -> {
                int g = (int)gap + 2;
                int s = 2;

                graphics.fill(centerX - g, centerY - g, centerX - g + s + 1, centerY - g + 1, finalColor); // Sağa uzanan kol
                graphics.fill(centerX - g, centerY - g, centerX - g + 1, centerY - g + s + 1, finalColor); // Aşağı uzanan kol

                graphics.fill(centerX + g - s, centerY - g, centerX + g + 1, centerY - g + 1, finalColor); // Sola uzanan kol
                graphics.fill(centerX + g, centerY - g, centerX + g + 1, centerY - g + s + 1, finalColor); // Aşağı uzanan kol

                graphics.fill(centerX - g, centerY + g, centerX - g + s + 1, centerY + g + 1, finalColor); // Sağa uzanan kol
                graphics.fill(centerX - g, centerY + g - s, centerX - g + 1, centerY + g + 1, finalColor); // Yukarı uzanan kol

                graphics.fill(centerX + g - s, centerY + g, centerX + g + 1, centerY + g + 1, finalColor); // Sola uzanan kol
                graphics.fill(centerX + g, centerY + g - s, centerX + g + 1, centerY + g + 1, finalColor); // Yukarı uzanan kol
            }
        }

        // Ortadaki küçük rehber nokta (Dot stilinde gizli)
        if (pull >= 1.0f) {
            // 1. Dış Katman (3x3 Çerçeve)
            // Bu, merkezdeki noktaya hafif bir parlama (glow) efekti verir.
            graphics.renderOutline(centerX - 1, centerY - 1, 3, 3, 0xAAFFFFFF);

            // 2. Çekirdek (1x1 Nokta): Tam merkezde parlayan bembeyaz asıl nokta.
            // En son çiz herşeyin üstünde parlar
            graphics.fill(centerX, centerY, centerX + 1, centerY + 1, 0xFFFFFFFF);
        } else if (style != 2) {
            // Yay gerilmemişken duran rehber nokta

            int guideAlpha = 120;
            graphics.fill(centerX, centerY, centerX + 1, centerY + 1, (guideAlpha << 24) | (baseColor & 0x00FFFFFF));
        }

        RenderSystem.disableBlend();
    }
    private static void drawCircle(GuiGraphics graphics, int cx, int cy, float radius, int color) {
        for (int i = 0; i < 360; i += 45) {
            double angle = Math.toRadians(i);
            int px = (int) (cx + Math.cos(angle) * radius);
            int py = (int) (cy + Math.sin(angle) * radius);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }
}