package com.lockon.brs.camera;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PlayerTransparencyController {

    private static final SmoothDamp.State1D alphaState = new SmoothDamp.State1D();

    private static final double FADE_START_DISTANCE = 1.6;   // blok
    private static final double FULLY_FADED_DISTANCE = 0.6;  // blok

    private static final float MIN_ALPHA = 0.15f;

    // Alpha geçişinin yumuşama süresi (saniye) — spring-damp smoothTime.
    private static final float FADE_SMOOTH_TIME = 0.12f;

    public static void update(Vec3 cameraPos, Vec3 playerAnchorPos, float frameDeltaTicks) {
        if (frameDeltaTicks <= 0f) return;

        double dist = cameraPos.distanceTo(playerAnchorPos);
        float targetAlpha;
        if (dist >= FADE_START_DISTANCE) {
            targetAlpha = 1.0f;
        } else if (dist <= FULLY_FADED_DISTANCE) {
            targetAlpha = MIN_ALPHA;
        } else {
            double t = (dist - FULLY_FADED_DISTANCE) / (FADE_START_DISTANCE - FULLY_FADED_DISTANCE);
            targetAlpha = (float) Mth.lerp(t, MIN_ALPHA, 1.0);
        }

        float deltaSeconds = frameDeltaTicks / 20.0f;
        SmoothDamp.smoothDamp(alphaState, targetAlpha, FADE_SMOOTH_TIME, Float.MAX_VALUE, deltaSeconds);
    }

    public static float getAlpha() {
        return alphaState.initialized ? alphaState.value : 1.0f;
    }

    public static void reset() {
        alphaState.initialized = false;
    }
}