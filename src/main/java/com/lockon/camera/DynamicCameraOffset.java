package com.lockon.camera;

import com.lockon.config.CameraViewConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class DynamicCameraOffset {
    public static float currentX = 0, currentY = 0, currentZ = 0;
    private static CameraType lastType = CameraType.FIRST_PERSON;
    public static boolean isSmoothing = false; // Senin bahsettiğin "BLOK"

    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        CameraType currentType = mc.options.getCameraType();

        // MOD DEĞİŞİMİNİ YAKALA (Bloku Koy)
        if (currentType != lastType) {
            isSmoothing = true;
            // Işınlanmayı engellemek için koordinatları anında sıfıra çakıyoruz
            currentX = 0; currentY = 0; currentZ = 0;
            lastType = currentType;
        }

        boolean isTPV = !currentType.isFirstPerson() && CameraViewConfig.ENABLE_SHOULDER_CAM.get();
        float targetX = isTPV ? CameraViewConfig.SHOULDER_OFFSET_X.get().floatValue() : 0.0f;
        float targetY = isTPV ? CameraViewConfig.SHOULDER_OFFSET_Y.get().floatValue() : 0.0f;
        float targetZ = isTPV ? CameraViewConfig.CAMERA_DISTANCE.get().floatValue() : 0.0f;

        float lerp = CameraViewConfig.CAMERA_SMOOTHNESS.get().floatValue();

        currentX = Mth.lerp(lerp, currentX, targetX);
        currentY = Mth.lerp(lerp, currentY, targetY);
        currentZ = Mth.lerp(lerp, currentZ, targetZ);

        // Hedefe çok yaklaştıysak bloğu kaldırabilirsin (Opsiyonel)
        if (Math.abs(currentX - targetX) < 0.01) isSmoothing = false;
    }
}
