package com.lockon.brs.camera;

import com.lockon.brs.config.CameraConfig;
import com.lockon.brs.config.LockOnConfig;
import com.lockon.config.CameraViewConfig;
import com.lockon.brs.lock.LockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CameraStateManager {

    public static Entity lockedTarget = null;
    public static float finalYaw = 0.0F;
    public static float finalPitch = 0.0F;
    private static int unlockTimer = 0;

    private static final SmoothDamp.State1D lockDistanceState = new SmoothDamp.State1D();

    private static final SmoothDamp.State1D cameraYawState = new SmoothDamp.State1D();
    private static final SmoothDamp.State1D cameraPitchState = new SmoothDamp.State1D();

    public static float getCameraYaw() {
        return cameraYawState.initialized ? cameraYawState.value : finalYaw;
    }

    public static float getCameraPitch() {
        return cameraPitchState.initialized ? cameraPitchState.value : finalPitch;
    }

    public static void onLockStart(Minecraft mc, Entity target) {
        lockedTarget = target;
        if (mc.player != null) {
            finalYaw = mc.player.getYRot();
            finalPitch = mc.player.getXRot();
        }
    }

    public static void onUnlock(Minecraft mc) {
        if (mc.player != null) {
            float currentYaw = finalYaw;
            float currentPitch = finalPitch;

            mc.player.setYRot(currentYaw);
            mc.player.setXRot(currentPitch);
            mc.player.yRotO = currentYaw;
            mc.player.xRotO = currentPitch;

            mc.mouseHandler.releaseMouse();
            mc.mouseHandler.grabMouse();

            mc.player.input.leftImpulse = 0;
            mc.player.input.forwardImpulse = 0;
        }
        lockedTarget = null;
        unlockTimer = 0;
        lockDistanceState.initialized = false;
        cameraYawState.initialized = false;
        cameraPitchState.initialized = false;
    }

    public static void updateCameraFPS(Minecraft mc, float partialTick, float frameDeltaTicks) {
        if (mc.player == null) return;
        if (frameDeltaTicks <= 0f) return;

        Entity currentTarget = LockState.getTarget();
        if (currentTarget == null && lockedTarget != null && unlockTimer == 0) {
            onUnlock(mc);
            return;
        }

        if (currentTarget != null) {
            lockedTarget = currentTarget;
            unlockTimer = 5;
        } else {
            if (unlockTimer > 0) {
                unlockTimer--;
            } else {
                lockedTarget = null;
            }
        }

        float targetYaw, targetPitch;
        if (lockedTarget != null && lockedTarget.isAlive()) {
            // Hedef pozisyonunu hesapla (partialTick ile smooth)
            double targetX = Mth.lerp(partialTick, lockedTarget.xo, lockedTarget.getX());
            double targetZ = Mth.lerp(partialTick, lockedTarget.zo, lockedTarget.getZ());
            double baseTargetY = Mth.lerp(partialTick, lockedTarget.yo, lockedTarget.getY());
            double finalTargetY;
            float entityHeight = lockedTarget.getBbHeight();

            if (entityHeight > 2.0f) {
                finalTargetY = baseTargetY + (entityHeight * CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD.get());
            } else {
                finalTargetY = baseTargetY + lockedTarget.getEyeHeight();
            }
            finalTargetY += LockOnConfig.CAMERA_FOCUS_OFFSET.get();

            // Delta hesapla
            double dx = targetX - mc.player.getX();
            double dy = finalTargetY - (mc.player.getY() + mc.player.getEyeHeight());
            double dz = targetZ - mc.player.getZ();

            // Hedef yaw ve pitch
            targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
            targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            targetPitch = Mth.clamp(targetPitch, -40.0F, 40.0F);

            float smoothFactor = LockOnConfig.LOCK_SPEED.get().floatValue();

            // Mesafe bazlı hız ayarı
            float distance = mc.player.distanceTo(lockedTarget);
            if (distance < 5.0f) {
                float distanceFactor = Mth.clamp(distance / 5.0f, 0.5f, 1.0f);
                smoothFactor *= distanceFactor;
            }

            // Hedef ile mevcut açı arasındaki farkı bul
            float yawDiff = Mth.wrapDegrees(targetYaw - finalYaw);
            float pitchDiff = targetPitch - finalPitch;

            float maxStep = Mth.lerp(Mth.clamp(smoothFactor, 0.0f, 1.0f), 6.0f, 45.0f) * frameDeltaTicks;

            float stepYaw = Mth.clamp(yawDiff * smoothFactor * frameDeltaTicks, -maxStep, maxStep);
            float stepPitch = Mth.clamp(pitchDiff * smoothFactor * frameDeltaTicks, -maxStep, maxStep);

            // Sınırlandırılmış adımı mevcut açıya ekle
            finalYaw += stepYaw;
            finalPitch += stepPitch;

        } else {
            // Kilit yokken normal oyuncu rotasyonunu takip et
            targetYaw = mc.player.getYRot();
            targetPitch = mc.player.getXRot();
            float currentLerp = LockOnConfig.MAX_SMOOTHING_FACTOR.get().floatValue();

            float frameLerp = 1.0f - (float) Math.pow(1.0 - Mth.clamp(currentLerp, 0.0001f, 0.9999f), frameDeltaTicks);

            finalYaw = Mth.rotLerp(frameLerp, finalYaw, targetYaw);
            finalPitch = Mth.lerp(frameLerp, finalPitch, targetPitch);
        }

        // Oyuncunun rotasyonunu zorla
        if (lockedTarget != null || unlockTimer > 0) {
            finalYaw = Mth.wrapDegrees(finalYaw);
            applyAbsoluteForce(mc.player, finalYaw, finalPitch);
        }

        if (lockedTarget != null && lockedTarget.isAlive()) {
            float camSpeed = Mth.clamp(CameraConfig.CAMERA_FOLLOW_SPEED.get().floatValue(), 0.01f, 1.0f);
            float smoothTime = 0.03f + 0.4f * (1.0f - camSpeed);
            float deltaSeconds = frameDeltaTicks / 20.0f;

            SmoothDamp.smoothDampAngle(cameraYawState, finalYaw, smoothTime, Float.MAX_VALUE, deltaSeconds);
            SmoothDamp.smoothDamp(cameraPitchState, finalPitch, smoothTime, Float.MAX_VALUE, deltaSeconds);
        } else {
            cameraYawState.initialized = false;
            cameraPitchState.initialized = false;
        }
    }

    private static void applyAbsoluteForce(net.minecraft.client.player.LocalPlayer player, float yaw, float pitch) {
        // SADECE KAMERA AÇISI
        player.setYRot(yaw);
        player.setXRot(pitch);

        // ÖNCEKİ KARE
        player.yRotO = yaw;
        player.xRotO = pitch;

        // SADECE KAFA
        player.setYHeadRot(yaw);
        player.yHeadRotO = yaw;

        // ÇİFTE KİLİT
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    public static double computeAndSmoothLockDistance(Minecraft mc, double baseDistance, float frameDeltaTicks) {
        if (!LockOnConfig.ENABLE_DYNAMIC_LOCK_DISTANCE.get()
                || lockedTarget == null
                || !lockedTarget.isAlive()) {
            lockDistanceState.initialized = false;
            return baseDistance;
        }

        // Dikey FOV (derece)
        double fovDeg = mc.options.fov().get();
        double halfFovRad = Math.toRadians(fovDeg / 2.0);

        double targetHeight = lockedTarget.getBbHeight();
        double margin = LockOnConfig.FRAME_MARGIN.get();

        double playerToTargetDistance = mc.player.distanceTo(lockedTarget);

        double requiredCameraToTargetDistanceV = targetHeight / (2.0 * margin * Math.tan(halfFovRad));

        double targetWidth = lockedTarget.getBbWidth();
        double aspectRatio = (double) mc.getWindow().getWidth() / (double) Math.max(1, mc.getWindow().getHeight());
        double halfHorizontalFovRad = Math.atan(Math.tan(halfFovRad) * aspectRatio);
        double requiredCameraToTargetDistanceH = targetWidth / (2.0 * margin * Math.tan(halfHorizontalFovRad));

        double requiredCameraToTargetDistance = Math.max(
                requiredCameraToTargetDistanceV, requiredCameraToTargetDistanceH);

        double requiredBack = requiredCameraToTargetDistance - playerToTargetDistance;

        double desired = Math.max(baseDistance, requiredBack);
        desired = Mth.clamp(desired,
                LockOnConfig.MIN_LOCK_CAMERA_DISTANCE.get(),
                LockOnConfig.MAX_LOCK_CAMERA_DISTANCE.get());


        float speed = Mth.clamp(LockOnConfig.LOCK_DISTANCE_SMOOTH_SPEED.get().floatValue(), 0.01f, 1.0f);
        float smoothTime = 0.05f + 0.5f * (1.0f - speed);
        float maxSpeedPerSecond = LockOnConfig.LOCK_DISTANCE_MAX_STEP_PER_TICK.get().floatValue() * 20.0f;
        float deltaSeconds = frameDeltaTicks / 20.0f;

        return SmoothDamp.smoothDamp(lockDistanceState, (float) desired, smoothTime, maxSpeedPerSecond, deltaSeconds);
    }
}