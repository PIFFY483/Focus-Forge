package com.lockon.camera;

import net.minecraft.util.Mth;

public class CameraPivotState {

    public static float yaw;
    public static float pitch;
    public static boolean initialized = false;

    // sadece lock-on başladığında çağrılacak
    public static void init(float startYaw, float startPitch) {
        yaw = startYaw;
        pitch = startPitch;
        initialized = true;
    }

    public static void update(float targetYaw, float targetPitch, float smooth) {
        if (!initialized) return;

        yaw = Mth.lerp(smooth, yaw, targetYaw);
        pitch = Mth.lerp(smooth, pitch, targetPitch);
    }

    public static void reset() {
        initialized = false;
    }
}
