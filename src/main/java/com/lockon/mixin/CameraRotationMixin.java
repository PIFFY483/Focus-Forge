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
        if (CameraStateManager.lockedTarget != null && CameraStateManager.cameraMode == 1) {
            Minecraft mc = Minecraft.getInstance();

            // mc.getFrameTime() bazen hatalı dönebilir.
            // Mixin'in kendi içindeki parametreyi değil, render anındaki gerçek zamanı gönderir.
            float partialTick = mc.getFrameTime();

            // Kamerayı güncelle
            CameraStateManager.updateCameraFPS(mc, partialTick);

            // Değerleri zorla yaz
            this.yRot = CameraStateManager.finalYaw;
            this.xRot = CameraStateManager.finalPitch;

            ci.cancel(); // Minecraft'ın kendi titrek hesaplamasını iptal et
        }
    }
}