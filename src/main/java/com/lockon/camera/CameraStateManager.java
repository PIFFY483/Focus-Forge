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

    public static boolean isShoulderCamActuallyActive(Minecraft mc) {
        // 1. Ayarlardan omuz kamerası tamamen kapalı mı?
        if (!CameraViewConfig.ENABLE_SHOULDER_CAM.get()) return false;

        // 2. Oyuncu birinci şahıs (First Person) modunda mı?
        // Minecraft'ta 0: First Person, 1: Third Person Back, 2: Third Person Front
        // Omuz kamerası sadece Third Person modlarında çalışmalı (tercihen sadece 1)
        if (mc.options.getCameraType().isFirstPerson()) return false;

        // 3. Bizim omuz kamerası modu "Hidden" değilse (modeNames'teki 0. index)
        return CameraStateManager.cameraMode > 0;
    }

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
            // Bazı sürümlerde mouseHandler.accumulatedDX/DY değişkenlerine erişilemiyebilir,
            // bu yüzden en garanti yöntem fareyi anlık olarak "serbest bırakıp tekrar yakalamaktır"
            // ya da oyuncunun dönme hızını (delta) sıfırla.

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

        if (currentTarget == null && lockedTarget != null && unlockTimer == 0) {
            onUnlock(mc);
            return;
        }

        if (currentTarget != null) {
            lockedTarget = currentTarget;
            unlockTimer = 5;
        } else {
            if (unlockTimer > 0) {
                unlockTimer--;
            } else {
                lockedTarget = null;
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
            double targetX = Mth.lerp(partialTick, lockedTarget.xo, lockedTarget.getX());
            double targetZ = Mth.lerp(partialTick, lockedTarget.zo, lockedTarget.getZ());

            double baseTargetY = Mth.lerp(partialTick, lockedTarget.yo, lockedTarget.getY());
            double finalTargetY;
            float entityHeight = lockedTarget.getBbHeight();

            if (entityHeight > 2.0f) {
                finalTargetY = baseTargetY + (entityHeight * CameraViewConfig.CLIENT.dynamicFocusHeightRatio.get());
            } else {
                finalTargetY = baseTargetY + lockedTarget.getEyeHeight();
            }

            finalTargetY += CameraViewConfig.CLIENT.focusOffsetY.get();

            double dx = targetX - mc.player.getX();
            double dy = finalTargetY - (mc.player.getY() + mc.player.getEyeHeight());
            double dz = targetZ - mc.player.getZ();

            targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
            targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            targetPitch = Mth.clamp(targetPitch, -40.0F, 40.0F);

            // --- YENİ SİNEMATİK TAKİP MANTIĞI BAŞLANIÇI ---
            float currentLerp = getSmoothness();

            // 1. Mesafe Freni: Mob 5 bloktan yakındaysa hızı mesafeye oranla %50'ye kadar düşürür
            float distance = mc.player.distanceTo(lockedTarget);
            if (distance < 5.0f) {
                float distanceFactor = Mth.clamp(distance / 5.0f, 0.5f, 1.0f);
                currentLerp *= distanceFactor;
            }

            // 2. Açısal Basamak (Stepping): Kare başına max dönüşü kısıtlar (Titremeyi bitiren ana nokta)
            float yawDiff = Mth.wrapDegrees(targetYaw - finalYaw);
            float pitchDiff = targetPitch - finalPitch;

            // maxStep: Bir karede kameranın "atlayabileceği" maksimum derece.
            // Bu değer yüksek senslerde sıçramayı (jitter) engeller.
            float maxStep = 4.5f;

            float stepYaw = Mth.clamp(yawDiff * currentLerp, -maxStep, maxStep);
            float stepPitch = Mth.clamp(pitchDiff * currentLerp, -maxStep, maxStep);

            finalYaw += stepYaw;
            finalPitch += stepPitch;
            // --- YENİ SİNEMATİK TAKİP MANTIĞI BİTİŞİ ---

        } else {
            targetYaw = mc.player.getYRot();
            targetPitch = mc.player.getXRot();

            // Kilit yokken normal yumuşatma ile devam et
            float currentLerp = getSmoothness();
            finalYaw = Mth.rotLerp(currentLerp, finalYaw, targetYaw);
            finalPitch = Mth.lerp(currentLerp, finalPitch, targetPitch);
        }

        // 4. UYGULAMA VE BLOKE
        if (lockedTarget != null || unlockTimer > 0) {
            finalYaw = Mth.wrapDegrees(finalYaw);
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