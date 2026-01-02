package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraRotationMixin {

    @Shadow private float yRot;
    @Shadow private float xRot;

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void lockon$smoothRotation(float yaw, float pitch, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        // YENİ ŞART: Hedef olacak VE Omuz kamerası gerçekten aktif olacak
        if (CameraStateManager.lockedTarget != null && CameraStateManager.isShoulderCamActuallyActive(mc)) {
            float partialTick = mc.getFrameTime();
            CameraStateManager.updateCameraFPS(mc, partialTick);

            this.yRot = CameraStateManager.finalYaw;
            this.xRot = CameraStateManager.finalPitch;

            ci.cancel();
        }
    }
}