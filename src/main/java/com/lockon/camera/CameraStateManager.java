package com.lockon.camera;

import com.lockon.config.CameraViewConfig;
import com.lockon.lock.LockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CameraStateManager {

    public static Entity lockedTarget = null;
    public static float finalYaw = 0.0F;
    public static float finalPitch = 0.0F;
    public static float zoomLerp = 1.0F;
    public static int cameraMode = 1;
    private static int unlockTimer = 0;
    private static int alignmentTicks = 0;

    public static float getSmoothness() {
        if (lockedTarget != null && cameraMode == 1) {
            return CameraViewConfig.CLIENT.lockOnSmoothness.get().floatValue();
        }
        return CameraViewConfig.CAMERA_SMOOTHNESS.get().floatValue();
    }

    public static void onLockStart(Minecraft mc, Entity target) {
        lockedTarget = target;
        alignmentTicks = 6; // Yaklaşık 0.3 saniye (20 tick = 1 sn ise 6 tick uygundur)
        if (mc.player != null) {
            finalYaw = mc.player.getYRot(); // Başlangıç açısını sabitle
            finalPitch = mc.player.getXRot();
            zoomLerp = 1.0F;
        }
    }

    public static boolean isZoomActive() {
        // Sinyal şartı: Bir hedef var MI ve kamera şu an kilit modunda MI?
        boolean configEnabled = CameraViewConfig.CLIENT.enableZoom.get();
        return lockedTarget != null && cameraMode == 1 && configEnabled;
    }


    public static void onUnlock(Minecraft mc) {
        if (mc.player != null) {
            float currentYaw = finalYaw;
            float currentPitch = finalPitch;

            // 1. ADIM: Rotasyonları eşitle
            mc.player.setYRot(currentYaw);
            mc.player.setXRot(currentPitch);
            mc.player.yRotO = currentYaw;
            mc.player.xRotO = currentPitch;

            // 2. ADIM: MOUSE SIFIRLAMA
            // Minecraft'ın farenin ne kadar döndüğünü hesapladığı o "birikmiş" veriyi temizliyoruz.
            // Bazı sürümlerde mouseHandler.accumulatedDX/DY değişkenlerine erişemeyebilirsin,
            // bu yüzden en garanti yöntem fareyi anlık olarak "serbest bırakıp tekrar yakalamaktır"
            // ya da oyuncunun dönme hızını (delta) sıfırlamaktır.

            mc.mouseHandler.releaseMouse();
            mc.mouseHandler.grabMouse();

            // Eğer release/grab çok sert gelirse, oyuncunun input değerlerini sıfırlıyoruz:
            mc.player.input.leftImpulse = 0;
            mc.player.input.forwardImpulse = 0;
        }
        lockedTarget = null;
        unlockTimer = 0;
    }

    public static void updateCameraFPS(Minecraft mc, float partialTick) {
        if (mc.player == null) return;

        // 1. HEDEF KONTROLÜ
        Entity currentTarget = LockState.getTarget();

        // DURUM A: Hedef az önce koptu (Temizler)
        if (currentTarget == null && lockedTarget != null && unlockTimer == 0) {
            onUnlock(mc);
            return; // Temizliği yap ve bu kareyi atla
        }

        // DURUM B: Hedef hala var veya tampon süresi işliyor
        if (currentTarget != null) {
            lockedTarget = currentTarget;
            unlockTimer = 5; // Hedef olduğu sürece süreyi tazele
        } else {
            if (unlockTimer > 0) {
                unlockTimer--; // Hedef yoksa ama süre bitmediyse geri say
            } else {
                lockedTarget = null; // Süre bittiyse tamamen bırak
            }
        }

        // 2. ZOOM MEKANİĞİ
        float targetZoom = isZoomActive() ? 0.85F : 1.0F;
        float inSpeed = CameraViewConfig.CLIENT.zoomInSpeed.get().floatValue() * 0.08f;
        float currentZoomSpeed = (targetZoom < 1.0F) ? inSpeed : 0.5F;

        if (Math.abs(zoomLerp - targetZoom) < 0.001F) {
            zoomLerp = targetZoom;
        } else {
            zoomLerp = Mth.lerp(currentZoomSpeed, zoomLerp, targetZoom);
        }

        // 3. EKSEN VE AÇI HESABI
        float targetYaw, targetPitch;
        if (lockedTarget != null && lockedTarget.isAlive() && cameraMode == 1) {
            // PartialTick ile yumuşatılmış konumlar
            double targetX = Mth.lerp(partialTick, lockedTarget.xo, lockedTarget.getX());
            double targetY = Mth.lerp(partialTick, lockedTarget.yo, lockedTarget.getY()) + lockedTarget.getEyeHeight() * 0.85;
            double targetZ = Mth.lerp(partialTick, lockedTarget.zo, lockedTarget.getZ());

            double dx = targetX - mc.player.getX();
            double dy = targetY - (mc.player.getY() + mc.player.getEyeHeight());
            double dz = targetZ - mc.player.getZ();

            targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
            targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            targetPitch = Mth.clamp(targetPitch, -40.0F, 40.0F);
        } else {
            targetYaw = mc.player.getYRot();
            targetPitch = mc.player.getXRot();
        }

        // 4. UYGULAMA VE BLOKE
        float currentLerp = getSmoothness();
        finalYaw = Mth.rotLerp(currentLerp, finalYaw, targetYaw);
        finalPitch = Mth.lerp(currentLerp, finalPitch, targetPitch);

        if (lockedTarget != null || unlockTimer > 0) {
            // Minecraft'ı çaresiz bırakan vuruş:
            applyAbsoluteForce(mc.player, finalYaw, finalPitch);
        }
    }

    private static void applyAbsoluteForce(net.minecraft.client.player.LocalPlayer player, float yaw, float pitch) {
        // 1. MEVCUT KARE (Current Frame)
        player.setYRot(yaw);
        player.setXRot(pitch);

        // 2. ÖNCEKİ KARE (Previous Frame - partialTicks etkisini sıfırlar)
        player.yRotO = yaw;
        player.xRotO = pitch;

        // 3. KAFA VE VÜCUT (Senkronizasyon)
        player.setYHeadRot(yaw);
        player.yHeadRotO = yaw;
        player.yBodyRot = yaw;
        player.yBodyRotO = yaw;

        // 4. ÇİFTE KİLİT (Bazı motor güncellemelerini yakalamak için tekrarlar)
        player.setYRot(yaw);
        player.setXRot(pitch);
    }
}