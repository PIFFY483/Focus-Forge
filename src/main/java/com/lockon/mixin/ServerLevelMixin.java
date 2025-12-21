package com.lockon.mixin;

import com.lockon.client.CrosshairTargetHelper;
import com.lockon.config.CameraViewConfig;
import com.lockon.lock.LockState;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Unique
    private boolean lockon$isProcessing = false;

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void lockon$interceptAndShiftProjectile(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (lockon$isProcessing) return;

        if (!(entity instanceof Projectile original) || !(original.getOwner() instanceof Player player)) return;
        if (LockState.isLocked() || !CameraViewConfig.ENABLE_SHOULDER_CAM.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.getCameraType().isFirstPerson()) return;

        // 1. HEDEFİ BUL (Crosshair'ın dünyada değdiği nokta)
        Vec3 targetPoint = CrosshairTargetHelper.getCrosshairTarget(128.0);
        if (targetPoint == null) return;

        // 2. ÇIKIŞ NOKTASINI HESAPLA (Kameranın önü ama Göz hizasında)
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 camLook = Vec3.directionFromRotation(mc.gameRenderer.getMainCamera().getXRot(), mc.gameRenderer.getMainCamera().getYRot());

        // Kamera ile oyuncu arasındaki mesafeyi bul ve 0.2 blok daha ekle (Oyuncunun önüne geçmek için)
        double distanceToPlayer = camPos.distanceTo(player.position());
        double pushForward = distanceToPlayer + 0.2;

        double spawnX = camPos.x + (camLook.x * pushForward);
        double spawnZ = camPos.z + (camLook.z * pushForward);
        double spawnY = player.getEyeY(); // Göz hizası kuralı

        Vec3 finalSpawnPoint = new Vec3(spawnX, spawnY, spawnZ);

        lockon$isProcessing = true;
        try {
            Entity replacement = entity.getType().create(player.level());
            if (replacement instanceof Projectile newProjectile) {
                newProjectile.restoreFrom(entity);
                newProjectile.setOwner(player);

                // Mermiyi hesaplanan o "ön" noktaya taşı
                newProjectile.moveTo(finalSpawnPoint.x, finalSpawnPoint.y, finalSpawnPoint.z, player.getYRot(), player.getXRot());

                // Yönü hesapla: (Yeni Ön Nokta -> Crosshair Hedefi)
                Vec3 direction = targetPoint.subtract(finalSpawnPoint).normalize();

                double speed = entity.getDeltaMovement().length();
                if (speed < 0.1) speed = 1.5;

                newProjectile.shoot(direction.x, direction.y, direction.z, (float) speed, 0.0F);

                player.level().addFreshEntity(newProjectile);
                cir.setReturnValue(true);
            }
        } finally {
            lockon$isProcessing = false;
        }
    }
}