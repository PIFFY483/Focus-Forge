package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 10000)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void applyLockOnZoom(Camera camera, float partialTicks, boolean useSpecialFovModifier, CallbackInfoReturnable<Double> cir) {
        if (CameraStateManager.lockedTarget != null && CameraStateManager.cameraMode == 1) {
            double currentZoom = CameraStateManager.zoomLerp;
            cir.setReturnValue(cir.getReturnValue() * currentZoom);
        }
    }
}