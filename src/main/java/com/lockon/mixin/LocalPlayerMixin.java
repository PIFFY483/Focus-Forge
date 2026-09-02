package com.lockon.mixin;

import com.lockon.camera.CameraStateManager;
import com.lockon.camera.ShoulderCamMode;
import com.lockon.config.CameraViewConfig;
import com.lockon.brs.camera.CameraRig;
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

        if (mc.options.getCameraType().isFirstPerson()) return;

        if (ShoulderCamMode.isOld()) {
            // --- OLD shoulder cam (Focus Forge) ---
            if (CameraViewConfig.ENABLE_SHOULDER_CAM.get()) {
                if ((player.xxa != 0 || player.zza != 0) && CameraStateManager.cameraMode == 1) {
                    float cameraYaw = mc.gameRenderer.getMainCamera().getYRot();
                    float currentYaw = player.getYRot();
                    float delta = Mth.wrapDegrees(cameraYaw - currentYaw);

                    player.setYRot(currentYaw + delta * 0.2f);
                    player.yRotO = player.getYRot();
                }
            }
        } else {
            // --- NEW shoulder cam (eski BRS) ---
            // NOT: Bu hook orijinal BRS projesinde de boştu (henüz implemente edilmemişti).
            // CameraRig.isShoulderCamActive() true olduğunda buraya gerçek "smart movement"
            // mantığı eklenmemiş - şimdilik bu bir yer tutucu.
            if (CameraRig.isShoulderCamActive()) {
                // TODO: BRS'in kendi smart movement mantığı eklenecek.
            }
        }
    }
}
