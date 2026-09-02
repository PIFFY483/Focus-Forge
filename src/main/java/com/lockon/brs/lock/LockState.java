package com.lockon.brs.lock;

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
            TargetScanner.resetIgnoredTargets();
        }
    }
}