package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void lockon$cancelMouseMovement(long window, double x, double y, CallbackInfo ci) {
        if (CameraStateManager.lockedTarget != null && CameraStateManager.cameraMode == 1) {
            // Fare hareketlerini burada 'yutuyoruz', böylece oyuncu fareyi sallasa da kamera titremez
            ci.cancel();
        }
    }
}