package com.lockon.brs.camera;

import net.minecraft.util.Mth;

public class OrbitCameraState {

    public enum CameraMode {
        SHOULDER,
        ORBIT
    }

    private static CameraMode currentMode = CameraMode.SHOULDER;
    private static float transitionProgress = 0.0f; // 0 = omuz, 1 = orbit
    private static boolean transitioning = false;

    private static float orbitYaw = 0.0f;
    private static float orbitPitch = 15.0f;

    public static CameraMode getMode() {
        return currentMode;
    }

    public static boolean isOrbitActive() {
        return currentMode == CameraMode.ORBIT || transitionProgress > 0.001f;
    }

    public static boolean isFullyOrbit() {
        return currentMode == CameraMode.ORBIT && transitionProgress >= 0.999f;
    }

    public static float getTransitionProgress() {
        return transitionProgress;
    }

    public static float getEasedProgress() {
        float t = Mth.clamp(transitionProgress, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    public static float getOrbitYaw() {
        return orbitYaw;
    }

    public static float getOrbitPitch() {
        return orbitPitch;
    }

    public static void toggle() {
        currentMode = (currentMode == CameraMode.SHOULDER) ? CameraMode.ORBIT : CameraMode.SHOULDER;
        transitioning = true;
    }

    public static void setMode(CameraMode mode) {
        if (currentMode == mode) return;
        currentMode = mode;
        transitioning = true;
    }

    public static void requestExit() {
        currentMode = CameraMode.SHOULDER;
        transitioning = true;
    }

    public static void initOrbitFromPlayer(float playerYaw, float playerPitch) {
        orbitYaw = playerYaw;
        orbitPitch = Mth.clamp(-playerPitch * 0.5f + 10.0f, -60.0f, 75.0f);
    }

    public static void updateTransition(float frameDeltaTicks, float transitionSpeed) {
        if (!transitioning) return;

        float step = transitionSpeed * frameDeltaTicks;

        if (currentMode == CameraMode.ORBIT) {
            transitionProgress = Math.min(1.0f, transitionProgress + step);
        } else {
            transitionProgress = Math.max(0.0f, transitionProgress - step);
        }

        float target = (currentMode == CameraMode.ORBIT) ? 1.0f : 0.0f;
        if (Math.abs(transitionProgress - target) < 0.001f) {
            transitionProgress = target;
            transitioning = false;
        }
    }

    public static void applyMouseDelta(double dx, double dy, double sensitivity) {
        float factor = (float)(sensitivity * 0.15);
        orbitYaw = Mth.wrapDegrees(orbitYaw + (float)(dx * factor));
        orbitPitch = Mth.clamp(orbitPitch + (float)(dy * factor), -60.0f, 75.0f);
    }

    public static void updateAutoRotate(float frameDeltaTicks, float speed) {
        if (speed > 0.0f && isFullyOrbit()) {
            orbitYaw = Mth.wrapDegrees(orbitYaw + speed * frameDeltaTicks);
        }
    }

    public static void reset() {
        currentMode = CameraMode.SHOULDER;
        transitionProgress = 0.0f;
        transitioning = false;
        orbitYaw = 0.0f;
        orbitPitch = 15.0f;
    }
}