package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractClientPlayer.class, priority = 2000) // Diğer modların üstüne yazar
public abstract class AbstractClientPlayerMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void injectLockOnZoom(CallbackInfoReturnable<Float> cir) {
        // Sadece zoom aktifse VEYA değer henüz hedefe oturmamışsa (giriş anı için)
        if (CameraStateManager.isZoomActive() || CameraStateManager.zoomLerp < 0.99F) {
            float fov = cir.getReturnValue();
            float safeZoom = Math.max(0.85f, Math.min(1.0f, CameraStateManager.zoomLerp));
            cir.setReturnValue(fov * safeZoom);
        }
    }
}