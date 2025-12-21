package com.lockon.mixin;

import com.lockon.client.CrosshairTargetHelper;
import com.lockon.config.CameraViewConfig;
import com.lockon.lock.LockState;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @Inject(method = "shoot(DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void lockon$redirectShoot(double x, double y, double z, float velocity, float inaccuracy, CallbackInfo ci) {
        // Kilitlenme KAPALI ve Omuz Kamerası AÇIK iken mermiyi düzelt
        if (!LockState.isLocked() && CameraViewConfig.ENABLE_SHOULDER_CAM.get()) {

            Vec3 target = CrosshairTargetHelper.getCrosshairTarget(128.0);
            // Artık getMuzzlePosition hata vermeyecektir
            Vec3 source = CrosshairTargetHelper.getMuzzlePosition();

            if (target != null && source != null) {
                Projectile projectile = (Projectile) (Object) this;

                // Merminin gideceği yönü kameradan hedefe doğru hesapla
                Vec3 dir = target.subtract(source).normalize();

                // Mermiyi yeni yörüngeye mühürle
                projectile.setDeltaMovement(dir.scale(velocity));

            }
        }
    }
}