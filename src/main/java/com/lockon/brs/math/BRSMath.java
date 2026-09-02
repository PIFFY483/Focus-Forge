package com.lockon.brs.math;

import net.minecraft.util.Mth;

public class BRSMath {
    // FPS bağımsız exponential smoothing (Delta time bazlı)
    public static float expSmooth(float current, float target, float speed, float deltaTime) {
        float alpha = 1.0f - (float) Math.exp(-speed * deltaTime);
        return current + (target - current) * alpha;
    }

    public static double expSmooth(double current, double target, double speed, double deltaTime) {
        double alpha = 1.0 - Math.exp(-speed * deltaTime);
        return current + (target - current) * alpha;
    }

    // Açısal smoothing (-180 ile 180 arası wrap ederek en kısa yolu bulur)
    public static float expSmoothAngle(float current, float target, float speed, float deltaTime) {
        float diff = Mth.wrapDegrees(target - current);
        float alpha = 1.0f - (float) Math.exp(-speed * deltaTime);
        return current + diff * alpha;
    }
}