package com.lockon.mixin;

import com.lockon.config.CameraViewConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "getViewVector", at = @At("HEAD"), cancellable = true)
    private void lockon$overrideViewVector(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        if (self != mc.player) return;
        if (!CameraViewConfig.ENABLE_SHOULDER_CAM.get()) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        Vec3 eyePos = self.getEyePosition(partialTicks);
        Vec3 cameraDir = Vec3.directionFromRotation(
                mc.gameRenderer.getMainCamera().getXRot(),
                mc.gameRenderer.getMainCamera().getYRot()
        );

        Item heldItem = mc.player.getMainHandItem().getItem();

        // Yay/arbalet/mızrak: mermi namlu/kameradan çıktığı için gerçek
        // yakınsama (convergence) sağlamak amacıyla yön telafisine ihtiyaç duyar.
        boolean isRangedWeapon = heldItem instanceof ProjectileWeaponItem
                || heldItem instanceof TridentItem;
        // Yerleştirilebilir bloklar: Player.pick() ray'i zaten eyePos'tan
        // (kaydırılmış omuz kamerasından DEĞİL) başladığı için hiçbir parallax
        // kayması yaşamaz — telafiye ihtiyacı yoktur. Aşağıdaki telafiyi bloklara
        // da uygulamak, hedefi merkezden gereksiz yere uzaklaştırıyordu (sola/
        // arkaya kayma bug'ının kaynağı buydu).
        boolean isBlock = heldItem instanceof BlockItem;
        boolean isRanged = isRangedWeapon || isBlock;

        /* =========================
           UZAK / FIRLATILAN SİLAH + BLOK
           ========================= */
        if (isRanged) {

            if (isRangedWeapon) {
                double shoulderX = CameraViewConfig.SHOULDER_OFFSET_X.get();

                // Sadece SAĞ omuzda telafi uygula
                if (shoulderX > 0.0) {
                    Vec3 up = new Vec3(0, 1, 0);
                    Vec3 left = cameraDir.cross(up).normalize();

                    // Hit noktasını karakterin önüne (merkeze) doğru çeken telafi miktarı.
                    // Tavan 0.12 -> 0.17 ve çarpan 0.18 -> 0.24 yapıldı: varsayılan
                    // shoulderX (0.7) değerinde artık tavana daha erken ulaşıp daha
                    // fazla merkeze doğru düzeltme uyguluyor (daha az "dışarı" sapma).
                    double compensationStrength = Math.min(0.17, shoulderX * 0.24);
                    cameraDir = cameraDir.subtract(left.scale(compensationStrength)).normalize();
                }
            }

            // Blok için: ham cameraDir + eyePos = zaten karakter merkezinden
            // baktığı yön, ekstra düzeltmeye gerek yok -> otomatik olarak ortalı/önde çıkar.
            cir.setReturnValue(cameraDir);
            return;
        }

        /* =========================
           YAKIN DÖVÜŞ
           ========================= */

        double scanDistance = 5.0;
        AABB scanBox = new AABB(
                eyePos,
                eyePos.add(cameraDir.scale(scanDistance))
        ).inflate(2.0);

        List<Entity> candidates = mc.level.getEntities(
                self,
                scanBox,
                e -> e instanceof LivingEntity && e.isPickable()
        );

        Entity bestTarget = null;
        double bestDot = 0.85;

        for (Entity target : candidates) {
            Vec3 toTarget = target.position()
                    .add(0, target.getEyeHeight() * 0.6, 0)
                    .subtract(eyePos)
                    .normalize();

            double dot = cameraDir.dot(toTarget);
            if (dot > bestDot) {
                bestDot = dot;
                bestTarget = target;
            }
        }

        if (bestTarget != null) {
            Vec3 aim = bestTarget.position()
                    .add(0, bestTarget.getEyeHeight() * 0.7, 0)
                    .subtract(eyePos)
                    .normalize();

            cir.setReturnValue(aim);
            return;
        }

        /* =========================
           FALLBACK – CROSSHAIR RAY
           ========================= */
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 rayEnd = camPos.add(cameraDir.scale(100.0));
        HitResult hit = mc.level.clip(
                new ClipContext(camPos, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, self)
        );

        Vec3 finalDir = hit.getType() == HitResult.Type.MISS
                ? cameraDir
                : hit.getLocation().subtract(eyePos).normalize();

        cir.setReturnValue(finalDir);
    }
}