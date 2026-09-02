package com.lockon.mixin.brs;

import com.lockon.brs.camera.CameraRig;
import com.lockon.camera.ShoulderCamMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererBobMixin {
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void brs$disableBobbing(PoseStack poseStack, float partialTicks, CallbackInfo ci) {

        if (ShoulderCamMode.isNew() && CameraRig.isShoulderCamActive()) {
            ci.cancel();
        }
    }
}