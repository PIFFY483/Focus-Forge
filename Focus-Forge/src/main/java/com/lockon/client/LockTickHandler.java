package com.lockon.client;

import com.lockon.config.LockOnConfig;
import com.lockon.lock.LockState;
import com.lockon.lock.TargetScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import com.lockon.camera.CameraStateManager;
import net.minecraftforge.client.event.ScreenEvent;


import java.lang.reflect.Field;
import java.util.Arrays;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LockTickHandler {

    private static final Minecraft mc = Minecraft.getInstance();


    private static double originalMouseSensitivity = -1.0;

    // YENİ ALAN: Son kilit açma zamanı
    private static long lastUnlockTime = 0;

    // GÜVENİLİR REFLECTION ALANI
    private static Field sensitivityField = null;

    /**
     * Fare hassasiyeti alanını bulmak için reflection stratejisini kullanır.
     */
    private static Field getSensitivityField() {
        if (sensitivityField != null) {
            return sensitivityField;
        }

        try {
            // net.minecraft.client.Options'da "sensitivity" adında bir Field ara
            Field field = net.minecraft.client.Options.class.getDeclaredField("sensitivity");
            field.setAccessible(true);
            sensitivityField = field;
            return sensitivityField;
        } catch (NoSuchFieldException e) {
            // Alan bulunamazsa (farklı bir obfuscated isim kullanılıyorsa)
            // Bu kısım, mapping sorunları için bir geri dönüş mekanizmasıdır.
            // Genellikle 'sensitivity' doğru isimdir.
            System.err.println("LockOnMod: Mouse sensitivity alanı yansıma ile bulunamadı: " + e.getMessage());
            return null;
        } catch (SecurityException e) {
            System.err.println("LockOnMod: Mouse sensitivity alanına erişim engellendi: " + e.getMessage());
            return null;
        }
    }


    /**
     * Fare hassasiyetini 0.0'a ayarlar (Reflection kullanarak).
     */
    private static void setZeroMouseSensitivity() {
        if (mc.options == null) return;

        try {
            Field field = getSensitivityField();
            // Reflection başarılı değilse, fallback olarak OptionInstance'ın public metodunu dene
            if (field == null) {
                // OptionInstance ile çalışan public metodu dene (eski hataya neden olan kısım)
                if (mc.options.sensitivity() != null) {
                    if (originalMouseSensitivity == -1.0) {
                        originalMouseSensitivity = mc.options.sensitivity().get();
                    }
                    mc.options.sensitivity().set(0.0);
                }
                return;
            }

            // Private OptionInstance alanına reflection ile eriş
            @SuppressWarnings("unchecked")
            net.minecraft.client.OptionInstance<Double> sensitivityInstance =
                    (net.minecraft.client.OptionInstance<Double>) field.get(mc.options);

            if (sensitivityInstance != null) {
                if (originalMouseSensitivity == -1.0) {
                    originalMouseSensitivity = sensitivityInstance.get();
                }
                // OptionInstance'ın set metodu public olduğu için güvenle kullanılabilir
                sensitivityInstance.set(0.0);
            }
        } catch (Exception e) {
            System.err.println("LockOnMod: Fare hassasiyetini ayarlarken yansıma hatası: " + e.getMessage());
        }
    }

    /**
     * Fare hassasiyetini orijinal değerine geri yükler (Reflection kullanarak).
     */
    private static void restoreOriginalMouseSensitivity() {
        if (mc.options == null || originalMouseSensitivity == -1.0) return;

        try {
            Field field = getSensitivityField();
            if (field == null) {
                // Fallback
                if (mc.options.sensitivity() != null) {
                    mc.options.sensitivity().set(originalMouseSensitivity);
                }
                originalMouseSensitivity = -1.0;
                return;
            }

            @SuppressWarnings("unchecked")
            net.minecraft.client.OptionInstance<Double> sensitivityInstance =
                    (net.minecraft.client.OptionInstance<Double>) field.get(mc.options);

            if (sensitivityInstance != null) {
                sensitivityInstance.set(originalMouseSensitivity);
                originalMouseSensitivity = -1.0;
            }
        } catch (Exception e) {
            System.err.println("LockOnMod: Orijinal fare hassasiyetini geri yüklerken yansıma hatası: " + e.getMessage());
        }
    }

    public static void forceRestoreSensitivity() {
        restoreOriginalMouseSensitivity();
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.player == null || mc.level == null) return;


        boolean isCurrentlyLocked = LockState.isLocked();

        // Target Scan Frequency kontrolü
        if (mc.player.tickCount % LockOnConfig.TARGET_SCAN_FREQUENCY.get() == 0) {
            if (isCurrentlyLocked) {
                LivingEntity currentTarget = LockState.getTarget();

                // Mevcut hedef hala geçerli değilse
                if (currentTarget == null || !currentTarget.isAlive() || !TargetScanner.isTargetStillValid(currentTarget)) {
                    LockState.unlock();
                }
            }
        }

        if (isCurrentlyLocked) {
            LivingEntity target = LockState.getTarget();
            if (target == null) {
                LockState.unlock();
                return;
            }

            double targetHeight = target.getEyeHeight() + LockOnConfig.CAMERA_FOCUS_OFFSET.get();
            Vec3 targetPos = target.getPosition(1.0F).add(0.0, targetHeight, 0.0);

            // Kamerayı hedefe kilitle
            CameraController.lockAt(targetPos);

        } else {
            // Kilitlenme yoksa ve hassasiyet 0.0 ise, geri yükler.
            if (originalMouseSensitivity != -1.0) {
                restoreOriginalMouseSensitivity();
            }
        }

        // Tuş Girişi İşleme (Kilitlenme/Kilit Açma)
        while (KeyBindings.LOCK_KEY.consumeClick()) {
            if (isCurrentlyLocked) {
                // Kilit açma
                LockState.unlock();
                CameraStateManager.onUnlock(Minecraft.getInstance());
                lastUnlockTime = System.currentTimeMillis(); // Kilit açma zamanını kaydet
            } else {
                // Kilitlenme girişimi
                long currentTime = System.currentTimeMillis();
                double unlockCooldown = LockOnConfig.UNLOCK_COOLDOWN_SECONDS.get();
                if (currentTime - lastUnlockTime >= (unlockCooldown * 1000L)) {

                    LivingEntity newTarget = TargetScanner.findTarget();
                    if (newTarget != null) {
                        setZeroMouseSensitivity();
                        LockState.lockOn(newTarget);
                        CameraStateManager.onLockStart(mc, newTarget);





                    }
                }
            }
        }

        // Target Switch tuşu
        while (KeyBindings.TARGET_SWITCH_KEY.consumeClick()) {
            if (isCurrentlyLocked) {
                LivingEntity targetToIgnore = LockState.getTarget();
                LivingEntity newTarget = TargetScanner.findTarget(targetToIgnore);

                if (newTarget != null && newTarget != targetToIgnore) {
                    // LockState.lockOn'dan önce hedefi yoksayılanlar listesine ekle.
                    TargetScanner.markTargetIgnored(targetToIgnore);
                    LockState.lockOn(newTarget);
                    CameraStateManager.lockedTarget = newTarget;
                }
            }
        }
    }


    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (LockState.isLocked()) {
            // kilidi ve hassasiyeti sıfırlar
            LockState.unlock();
            CameraStateManager.onUnlock(mc);
            lastUnlockTime = System.currentTimeMillis();

        }
    }

}
