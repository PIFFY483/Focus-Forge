package com.lockon.client;

import com.lockon.config.LockOnConfig;
import com.lockon.config.CameraViewConfig.VisualStyle;
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
import com.lockon.config.CameraViewConfig;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LockRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || mc.player == null) {
            return;
        }

        // 1. DURUM KONTROLÜ
        boolean isLocked = LockState.isLocked();
        boolean isPreview = mc.screen instanceof LockOnConfigScreen ||
                (mc.screen != null && mc.screen.getClass().getSimpleName().contains("CameraConfigScreen"));

        if (!isLocked && !isPreview) return;

        // --- KRİTİK: Görsel Stil Kontrolü ---
        VisualStyle style = CameraViewConfig.CLIENT.visualStyle.get();
        if (style == VisualStyle.OFF) return; // Stil kapalıysa hiçbir şey yapma

        LivingEntity target = LockState.getTarget();
        if (target == null && isPreview) {
            target = mc.player;
        }

        if (target == null) return;

        // DEĞİŞKENLERİ BURADA
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        // 2. POZİSYON HESABI
        double x = Mth.lerp(event.getPartialTick(), target.xo, target.getX()) - cameraPos.x;
        double z = Mth.lerp(event.getPartialTick(), target.zo, target.getZ()) - cameraPos.z;

        double baseTargetY = Mth.lerp(event.getPartialTick(), target.yo, target.getY());
        double finalTargetY;
        float entityHeight = target.getBbHeight();

        if (entityHeight > 2.0f) {
            finalTargetY = baseTargetY + (entityHeight * CameraViewConfig.CLIENT.dynamicFocusHeightRatio.get());
        } else {
            finalTargetY = baseTargetY + target.getEyeHeight();
        }

        finalTargetY += CameraViewConfig.CLIENT.iconYOffset.get();
        double y = finalTargetY - cameraPos.y;

        // 3. RENK HESABI
        float healthPercent = isLocked ? Mth.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F) : 0.75F;
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

        // --- ÇİZİM BAŞLANGICI ---
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Süzülme efekti
        float floating = Mth.sin((mc.player.tickCount + event.getPartialTick()) * 0.15F) * 0.04F;
        poseStack.translate(0, floating, 0);


        // Billboard
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        //Dönüş efekti
        float rotation = (mc.player.tickCount + event.getPartialTick()) * 0.04F;
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(rotation));

        // Boyut
        float iconScale = LockOnConfig.CROSSHAIR_XZ_SIZE.get().floatValue();
        poseStack.scale(iconScale, iconScale, iconScale);

        // 4. ASIL ÇİZİM ÇAĞRISI
        renderAdvancedIndicator(poseStack, r, g, b, 0.9F, style);

        poseStack.popPose();
    }

    private static void renderAdvancedIndicator(PoseStack poseStack, float r, float g, float b, float a, VisualStyle style) {
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.13F;
        float innerSize = size * 0.55F;

        // --- STİL 1: CROSSHAIR ---
        if (style == VisualStyle.CROSSHAIR) {
            drawSelectionLine(builder, matrix, 0, size, size, 0, r, g, b, a);
            drawSelectionLine(builder, matrix, size, 0, 0, -size, r, g, b, a);
            drawSelectionLine(builder, matrix, 0, -size, -size, 0, r, g, b, a);
            drawSelectionLine(builder, matrix, -size, 0, 0, size, r, g, b, a);

            float innerAlpha = a * 0.4F;
            drawSelectionLine(builder, matrix, 0, innerSize, innerSize, 0, r, g, b, innerAlpha);
            drawSelectionLine(builder, matrix, innerSize, 0, 0, -innerSize, r, g, b, innerAlpha);
            drawSelectionLine(builder, matrix, 0, -innerSize, -innerSize, 0, r, g, b, innerAlpha);
            drawSelectionLine(builder, matrix, -innerSize, 0, 0, innerSize, r, g, b, innerAlpha);
        }
        // --- STİL 2: HEXAGON ---
        else if (style == VisualStyle.HEXAGON) {
            for (int i = 0; i < 6; i++) {
                float angle1 = (float) Math.toRadians(i * 60);
                float angle2 = (float) Math.toRadians(i * 60 + 40);
                drawSelectionLine(builder, matrix, Mth.cos(angle1) * size, Mth.sin(angle1) * size, Mth.cos(angle2) * size, Mth.sin(angle2) * size, r, g, b, a);
            }
        }
        // --- STİL 3: STAR ---
        else if (style == VisualStyle.STAR) {
            float outer = size * 1.2f;
            float inner = size * 0.4f;
            for (int i = 0; i < 8; i++) {
                float angle = (float) Math.toRadians(i * 45);
                float length = (i % 2 == 0) ? outer : inner;
                drawSelectionLine(builder, matrix, 0, 0, Mth.cos(angle) * length, Mth.sin(angle) * length, r, g, b, a);
            }
        }

        else if (style == VisualStyle.VANGUARD) {
            float arm = size * 0.4F;
            // Sol Üst
            drawSelectionLine(builder, matrix, -size, -size + arm, -size, -size, r, g, b, a);
            drawSelectionLine(builder, matrix, -size, -size, -size + arm, -size, r, g, b, a);
            // Sağ Üst
            drawSelectionLine(builder, matrix, size, -size + arm, size, -size, r, g, b, a);
            drawSelectionLine(builder, matrix, size, -size, size - arm, -size, r, g, b, a);
            // Sol Alt
            drawSelectionLine(builder, matrix, -size, size - arm, -size, size, r, g, b, a);
            drawSelectionLine(builder, matrix, -size, size, -size + arm, size, r, g, b, a);
            // Sağ Alt
            drawSelectionLine(builder, matrix, size, size - arm, size, size, r, g, b, a);
            drawSelectionLine(builder, matrix, size, size, size - arm, size, r, g, b, a);
        }

        else if (style == VisualStyle.TRINITY) {
            for (int i = 0; i < 3; i++) {
                float angle = (float) Math.toRadians(i * 120 - 90); // -90 ile yukarı baktırdık
                float x1 = Mth.cos(angle) * size;
                float y1 = Mth.sin(angle) * size;
                float angleNext = (float) Math.toRadians((i + 1) * 120 - 90);
                drawSelectionLine(builder, matrix, x1, y1, Mth.cos(angleNext) * size, Mth.sin(angleNext) * size, r, g, b, a);
                // Köşelerdeki toplar (Artı şeklinde)
                float dot = 0.03F;
                drawSelectionLine(builder, matrix, x1 - dot, y1, x1 + dot, y1, r, g, b, a);
                drawSelectionLine(builder, matrix, x1, y1 - dot, x1, y1 + dot, r, g, b, a);
            }
            // Merkezdeki kutucuk
            float bS = 0.025F;
            drawSelectionLine(builder, matrix, -bS, -bS, bS, -bS, r, g, b, a);
            drawSelectionLine(builder, matrix, bS, -bS, bS, bS, r, g, b, a);
            drawSelectionLine(builder, matrix, bS, bS, -bS, bS, r, g, b, a);
            drawSelectionLine(builder, matrix, -bS, bS, -bS, -bS, r, g, b, a);
        }
        // --- STİL 5: HUNTER ---
        else if (style == VisualStyle.HUNTER) {
            for (int i = 0; i < 4; i++) {
                float angle = (float) Math.toRadians(i * 90);
                float tx = Mth.cos(angle) * size; float ty = Mth.sin(angle) * size;
                float sx1 = Mth.cos(angle + 0.35f) * (size * 0.7f); float sy1 = Mth.sin(angle + 0.35f) * (size * 0.7f);
                float sx2 = Mth.cos(angle - 0.35f) * (size * 0.7f); float sy2 = Mth.sin(angle - 0.35f) * (size * 0.7f);
                drawSelectionLine(builder, matrix, tx, ty, sx1, sy1, r, g, b, a);
                drawSelectionLine(builder, matrix, tx, ty, sx2, sy2, r, g, b, a);
            }
        }

        //  STİL 7: CHRONOS
        else if (style == VisualStyle.CHRONOS) {
            // 1. Dış Kesikli Daire (12 parça, saat gibi)
            for (int i = 0; i < 12; i++) {
                float angle1 = (float) Math.toRadians(i * 30);
                float angle2 = (float) Math.toRadians(i * 30 + 15); // Kesikli görünüm
                drawSelectionLine(builder, matrix,
                        Mth.cos(angle1) * size, Mth.sin(angle1) * size,
                        Mth.cos(angle2) * size, Mth.sin(angle2) * size, r, g, b, a);
            }

            // 2. İç İğneler
            for (int i = 0; i < 4; i++) {
                float angle = (float) Math.toRadians(i * 90);
                float xOuter = Mth.cos(angle) * (size * 0.8f);
                float yOuter = Mth.sin(angle) * (size * 0.8f);
                float xInner = Mth.cos(angle) * (size * 0.4f);
                float yInner = Mth.sin(angle) * (size * 0.4f);
                drawSelectionLine(builder, matrix, xOuter, yOuter, xInner, yInner, r, g, b, a * 0.7f);
            }
        }

        // --- ORTAK MERKEZ NOKTASI
        if (style != VisualStyle.OFF) {
            float d = 0.02F;
            drawSelectionLine(builder, matrix, -d, 0, d, 0, r, g, b, a);
            drawSelectionLine(builder, matrix, 0, -d, 0, d, r, g, b, a);
        }

        bufferSource.endBatch(RenderType.lines());
    }


    private static void drawSelectionLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (mc.player == null || !LockState.isLocked()) return;
        if (CameraViewConfig.CLIENT.visualStyle.get() == CameraViewConfig.VisualStyle.OFF) return;

        if (LockOnConfig.ENABLE_MOUSE_INPUT_WARNING.get()) {
            GuiGraphics graphics = event.getGuiGraphics();
            Component warning = Component.translatable("lockon.warning.mouse_input_disabled");
            int width = graphics.guiWidth();
            graphics.drawString(mc.font, warning, (width - mc.font.width(warning)) / 2, graphics.guiHeight() / 2 + 25, 0xFFFFFF, true);
        }
    }
}