package com.lockon.client;

import com.lockon.config.LockOnConfig;
import com.lockon.config.LockOnConfig.VisualStyle;
import com.lockon.lock.LockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LockRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES ||
                mc.player == null || !LockState.isLocked()) {
            return;
        }

        if (LockOnConfig.VISUAL_STYLE.get() == VisualStyle.OFF) return;

        LivingEntity target = LockState.getTarget();
        if (target == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();

        // 1. POZİSYON: Mobun kafasının biraz üstü ve akıcı takip (Lerp)
        double x = Mth.lerp(event.getPartialTick(), target.xo, target.getX()) - cameraPos.x;
        double y = Mth.lerp(event.getPartialTick(), target.yo, target.getY()) - cameraPos.y + target.getBbHeight() + 0.35;
        double z = Mth.lerp(event.getPartialTick(), target.zo, target.getZ()) - cameraPos.z;

        // 2. ANİMASYON: Hafif süzülme (Sinüs dalgası)
        float floating = Mth.sin((mc.player.tickCount + event.getPartialTick()) * 0.15F) * 0.04F;
        poseStack.translate(x, y + floating, z);

        // 3. BILLBOARD: Her zaman oyuncuya dönük kalmasını sağlar
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        // 4. RENK HESABI: Altın -> Turuncu -> Kırmızı
        float healthPercent = Mth.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F);
        float r, g, b;
        if (healthPercent > 0.5F) {
            r = 1.0F;
            g = Mth.lerp((healthPercent - 0.5F) * 2.0F, 0.45F, 0.85F);
            b = 0.0F;
        } else {
            r = Mth.lerp(healthPercent * 2.0F, 0.75F, 1.0F);
            g = Mth.lerp(healthPercent * 2.0F, 0.0F, 0.45F);
            b = 0.0F;
        }

        // 5. ÇİZİM
        renderAdvancedIndicator(poseStack, r, g, b, 0.9F);

        poseStack.popPose();
    }

    private static void renderAdvancedIndicator(PoseStack poseStack, float r, float g, float b, float a) {
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.13F;
        float innerSize = size * 0.55F;

        // DIŞ KATMAN (Ana Elmas)
        drawSelectionLine(builder, matrix, 0, size, size, 0, r, g, b, a);
        drawSelectionLine(builder, matrix, size, 0, 0, -size, r, g, b, a);
        drawSelectionLine(builder, matrix, 0, -size, -size, 0, r, g, b, a);
        drawSelectionLine(builder, matrix, -size, 0, 0, size, r, g, b, a);

        // İÇ KATMAN (Derinlik veren ikinci elmas)
        float innerAlpha = a * 0.4F;
        drawSelectionLine(builder, matrix, 0, innerSize, innerSize, 0, r, g, b, innerAlpha);
        drawSelectionLine(builder, matrix, innerSize, 0, 0, -innerSize, r, g, b, innerAlpha);
        drawSelectionLine(builder, matrix, 0, -innerSize, -innerSize, 0, r, g, b, innerAlpha);
        drawSelectionLine(builder, matrix, -innerSize, 0, 0, innerSize, r, g, b, innerAlpha);

        // MERKEZ ODAK (Küçük artı işareti)
        float dot = 0.02F;
        drawSelectionLine(builder, matrix, -dot, 0, dot, 0, r, g, b, a);
        drawSelectionLine(builder, matrix, 0, -dot, 0, dot, r, g, b, a);

        bufferSource.endBatch(RenderType.lines());
    }

    private static void drawSelectionLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (mc.player == null || !LockState.isLocked()) return;
        if (LockOnConfig.VISUAL_STYLE.get() != VisualStyle.CROSSHAIR) return;

        if (LockOnConfig.ENABLE_MOUSE_INPUT_WARNING.get()) {
            GuiGraphics graphics = event.getGuiGraphics();
            Component warning = Component.translatable("lockon.warning.mouse_input_disabled");
            int width = graphics.guiWidth();
            graphics.drawString(mc.font, warning, (width - mc.font.width(warning)) / 2, graphics.guiHeight() / 2 + 25, 0xFFFFFF, true);
        }
    }
}