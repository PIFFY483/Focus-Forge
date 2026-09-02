package com.lockon.mixin.brs;

import com.lockon.brs.camera.AlphaScalingVertexConsumer;
import com.lockon.brs.camera.PlayerTransparencyController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererAlphaMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private MultiBufferSource brs$wrapBufferSource(MultiBufferSource bufferSource,
                                                   LivingEntity entity,
                                                   float entityYaw,
                                                   float partialTicks,
                                                   PoseStack poseStack,
                                                   MultiBufferSource originalBufferSource,
                                                   int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity != mc.player) {
            return bufferSource;
        }

        float alpha = PlayerTransparencyController.getAlpha();
        if (alpha >= 0.999f) {
            return bufferSource;
        }

        MultiBufferSource original = bufferSource;
        return renderType -> new AlphaScalingVertexConsumer(original.getBuffer(renderType), alpha);
    }
}