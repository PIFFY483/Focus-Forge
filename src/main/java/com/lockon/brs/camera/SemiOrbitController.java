package com.lockon.brs.camera;

import com.lockon.brs.lock.LockState;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class SemiOrbitController {

    private enum Phase {
        SHOULDER,   // yürüyorken / orbit kapalı
        ORBIT_IDLE, // durgun, tamamen orbit
        TURNING,    // harekete geçildi
        EXITING     // orbit -> shoulder çıkışı
    }

    private static boolean enabled = false;
    private static Phase phase = Phase.SHOULDER;

    private static float turnCurrentYaw = 0.0f;
    private static float turnTargetYaw = 0.0f;

    private static final float TURN_DEGREES_PER_SECOND = 420.0f;
    private static final float TURN_DONE_EPSILON = 0.5f;

    private SemiOrbitController() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {

        enabled = value;

        if (!value) {
            reset();
            return;
        }

        syncPhaseWithCamera();
    }

    public static void reset() {
        phase = Phase.SHOULDER;
    }

    private static void syncPhaseWithCamera() {

        if (OrbitCameraState.getMode()
                == OrbitCameraState.CameraMode.ORBIT) {

            phase = Phase.ORBIT_IDLE;

            return;
        }

        if (OrbitCameraState.isTransitioning()
                && OrbitCameraState.getTransitionProgress() > 0.001f) {

            phase = Phase.EXITING;

            return;
        }

        phase = Phase.SHOULDER;
    }

    private static boolean isMovementInputActive(Minecraft mc) {

        if (mc.options == null) {
            return false;
        }

        return mc.options.keyUp.isDown()
                || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown()
                || mc.options.keyRight.isDown();
    }

    public static void tick(
            Minecraft mc,
            float frameDeltaTicks
    ) {

        if (!enabled) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        if (LockState.isLocked()) {
            reset();
            return;
        }

        CameraType type = mc.options.getCameraType();

        if (type.isFirstPerson()) {

            if (OrbitCameraState.isOrbitActive()) {
                OrbitCameraState.requestExit();
            }

            reset();
            return;
        }

        boolean moving = isMovementInputActive(mc);

        switch (phase) {

            case SHOULDER -> {

                if (!OrbitCameraState.isFullyShoulder()) {
                    return;
                }

                if (!moving) {

                    OrbitCameraState.initOrbitFromPlayer(
                            mc.player.getYRot(),
                            mc.player.getXRot()
                    );

                    OrbitCameraState.setMode(
                            OrbitCameraState.CameraMode.ORBIT
                    );

                    phase = Phase.ORBIT_IDLE;
                }
            }

            case ORBIT_IDLE -> {

                if (!OrbitCameraState.isFullyOrbit()) {
                    return;
                }

                if (moving) {

                    turnCurrentYaw = mc.player.getYRot();
                    turnTargetYaw = OrbitCameraState.getOrbitYaw();

                    phase = Phase.TURNING;
                }
            }

            case TURNING -> {

                float diff =
                        Mth.wrapDegrees(
                                turnTargetYaw - turnCurrentYaw
                        );

                float maxStep =
                        (TURN_DEGREES_PER_SECOND / 20.0f)
                                * frameDeltaTicks;

                float step =
                        Mth.clamp(
                                diff,
                                -maxStep,
                                maxStep
                        );

                turnCurrentYaw =
                        Mth.wrapDegrees(
                                turnCurrentYaw + step
                        );

                forcePlayerYaw(
                        mc.player,
                        turnCurrentYaw
                );

                if (!moving) {

                    phase = Phase.ORBIT_IDLE;
                    return;
                }

                if (Math.abs(diff) <= TURN_DONE_EPSILON) {

                    OrbitCameraState.requestExit();

                    phase = Phase.EXITING;
                }
            }

            case EXITING -> {

                if (OrbitCameraState.isOrbitActive()) {
                    return;
                }

                if (moving) {

                    phase = Phase.SHOULDER;

                } else {

                    OrbitCameraState.initOrbitFromPlayer(
                            mc.player.getYRot(),
                            mc.player.getXRot()
                    );

                    OrbitCameraState.setMode(
                            OrbitCameraState.CameraMode.ORBIT
                    );

                    phase = Phase.ORBIT_IDLE;
                }
            }
        }
    }

    private static void forcePlayerYaw(
            LocalPlayer player,
            float yaw
    ) {
        player.setYRot(yaw);
        player.yRotO = yaw;

        player.setYHeadRot(yaw);
        player.yHeadRotO = yaw;
    }
}