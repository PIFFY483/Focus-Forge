package com.lockon.brs.camera;

import com.lockon.brs.config.CameraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class CameraRig {

    // Config'den okunan cache'lenmiş değerler
    public static boolean enableShoulderCam = true;
    public static double shoulderOffset = 0.5;
    public static double heightOffset = 0.3;
    public static double cameraDistance = 4.0;
    public static double rotationSmoothness = 0.15;
    public static boolean enableOrbitCamera = true;
    public static double orbitDistance = 5.0;
    public static double orbitHeightOffset = 1.0;
    public static double orbitTransitionSpeed = 0.06;
    public static double orbitAutoRotateSpeed = 0.0;
    public static double transitionSpeed = 0.15;

    // Smooth yaw/pitch
    private static float smoothYaw = 0.0f;
    private static float smoothPitch = 0.0f;
    private static boolean smoothInitialized = false;
    public static boolean enableSmoothTransition = true;

    /**
     * Config değerlerini oku ve cache'le.
     */
    public static void loadConfig() {
        try {
            enableShoulderCam   = CameraConfig.ENABLE_SHOULDER_CAM.get();
            shoulderOffset      = CameraConfig.SHOULDER_OFFSET.get();
            heightOffset        = CameraConfig.HEIGHT_OFFSET.get();
            cameraDistance      = CameraConfig.CAMERA_DISTANCE.get();
            rotationSmoothness  = CameraConfig.CAMERA_SMOOTHNESS.get();
            enableOrbitCamera     = CameraConfig.ENABLE_ORBIT_CAMERA.get();
            orbitDistance         = CameraConfig.ORBIT_DISTANCE.get();
            orbitHeightOffset     = CameraConfig.ORBIT_HEIGHT_OFFSET.get();
            orbitTransitionSpeed  = CameraConfig.ORBIT_TRANSITION_SPEED.get();
            orbitAutoRotateSpeed  = CameraConfig.ORBIT_AUTO_ROTATE_SPEED.get();
            transitionSpeed = CameraConfig.TRANSITION_SPEED.get();
            enableSmoothTransition = CameraConfig.ENABLE_SMOOTH_TRANSITION.get();
        } catch (Exception e) {
            enableShoulderCam   = true;
            shoulderOffset      = 0.5;
            heightOffset        = 0.3;
            cameraDistance      = 4.0;
            rotationSmoothness  = 0.15;
        }
    }

    public static boolean isShoulderCamActive() {
        if (!enableShoulderCam) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (mc.options.getCameraType().isFirstPerson()) return false;
        return true;
    }

    /**
     * Her client tick'te çağrılır.
     * Smooth yaw/pitch hesaplar.
     */
    public static void update(Minecraft mc, float partialTick) {
        if (mc.player == null) return;

        float targetYaw = mc.player.getYRot();
        float targetPitch = mc.player.getXRot();

        if (!smoothInitialized) {
            smoothYaw = targetYaw;
            smoothPitch = targetPitch;
            smoothInitialized = true;
            return;
        }

        float smooth = (float) rotationSmoothness;
        smoothYaw += Mth.wrapDegrees(targetYaw - smoothYaw) * smooth;
        smoothPitch += (targetPitch - smoothPitch) * smooth;
    }

    public static float getSmoothYaw() {
        return smoothYaw;
    }

    public static float getSmoothPitch() {
        return smoothPitch;
    }

    public static void resetSmoothing() {
        smoothInitialized = false;
    }
}