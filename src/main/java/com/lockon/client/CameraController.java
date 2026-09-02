package com.lockon.client;

import com.lockon.config.LockOnConfig;
import com.lockon.lock.LockState;
import com.lockon.lock.TargetScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CameraController {

    private static final Minecraft mc = Minecraft.getInstance();

    public static float targetLockYaw = 0.0F;
    public static float targetLockPitch = 0.0F;

    /**
     * LockTickHandler tarafından çağrılır.
     */
    public static void lockAt(Vec3 targetPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double dX = targetPos.x() - mc.player.getX();
        double dY = targetPos.y() - (mc.player.getY() + mc.player.getEyeHeight());
        double dZ = targetPos.z() - mc.player.getZ();

        double distanceXZ = Math.sqrt(dX * dX + dZ * dZ);
        float calculatedPitch = (float) Mth.wrapDegrees(-Math.toDegrees(Mth.atan2(dY, distanceXZ)));
        float calculatedYaw = (float) Mth.wrapDegrees(Math.toDegrees(Mth.atan2(dZ, dX)) - 90.0);

        targetLockYaw = calculatedYaw;
        targetLockPitch = calculatedPitch;
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;
        boolean isLocked = LockState.isLocked();

        if (event.phase == TickEvent.Phase.END) {

            if (isLocked) {
                LivingEntity target = LockState.getTarget();
                double maxDisengagementRange = LockOnConfig.MAX_DISENGAGEMENT_RANGE.get();

                boolean targetDiedOrInvalid = target == null || !target.isAlive() || target.getHealth() <= 0;
                boolean targetTooFar = target != null && player.distanceTo(target) > maxDisengagementRange;

                // Kilitlenme Kesme Mantığı
                if (targetDiedOrInvalid || targetTooFar) {
                    if (targetDiedOrInvalid) {
                        LivingEntity newTarget = TargetScanner.findTarget();
                        if (newTarget != null) {
                            LockState.lockOn(newTarget);
                        } else {
                            LockState.unlock();
                            return;
                        }
                    } else {
                        LockState.unlock();
                        return;
                    }
                }

                player.yHeadRot = targetLockYaw;
                player.yBodyRot = targetLockYaw;

            } else {
                targetLockYaw = player.getYRot();
                targetLockPitch = player.getXRot();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (mc.player == null) return;

        LocalPlayer player = mc.player;
        boolean isLocked = LockState.isLocked();

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {

            if (isLocked) {
                LivingEntity target = LockState.getTarget();
                if (target == null) return;

                float partialTicks = event.getPartialTick(); // KRİTİK: Kısmi tikleri al

                // SMOOTH HEDEF POZİSYONLARINI HESAPLA
                double focusOffset = LockOnConfig.CAMERA_FOCUS_OFFSET.get();

                // Hedefin yumuşak pozisyonu
                double targetX = Mth.lerp(partialTicks, target.xOld, target.getX());
                double targetY = Mth.lerp(partialTicks, target.yOld, target.getY()) + focusOffset;
                double targetZ = Mth.lerp(partialTicks, target.zOld, target.getZ());

                // Oyuncunun yumuşak pozisyonu
                double playerX = Mth.lerp(partialTicks, player.xOld, player.getX());
                double playerY = Mth.lerp(partialTicks, player.yOld, player.getY()) + player.getEyeHeight();
                double playerZ = Mth.lerp(partialTicks, player.zOld, player.getZ());


                //SMOOTH HEDEF AÇILARINI HESAPLA
                double dX = targetX - playerX;
                double dY = targetY - playerY;
                double dZ = targetZ - playerZ;

                double distanceXZ = Math.sqrt(dX * dX + dZ * dZ);
                float smoothTargetPitch = (float) Mth.wrapDegrees(-Math.toDegrees(Mth.atan2(dY, distanceXZ)));
                float smoothTargetYaw = (float) Mth.wrapDegrees(Math.toDegrees(Mth.atan2(dZ, dX)) - 90.0);


                // titreşim hafiflet
                float currentYaw = player.getYRot();
                float currentPitch = player.getXRot();
                float rawLockSpeed = LockOnConfig.LOCK_SPEED.get().floatValue();

                final float FRAME_SMOOTH_FACTOR;

                final float MAX_INTERPOLATION_FACTOR = LockOnConfig.MAX_SMOOTHING_FACTOR.get().floatValue();

                if (rawLockSpeed >= 0.99) {
                    FRAME_SMOOTH_FACTOR = MAX_INTERPOLATION_FACTOR;
                } else {
                    // rawLockSpeed * 0.3F çarpanını 0.01F ile MAX_INTERPOLATION_FACTOR arasında sınırla.
                    FRAME_SMOOTH_FACTOR = Mth.clamp(rawLockSpeed * 0.3F, 0.01F, MAX_INTERPOLATION_FACTOR);
                }

                float deltaYaw = Mth.wrapDegrees(smoothTargetYaw - currentYaw);
                float deltaPitch = smoothTargetPitch - currentPitch;

                float interpolatedYaw = currentYaw + deltaYaw * FRAME_SMOOTH_FACTOR;
                float interpolatedPitch = currentPitch + deltaPitch * FRAME_SMOOTH_FACTOR;

                // Açıları kısıtla
                interpolatedPitch = Mth.clamp(interpolatedPitch, -90.0F, 90.0F);
                interpolatedYaw = Mth.wrapDegrees(interpolatedYaw);

                // Oyuncunun bakış açılarını (kamera) ayarlar
                player.setYRot(interpolatedYaw);
                player.setXRot(interpolatedPitch);

                // Oyuncunun kafa ve vücut rotasyonunu  ayarlar (Smooth bir şekilde)
                player.yHeadRot = interpolatedYaw;
                player.yBodyRot = interpolatedYaw;
            }
        }
    }
}