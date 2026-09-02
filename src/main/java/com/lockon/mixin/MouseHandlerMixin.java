package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import com.lockon.camera.ShoulderCamMode;
import com.lockon.brs.camera.OrbitCameraState;
import com.lockon.brs.config.CameraConfig;
import com.lockon.brs.lock.LockState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    // --- OLD shoulder cam (Focus Forge): kilitliyken fare titremesini yut ---
    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void lockon$cancelMouseMovement(long window, double x, double y, CallbackInfo ci) {
        if (ShoulderCamMode.isOld()
                && CameraStateManager.lockedTarget != null
                && CameraStateManager.cameraMode == 1) {
            ci.cancel();
        }
    }

    // --- NEW shoulder cam (eski BRS): orbit kamera / lock-on fare kontrolü ---
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void brs$handleMouseInput(CallbackInfo ci) {
        if (!ShoulderCamMode.isNew()) return;

        if (OrbitCameraState.isOrbitActive()) {
            OrbitCameraState.applyMouseDelta(
                    this.accumulatedDX, this.accumulatedDY,
                    CameraConfig.ORBIT_SENSITIVITY.get());
            this.accumulatedDX = 0.0D;
            this.accumulatedDY = 0.0D;
            ci.cancel();
        } else if (LockState.isLocked()) {
            this.accumulatedDX = 0.0D;
            this.accumulatedDY = 0.0D;
            ci.cancel();
        }
    }
}
