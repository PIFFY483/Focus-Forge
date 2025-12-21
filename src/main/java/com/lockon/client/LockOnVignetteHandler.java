package com.lockon.client;

import com.lockon.camera.CameraStateManager;
import com.lockon.config.CameraViewConfig; // Config bağlantısı eklendi
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LockOnVignetteHandler {
    private static float vignetteOpacity = 0.0f;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 1. ADIM: Config kontrolü - Eğer vinyet kapalıysa çizimi durdur ve opaklığı sıfırlar
        if (!CameraViewConfig.CLIENT.enableVignette.get()) {
            vignetteOpacity = 0.0f;
            return;
        }

        // 2. ADIM: Kilit durumunu kontrol et
        boolean isActive = CameraStateManager.lockedTarget != null;

        // Geçiş hızını ipeksi bir süzülme için koruyoruz
        if (isActive) {
            if (vignetteOpacity < 1.0f) vignetteOpacity += 0.04f;
        } else {
            if (vignetteOpacity > 0.0f) vignetteOpacity -= 0.04f;
        }

        if (vignetteOpacity <= 0.0f) return;

        renderSurgicalVignette(event, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    private static void renderSurgicalVignette(RenderGuiEvent.Post event, int width, int height) {
        // 40 KATMAN - SADECE 5-6 PİKSEL İÇİNE PRESLENDİ
        int layers = 40;
        float maxThickness = 5.5f;
        float step = maxThickness / layers;

        for (int i = 0; i < layers; i++) {
            float offset = i * step;

            // Logaritmik Azalma: Dış en koyu, içe doğru  şeffaflaşır
            float ratio = (float) i / layers;
            int alpha = (int) (vignetteOpacity * 90 * Math.pow(1.0f - ratio, 1.8));

            if (alpha <= 1) break;

            float curve = offset * 1.5f;

            drawSurgicalFrame(event, width, height, offset, alpha, curve);
        }
    }

    private static void drawSurgicalFrame(RenderGuiEvent.Post event, int width, int height, float offset, int alpha, float curve) {
        int color = (alpha << 24);

        // Kenarlar ve Köşe Birleşimleri
        event.getGuiGraphics().fill((int)curve, (int)offset, (int)(width - curve), (int)(offset + 1), color); // Üst
        event.getGuiGraphics().fill((int)curve, (int)(height - offset - 1), (int)(width - curve), (int)(height - offset), color); // Alt
        event.getGuiGraphics().fill((int)offset, (int)curve, (int)(offset + 1), (int)(height - curve), color); // Sol
        event.getGuiGraphics().fill((int)(width - offset - 1), (int)curve, (int)(width - offset), (int)(height - curve), color); // Sağ

        event.getGuiGraphics().fill((int)offset, (int)offset, (int)curve + 1, (int)curve + 1, color);
        event.getGuiGraphics().fill((int)(width - curve - 1), (int)offset, (int)(width - offset), (int)curve + 1, color);
        event.getGuiGraphics().fill((int)offset, (int)(height - curve - 1), (int)curve + 1, (int)(height - offset), color);
        event.getGuiGraphics().fill((int)(width - curve - 1), (int)(height - curve - 1), (int)(width - offset), (int)(height - offset), color);
    }
}