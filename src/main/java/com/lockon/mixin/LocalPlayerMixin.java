package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import com.lockon.config.CameraViewConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void smartMovement(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        // SİSTEMİ TETİKLEYEN ANA KOMUT

        if (CameraViewConfig.ENABLE_SHOULDER_CAM.get() && !mc.options.getCameraType().isFirstPerson()) {

            if ((player.xxa != 0 || player.zza != 0) && CameraStateManager.cameraMode == 1) {
                float cameraYaw = mc.gameRenderer.getMainCamera().getYRot();
                float currentYaw = player.getYRot();
                float delta = Mth.wrapDegrees(cameraYaw - currentYaw);

                player.setYRot(currentYaw + delta * 0.2f);
                player.yRotO = player.getYRot();
            }
        }
    }
}