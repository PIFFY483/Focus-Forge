package com.lockon.lock;

import com.lockon.client.LockTickHandler;
import net.minecraft.world.entity.LivingEntity;

public class LockState {

    private static LivingEntity target;
    private static boolean isLocked = false;

    public static LivingEntity getTarget() {
        return target;
    }

    public static boolean isLocked() {
        return isLocked;
    }

    public static void lockOn(LivingEntity newTarget) {
        if (newTarget != null) {
            target = newTarget;
            isLocked = true;
        }
    }

    public static void unlock() {
        if (isLocked) {
            isLocked = false;
            target = null;
            // Fare hassasiyetini geri yükle
            LockTickHandler.forceRestoreSensitivity();
            // Cooldown listesini sıfırla
            TargetScanner.resetIgnoredTargets();
        }
    }
}