package com.lockon.brs.lock;

import com.lockon.LockOnMod;
import com.lockon.brs.camera.CameraStateManager;
import com.lockon.client.KeyBindings;
import com.lockon.brs.config.LockOnConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, value = Dist.CLIENT)
public class LockTickHandler {

    private static final Minecraft mc = Minecraft.getInstance();
    private static long lastUnlockTime = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.player == null || mc.level == null) return;

        if (!com.lockon.lock.LockType.isType2()) {
            if (LockState.isLocked()) {
                LockState.unlock();
                CameraStateManager.onUnlock(mc);
                lastUnlockTime = System.currentTimeMillis();
            }
            return;
        }

        boolean isCurrentlyLocked = LockState.isLocked();

        // Target Scan Frequency kontrolü
        if (mc.player.tickCount % LockOnConfig.TARGET_SCAN_FREQUENCY.get() == 0) {
            if (isCurrentlyLocked) {
                LivingEntity currentTarget = LockState.getTarget();

                if (currentTarget == null || !currentTarget.isAlive()) {

                    LivingEntity replacement = TargetScanner.findReacquireTarget(currentTarget);
                    if (replacement != null) {
                        LockState.lockOn(replacement);
                        CameraStateManager.lockedTarget = replacement;
                    } else {
                        LockState.unlock();
                        CameraStateManager.onUnlock(mc);
                    }
                } else if (!TargetScanner.isTargetStillValid(currentTarget)) {

                    LockState.unlock();
                    CameraStateManager.onUnlock(mc);
                    lastUnlockTime = System.currentTimeMillis();
                }
            }
        }

        // V tuşu ile lock/unlock
        while (KeyBindings.LOCK_KEY.consumeClick()) {
            if (isCurrentlyLocked) {
                LockState.unlock();
                CameraStateManager.onUnlock(mc);
                lastUnlockTime = System.currentTimeMillis();
            } else {
                long currentTime = System.currentTimeMillis();
                double unlockCooldown = LockOnConfig.UNLOCK_COOLDOWN_SECONDS.get();
                if (currentTime - lastUnlockTime >= (unlockCooldown * 1000L)) {
                    LivingEntity newTarget = TargetScanner.findTarget();
                    if (newTarget != null) {
                        LockState.lockOn(newTarget);
                        CameraStateManager.onLockStart(mc, newTarget);
                    }
                }
            }
        }

        // Orta fare tuşu ile hedef değiştirme
        while (KeyBindings.TARGET_SWITCH_KEY.consumeClick()) {
            if (isCurrentlyLocked) {
                LivingEntity targetToIgnore = LockState.getTarget();
                LivingEntity newTarget = TargetScanner.findTarget(targetToIgnore);
                if (newTarget != null && newTarget != targetToIgnore) {
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
            LockState.unlock();
            CameraStateManager.onUnlock(mc);
            lastUnlockTime = System.currentTimeMillis();
        }
    }
}