package com.lockon.camera;

import com.lockon.config.CameraViewConfig;
import com.lockon.util.CameraMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = "lockon", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VirtualCameraHandler {

    private static Vec3 lerpPos = Vec3.ZERO;

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (ShoulderCamMode.isNew()) {
            lerpPos = Vec3.ZERO;
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (!CameraViewConfig.ENABLE_SHOULDER_CAM.get()) {
            lerpPos = Vec3.ZERO;
            return;
        }

        if (mc.player == null || mc.options.getCameraType().isFirstPerson() ||
                CameraStateManager.cameraMode != 1 || !CameraViewConfig.ENABLE_SHOULDER_CAM.get()) {

            lerpPos = Vec3.ZERO; // Pozisyonu sıfırla
            return;
        }

        Camera camera = event.getCamera();
        float pt = (float) event.getPartialTick();

        //  CONFIG DEN CANLI DEĞERLERİ ÇEKER
        // buradaki side, vert, back ve smoothness değerleri GUI'den ne seçersen o olacak
        double side = CameraViewConfig.SHOULDER_OFFSET_X.get();
        double vert = CameraViewConfig.SHOULDER_OFFSET_Y.get();
        double back = CameraViewConfig.CAMERA_DISTANCE.get();
        double lerpFactor = CameraViewConfig.CAMERA_SMOOTHNESS.get();

        // 3. AÇI HESAPLAMALARI
        float yaw = camera.getYRot();
        float pitch = camera.getXRot();
        float yawRad = yaw * (float)(Math.PI / 180.0);
        float pitchRad = pitch * (float)(Math.PI / 180.0);

        // OYUNCU POZİSYONU
        double px = Mth.lerp(pt, mc.player.xo, mc.player.getX());
        double py = Mth.lerp(pt, mc.player.yo, mc.player.getY()) + mc.player.getEyeHeight();
        double pz = Mth.lerp(pt, mc.player.zo, mc.player.getZ());
        Vec3 eyePos = new Vec3(px, py, pz);

        //İDEAL KONUM VE DUVAR KONTROLÜ (Collision)
        double offX = -Math.cos(yawRad) * side + Math.sin(yawRad) * Math.cos(pitchRad) * back;
        double offZ = -Math.sin(yawRad) * side - Math.cos(yawRad) * Math.cos(pitchRad) * back;
        double offY = vert + Math.sin(pitchRad) * back;
        Vec3 idealPos = eyePos.add(offX, offY, offZ);

        HitResult hit = mc.level.clip(new ClipContext(eyePos, idealPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player));
        Vec3 targetPos = hit.getType() == HitResult.Type.MISS ? idealPos : hit.getLocation().add(eyePos.subtract(idealPos).normalize().scale(0.15));

        // YUMUŞAK TAKİP
        if (lerpPos.equals(Vec3.ZERO)) lerpPos = targetPos;

        lerpPos = new Vec3(
                Mth.lerp(lerpFactor, lerpPos.x, targetPos.x),
                Mth.lerp(lerpFactor, lerpPos.y, targetPos.y),
                Mth.lerp(lerpFactor, lerpPos.z, targetPos.z)
        );

        // SONUÇLARI MIXIN İLE KAMERAYA GÖNDER
        if (camera instanceof CameraMixinInterface mixinCamera) {
            mixinCamera.lockon$setCustomPosition(lerpPos);
        }
    }
}