package com.lockon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;

public class CrosshairTargetHelper {

    /**
     * Crosshair'in baktığı dünyadaki noktayı döndürür.
     */
    public static Vec3 getCrosshairTarget(double maxDistance) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        float pitch = mc.gameRenderer.getMainCamera().getXRot();
        float yaw = mc.gameRenderer.getMainCamera().getYRot();

        // Görsel kaymayı hesaba katarak açıyı düzelt
        float visualOffset = CrosshairHandler.getCurrentVisualOffset();
        double screenWidth = mc.getWindow().getGuiScaledWidth();

        if (screenWidth > 0 && Math.abs(visualOffset) > 0.1f) {
            double fov = mc.options.fov().get();
            double aspectRatio = (double)mc.getWindow().getWidth() / mc.getWindow().getHeight();
            double horizontalFov = fov * aspectRatio;
            float yawAdjustment = (float) ((visualOffset / screenWidth) * horizontalFov);
            yaw += yawAdjustment;
        }

        Vec3 lookDir = Vec3.directionFromRotation(pitch, yaw);
        Vec3 endPos = camPos.add(lookDir.scale(maxDistance));

        HitResult hit = mc.level.clip(new ClipContext(
                camPos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        ));

        return hit.getType() == HitResult.Type.MISS ? endPos : hit.getLocation();
    }

    public static Vec3 getMuzzlePosition() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }
}