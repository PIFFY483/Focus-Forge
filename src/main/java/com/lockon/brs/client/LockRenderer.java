package com.lockon.brs.client;

import com.lockon.brs.config.LockOnConfig;
import com.lockon.config.CameraViewConfig;
import com.lockon.brs.client.gui.LockOnConfigScreen;
import com.lockon.brs.lock.LockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LockRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || mc.player == null) {
            return;
        }

        boolean isLocked;
        LivingEntity target;

        if (com.lockon.lock.LockType.isType1()) {
            isLocked = com.lockon.lock.LockState.isLocked();
            target = com.lockon.lock.LockState.getTarget();
        } else {
            isLocked = com.lockon.brs.lock.LockState.isLocked();
            target = com.lockon.brs.lock.LockState.getTarget();
        }

        boolean isPreview = mc.screen instanceof LockOnConfigScreen;
        if (!isLocked && !isPreview) return;

        int style = LockOnConfig.CROSSHAIR_STYLE.get();
        if (style < 0 || style > 11) return;

        if (target == null && isPreview) {
            target = mc.player;
        }
        if (target == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        double x = Mth.lerp(event.getPartialTick(), target.xo, target.getX()) - cameraPos.x;
        double z = Mth.lerp(event.getPartialTick(), target.zo, target.getZ()) - cameraPos.z;
        double baseTargetY = Mth.lerp(event.getPartialTick(), target.yo, target.getY());
        double finalTargetY;
        float entityHeight = target.getBbHeight();

        if (entityHeight > 2.0f) {
            finalTargetY = baseTargetY + (entityHeight * CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD.get());
        } else {
            finalTargetY = baseTargetY + target.getEyeHeight();
        }
        finalTargetY += LockOnConfig.ICON_Y_OFFSET.get();

        double y = finalTargetY - cameraPos.y;

        // Renk hesabı (can oranına göre)
        float healthPercent = isLocked
                ? Mth.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F)
                : 0.75F; // Preview modunda %75 sağlık

        // 1. Config'den seçilen baz rengi al
        int colorIndex = LockOnConfig.CROSSHAIR_COLOR_INDEX.get();
        float baseR, baseG, baseB;
        switch (colorIndex) {
            case 1 -> { baseR = 0.0f; baseG = 1.0f; baseB = 0.0f; }   // Green
            case 2 -> { baseR = 0.13f; baseG = 0.27f; baseB = 1.0f; }  // Blue
            case 3 -> { baseR = 1.0f; baseG = 0.15f; baseB = 0.15f; }  // Red
            case 4 -> { baseR = 1.0f; baseG = 1.0f; baseB = 1.0f; }    // White
            default -> { baseR = 0.0f; baseG = 1.0f; baseB = 1.0f; }   // Turquoise
        }

        // 2. Kapalı gri hedef (sağlık 0 olduğunda bu renge ulaşır)
        float grayR = 0.30f, grayG = 0.30f, grayB = 0.30f;

        // 3. Sağlık oranına göre baz renkten griye doğru lerp
        float r = Mth.lerp(healthPercent, grayR, baseR);
        float g = Mth.lerp(healthPercent, grayG, baseG);
        float b = Mth.lerp(healthPercent, grayB, baseB);

        // 4. Alpha da sağlıkla azalsın (zayıf hedefler daha şeffaf — sinematik)
        float baseAlpha = 0.9F;
        float alpha = Mth.lerp(healthPercent, 0.3F, baseAlpha);

        // Çizim başlangıcı
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Süzülme efekti
        float floating = Mth.sin((mc.player.tickCount + event.getPartialTick()) * 0.15F) * 0.04F;
        poseStack.translate(0, floating, 0);

        // Billboard (her zaman kameraya baksın)
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        // Dönüş efekti
        float rotation = (mc.player.tickCount + event.getPartialTick()) * 0.04F;
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(rotation));

        // Boyut
        float iconScale = LockOnConfig.CROSSHAIR_SIZE.get().floatValue();
        poseStack.scale(iconScale, iconScale, iconScale);

        // Asıl çizim
        renderIndicator(poseStack, r, g, b, alpha, style);

        poseStack.popPose();
    }

    private static void renderIndicator(PoseStack poseStack, float r, float g, float b, float a, int style) {
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.13F;
        float innerSize = size * 0.55F;

        switch (style) {
            case 0 -> { // CROSSHAIR
                drawLine(builder, matrix, 0, size, size, 0, r, g, b, a);
                drawLine(builder, matrix, size, 0, 0, -size, r, g, b, a);
                drawLine(builder, matrix, 0, -size, -size, 0, r, g, b, a);
                drawLine(builder, matrix, -size, 0, 0, size, r, g, b, a);
                float innerAlpha = a * 0.4F;
                drawLine(builder, matrix, 0, innerSize, innerSize, 0, r, g, b, innerAlpha);
                drawLine(builder, matrix, innerSize, 0, 0, -innerSize, r, g, b, innerAlpha);
                drawLine(builder, matrix, 0, -innerSize, -innerSize, 0, r, g, b, innerAlpha);
                drawLine(builder, matrix, -innerSize, 0, 0, innerSize, r, g, b, innerAlpha);
            }
            case 1 -> { // HEXAGON
                for (int i = 0; i < 6; i++) {
                    float angle1 = (float) Math.toRadians(i * 60);
                    float angle2 = (float) Math.toRadians(i * 60 + 40);
                    drawLine(builder, matrix,
                            Mth.cos(angle1) * size, Mth.sin(angle1) * size,
                            Mth.cos(angle2) * size, Mth.sin(angle2) * size,
                            r, g, b, a);
                }
            }
            case 2 -> { // STAR
                float outer = size * 1.2f;
                float inner = size * 0.4f;
                for (int i = 0; i < 8; i++) {
                    float angle = (float) Math.toRadians(i * 45);
                    float length = (i % 2 == 0) ? outer : inner;
                    drawLine(builder, matrix, 0, 0,
                            Mth.cos(angle) * length, Mth.sin(angle) * length,
                            r, g, b, a);
                }
            }
            case 3 -> { // VANGUARD
                float arm = size * 0.4F;
                drawLine(builder, matrix, -size, -size + arm, -size, -size, r, g, b, a);
                drawLine(builder, matrix, -size, -size, -size + arm, -size, r, g, b, a);
                drawLine(builder, matrix, size, -size + arm, size, -size, r, g, b, a);
                drawLine(builder, matrix, size, -size, size - arm, -size, r, g, b, a);
                drawLine(builder, matrix, -size, size - arm, -size, size, r, g, b, a);
                drawLine(builder, matrix, -size, size, -size + arm, size, r, g, b, a);
                drawLine(builder, matrix, size, size - arm, size, size, r, g, b, a);
                drawLine(builder, matrix, size, size, size - arm, size, r, g, b, a);
            }
            case 4 -> { // TRINITY
                for (int i = 0; i < 3; i++) {
                    float angle = (float) Math.toRadians(i * 120 - 90);
                    float x1 = Mth.cos(angle) * size;
                    float y1 = Mth.sin(angle) * size;
                    float angleNext = (float) Math.toRadians((i + 1) * 120 - 90);
                    drawLine(builder, matrix, x1, y1,
                            Mth.cos(angleNext) * size, Mth.sin(angleNext) * size,
                            r, g, b, a);
                    float dot = 0.03F;
                    drawLine(builder, matrix, x1 - dot, y1, x1 + dot, y1, r, g, b, a);
                    drawLine(builder, matrix, x1, y1 - dot, x1, y1 + dot, r, g, b, a);
                }
                float bS = 0.025F;
                drawLine(builder, matrix, -bS, -bS, bS, -bS, r, g, b, a);
                drawLine(builder, matrix, bS, -bS, bS, bS, r, g, b, a);
                drawLine(builder, matrix, bS, bS, -bS, bS, r, g, b, a);
                drawLine(builder, matrix, -bS, bS, -bS, -bS, r, g, b, a);
            }
            case 5 -> { // HUNTER
                for (int i = 0; i < 4; i++) {
                    float angle = (float) Math.toRadians(i * 90);
                    float tx = Mth.cos(angle) * size;
                    float ty = Mth.sin(angle) * size;
                    float sx1 = Mth.cos(angle + 0.35f) * (size * 0.7f);
                    float sy1 = Mth.sin(angle + 0.35f) * (size * 0.7f);
                    float sx2 = Mth.cos(angle - 0.35f) * (size * 0.7f);
                    float sy2 = Mth.sin(angle - 0.35f) * (size * 0.7f);
                    drawLine(builder, matrix, tx, ty, sx1, sy1, r, g, b, a);
                    drawLine(builder, matrix, tx, ty, sx2, sy2, r, g, b, a);
                }
            }
            case 6 -> { // CHRONOS
                for (int i = 0; i < 12; i++) {
                    float angle1 = (float) Math.toRadians(i * 30);
                    float angle2 = (float) Math.toRadians(i * 30 + 15);
                    drawLine(builder, matrix,
                            Mth.cos(angle1) * size, Mth.sin(angle1) * size,
                            Mth.cos(angle2) * size, Mth.sin(angle2) * size,
                            r, g, b, a);
                }
                for (int i = 0; i < 4; i++) {
                    float angle = (float) Math.toRadians(i * 90);
                    float xOuter = Mth.cos(angle) * (size * 0.8f);
                    float yOuter = Mth.sin(angle) * (size * 0.8f);
                    float xInner = Mth.cos(angle) * (size * 0.4f);
                    float yInner = Mth.sin(angle) * (size * 0.4f);
                    drawLine(builder, matrix, xOuter, yOuter, xInner, yInner, r, g, b, a * 0.7f);
                }
            }

            case 7 -> {

                for (int i = 0; i < 8; i++) {
                    float a1 = (float) Math.toRadians(i * 45 + 10);
                    float a2 = (float) Math.toRadians(i * 45 + 35);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * size * 1.1f, Mth.sin(a1) * size * 1.1f,
                            Mth.cos(a2) * size * 1.1f, Mth.sin(a2) * size * 1.1f,
                            r, g, b, a * 0.6f);
                }

                for (int i = 0; i < 12; i++) {
                    float a1 = (float) Math.toRadians(i * 30);
                    float a2 = (float) Math.toRadians(i * 30 + 20);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * size * 0.5f, Mth.sin(a1) * size * 0.5f,
                            Mth.cos(a2) * size * 0.5f, Mth.sin(a2) * size * 0.5f,
                            r, g, b, a * 0.8f);
                }

                float outerRay = size * 1.3f;
                float innerRay = size * 0.7f;
                for (int i = 0; i < 4; i++) {
                    float angle = (float) Math.toRadians(i * 90);
                    drawLine(builder, matrix,
                            Mth.cos(angle) * innerRay, Mth.sin(angle) * innerRay,
                            Mth.cos(angle) * outerRay, Mth.sin(angle) * outerRay,
                            r, g, b, a);
                }

                float shortOuter = size * 0.9f;
                float shortInner = size * 0.55f;
                for (int i = 0; i < 4; i++) {
                    float angle = (float) Math.toRadians(i * 90 + 45);
                    drawLine(builder, matrix,
                            Mth.cos(angle) * shortInner, Mth.sin(angle) * shortInner,
                            Mth.cos(angle) * shortOuter, Mth.sin(angle) * shortOuter,
                            r, g, b, a * 0.7f);
                }

                float d2 = 0.035F;
                drawLine(builder, matrix, -d2, -d2, d2, -d2, r, g, b, a);
                drawLine(builder, matrix, d2, -d2, d2, d2, r, g, b, a);
                drawLine(builder, matrix, d2, d2, -d2, d2, r, g, b, a);
                drawLine(builder, matrix, -d2, d2, -d2, -d2, r, g, b, a);
            }
            case 8 -> {

                for (int i = 0; i < 16; i++) {
                    float a1 = (float) Math.toRadians(i * 22.5);
                    float a2 = (float) Math.toRadians((i + 1) * 22.5);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * size, Mth.sin(a1) * size,
                            Mth.cos(a2) * size, Mth.sin(a2) * size,
                            r, g, b, a);
                }

                float dotR = size * 1.15f;
                float dot = 0.015F;
                for (int i = 0; i < 12; i++) {
                    float angle = (float) Math.toRadians(i * 30);
                    float dx = Mth.cos(angle) * dotR;
                    float dy = Mth.sin(angle) * dotR;
                    drawLine(builder, matrix, dx - dot, dy, dx + dot, dy, r, g, b, a);
                    drawLine(builder, matrix, dx, dy - dot, dx, dy + dot, r, g, b, a);
                }

                float gap = size * 0.25f;  // Merkeze boşluk
                float ext = size * 0.85f;  // Dışa uzanma
                drawLine(builder, matrix, gap, gap, ext, ext, r, g, b, a);
                drawLine(builder, matrix, -gap, gap, -ext, ext, r, g, b, a);
                drawLine(builder, matrix, gap, -gap, ext, -ext, r, g, b, a);
                drawLine(builder, matrix, -gap, -gap, -ext, -ext, r, g, b, a);

                float boxR = size * 0.45f;
                float bS = 0.018F;
                for (int i = 0; i < 4; i++) {
                    float angle = (float) Math.toRadians(i * 90);
                    float bx = Mth.cos(angle) * boxR;
                    float by = Mth.sin(angle) * boxR;
                    drawLine(builder, matrix, bx - bS, by - bS, bx + bS, by - bS, r, g, b, a * 0.9f);
                    drawLine(builder, matrix, bx + bS, by - bS, bx + bS, by + bS, r, g, b, a * 0.9f);
                    drawLine(builder, matrix, bx + bS, by + bS, bx - bS, by + bS, r, g, b, a * 0.9f);
                    drawLine(builder, matrix, bx - bS, by + bS, bx - bS, by - bS, r, g, b, a * 0.9f);
                }
            }
            case 9 -> {

                float cutLen = size * 1.1f;
                float cutStart = size * 0.15f;

                drawLine(builder, matrix, -cutLen, -cutLen, -cutStart, -cutStart, r, g, b, a);
                drawLine(builder, matrix, cutStart, cutStart, cutLen, cutLen, r, g, b, a);
                drawLine(builder, matrix, cutLen, -cutLen, cutStart, -cutStart, r, g, b, a);
                drawLine(builder, matrix, -cutStart, cutStart, -cutLen, cutLen, r, g, b, a);

                float dropR = 0.025F;
                float[][] dropPoints = {
                        {-cutLen, -cutLen}, {cutLen, cutLen},
                        {cutLen, -cutLen}, {-cutLen, cutLen}
                };
                for (float[] p : dropPoints) {
                    drawLine(builder, matrix, p[0] - dropR, p[1], p[0] + dropR, p[1], r, g, b, a);
                    drawLine(builder, matrix, p[0], p[1] - dropR, p[0], p[1] + dropR, r, g, b, a);
                    // Çapraz damla detayı
                    drawLine(builder, matrix, p[0] - dropR * 0.7f, p[1] - dropR * 0.7f,
                            p[0] + dropR * 0.7f, p[1] + dropR * 0.7f, r, g, b, a * 0.8f);
                }

                float frame = size * 1.3f;
                float cut = size * 0.35f;

                drawLine(builder, matrix, -frame, -frame, -frame + cut, -frame, r, g, b, a * 0.5f);
                drawLine(builder, matrix, frame - cut, -frame, frame, -frame, r, g, b, a * 0.5f);

                drawLine(builder, matrix, -frame, frame, -frame + cut, frame, r, g, b, a * 0.5f);
                drawLine(builder, matrix, frame - cut, frame, frame, frame, r, g, b, a * 0.5f);
                // Sol kenar
                drawLine(builder, matrix, -frame, -frame, -frame, -frame + cut, r, g, b, a * 0.5f);
                drawLine(builder, matrix, -frame, frame - cut, -frame, frame, r, g, b, a * 0.5f);
                // Sağ kenar
                drawLine(builder, matrix, frame, -frame, frame, -frame + cut, r, g, b, a * 0.5f);
                drawLine(builder, matrix, frame, frame - cut, frame, frame, r, g, b, a * 0.5f);

                float triR = size * 0.2f;
                for (int i = 0; i < 3; i++) {
                    float a1 = (float) Math.toRadians(i * 120 - 90);
                    float a2 = (float) Math.toRadians((i + 1) * 120 - 90);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * triR, Mth.sin(a1) * triR,
                            Mth.cos(a2) * triR, Mth.sin(a2) * triR,
                            r, g, b, a * 0.6f);
                }
            }

            case 10 -> { // DEATH SKULL
                float s = size * 1.2f; // Genel ölçek

                for (int i = 0; i < 8; i++) {
                    float a1 = (float) Math.toRadians(180 + i * 22.5);
                    float a2 = (float) Math.toRadians(180 + (i + 1) * 22.5);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * s, Mth.sin(a1) * s * 0.95f - s * 0.1f,
                            Mth.cos(a2) * s, Mth.sin(a2) * s * 0.95f - s * 0.1f,
                            r, g, b, a);
                }


                // Sol yanak
                drawLine(builder, matrix, -s * 0.95f, -s * 0.1f, -s * 0.85f, s * 0.3f, r, g, b, a);
                drawLine(builder, matrix, -s * 0.85f, s * 0.3f, -s * 0.55f, s * 0.55f, r, g, b, a);
                // Sağ yanak
                drawLine(builder, matrix, s * 0.95f, -s * 0.1f, s * 0.85f, s * 0.3f, r, g, b, a);
                drawLine(builder, matrix, s * 0.85f, s * 0.3f, s * 0.55f, s * 0.55f, r, g, b, a);

                float eyeW = s * 0.28f;
                float eyeH = s * 0.25f;
                float eyeY = -s * 0.05f;
                float eyeX = s * 0.38f;
                // Sol göz (hafif aşağı eğik)
                drawLine(builder, matrix, -eyeX - eyeW, eyeY - eyeH * 0.8f, -eyeX + eyeW, eyeY - eyeH, r, g, b, a);
                drawLine(builder, matrix, -eyeX + eyeW, eyeY - eyeH, -eyeX + eyeW, eyeY + eyeH * 0.7f, r, g, b, a);
                drawLine(builder, matrix, -eyeX + eyeW, eyeY + eyeH * 0.7f, -eyeX - eyeW, eyeY + eyeH * 0.9f, r, g, b, a);
                drawLine(builder, matrix, -eyeX - eyeW, eyeY + eyeH * 0.9f, -eyeX - eyeW, eyeY - eyeH * 0.8f, r, g, b, a);
                // Sağ göz (ayna)
                drawLine(builder, matrix, eyeX + eyeW, eyeY - eyeH * 0.8f, eyeX - eyeW, eyeY - eyeH, r, g, b, a);
                drawLine(builder, matrix, eyeX - eyeW, eyeY - eyeH, eyeX - eyeW, eyeY + eyeH * 0.7f, r, g, b, a);
                drawLine(builder, matrix, eyeX - eyeW, eyeY + eyeH * 0.7f, eyeX + eyeW, eyeY + eyeH * 0.9f, r, g, b, a);
                drawLine(builder, matrix, eyeX + eyeW, eyeY + eyeH * 0.9f, eyeX + eyeW, eyeY - eyeH * 0.8f, r, g, b, a);

                float noseTop = s * 0.2f;
                float noseBot = s * 0.38f;
                float noseW = s * 0.12f;
                drawLine(builder, matrix, 0, noseTop, -noseW, noseBot, r, g, b, a);
                drawLine(builder, matrix, 0, noseTop, noseW, noseBot, r, g, b, a);
                drawLine(builder, matrix, -noseW, noseBot, noseW, noseBot, r, g, b, a);
                // Burun bölmesi (dikey çizgi)
                drawLine(builder, matrix, 0, noseTop + s * 0.05f, 0, noseBot, r, g, b, a * 0.5f);

                float jawY = s * 0.55f;
                float jawBot = s * 0.8f;
                float jawW = s * 0.55f;
                // Çene dış çerçevesi
                drawLine(builder, matrix, -jawW, jawY, -jawW, jawBot, r, g, b, a);
                drawLine(builder, matrix, jawW, jawY, jawW, jawBot, r, g, b, a);
                drawLine(builder, matrix, -jawW, jawBot, jawW, jawBot, r, g, b, a);
                // Üst dudak çizgisi
                drawLine(builder, matrix, -jawW, jawY, jawW, jawY, r, g, b, a * 0.7f);

                float toothW = jawW * 2.0f / 5.0f;
                for (int i = 1; i < 5; i++) {
                    float tx = -jawW + toothW * i;
                    drawLine(builder, matrix, tx, jawY + s * 0.02f, tx, jawBot - s * 0.02f, r, g, b, a * 0.6f);
                }

                drawLine(builder, matrix, -s * 0.1f, -s * 0.85f, 0, -s * 0.55f, r, g, b, a * 0.5f);
                drawLine(builder, matrix, 0, -s * 0.55f, s * 0.15f, -s * 0.4f, r, g, b, a * 0.5f);
                drawLine(builder, matrix, s * 0.15f, -s * 0.4f, s * 0.05f, -s * 0.25f, r, g, b, a * 0.5f);
            }

            case 11 -> { // BRS SIGIL — Runik koruma sembolü
                float s = size * 1.1f;

                // Dış üçgen
                float triOuter = s * 0.55f;
                for (int i = 0; i < 3; i++) {
                    float a1 = (float) Math.toRadians(i * 120 - 90);
                    float a2 = (float) Math.toRadians((i + 1) * 120 - 90);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * triOuter, Mth.sin(a1) * triOuter,
                            Mth.cos(a2) * triOuter, Mth.sin(a2) * triOuter,
                            r, g, b, a);
                }
                // İç üçgen
                float triInner = s * 0.30f;
                for (int i = 0; i < 3; i++) {
                    float a1 = (float) Math.toRadians(i * 120 - 90);
                    float a2 = (float) Math.toRadians((i + 1) * 120 - 90);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * triInner, Mth.sin(a1) * triInner,
                            Mth.cos(a2) * triInner, Mth.sin(a2) * triInner,
                            r, g, b, a * 0.8f);
                }

                // 2. BOŞLUKLU ARTI (ortada birbirine değmeyen 4 kol)
                float gap = s * 0.08f;     // merkezdeki boşluk
                float armLen = s * 0.22f;  // kol uzunluğu
                float armW = s * 0.04f;    // kol kalınlığı

                // Üst kol
                drawLine(builder, matrix, -armW, -(gap), armW, -(gap), r, g, b, a);
                drawLine(builder, matrix, armW, -(gap), armW, -(gap + armLen), r, g, b, a);
                drawLine(builder, matrix, armW, -(gap + armLen), -armW, -(gap + armLen), r, g, b, a);
                drawLine(builder, matrix, -armW, -(gap + armLen), -armW, -(gap), r, g, b, a);
                // Alt kol
                drawLine(builder, matrix, -armW, gap, armW, gap, r, g, b, a);
                drawLine(builder, matrix, armW, gap, armW, gap + armLen, r, g, b, a);
                drawLine(builder, matrix, armW, gap + armLen, -armW, gap + armLen, r, g, b, a);
                drawLine(builder, matrix, -armW, gap + armLen, -armW, gap, r, g, b, a);
                // Sol kol
                drawLine(builder, matrix, -gap, -armW, -gap, armW, r, g, b, a);
                drawLine(builder, matrix, -gap, armW, -(gap + armLen), armW, r, g, b, a);
                drawLine(builder, matrix, -(gap + armLen), armW, -(gap + armLen), -armW, r, g, b, a);
                drawLine(builder, matrix, -(gap + armLen), -armW, -gap, -armW, r, g, b, a);
                // Sağ kol
                drawLine(builder, matrix, gap, -armW, gap, armW, r, g, b, a);
                drawLine(builder, matrix, gap, armW, gap + armLen, armW, r, g, b, a);
                drawLine(builder, matrix, gap + armLen, armW, gap + armLen, -armW, r, g, b, a);
                drawLine(builder, matrix, gap + armLen, -armW, gap, -armW, r, g, b, a);

                // 3. ARTININ UÇLARINI BİRLEŞTİREN BÜYÜK ÜÇGEN
                float bigTri = s * 0.85f;
                for (int i = 0; i < 3; i++) {
                    float a1 = (float) Math.toRadians(i * 120 - 90);
                    float a2 = (float) Math.toRadians((i + 1) * 120 - 90);
                    drawLine(builder, matrix,
                            Mth.cos(a1) * bigTri, Mth.sin(a1) * bigTri,
                            Mth.cos(a2) * bigTri, Mth.sin(a2) * bigTri,
                            r, g, b, a * 0.75f);
                }

                // 4. İÇ İÇE 2 KARE (biri dik, biri 45° döndürülmüş — elmas)
                float sqSize = s * 1.0f;
                // Dik kare
                drawLine(builder, matrix, -sqSize, -sqSize, sqSize, -sqSize, r, g, b, a * 0.55f);
                drawLine(builder, matrix, sqSize, -sqSize, sqSize, sqSize, r, g, b, a * 0.55f);
                drawLine(builder, matrix, sqSize, sqSize, -sqSize, sqSize, r, g, b, a * 0.55f);
                drawLine(builder, matrix, -sqSize, sqSize, -sqSize, -sqSize, r, g, b, a * 0.55f);
                // 45° döndürülmüş kare (elmas)
                drawLine(builder, matrix, 0, -sqSize, sqSize, 0, r, g, b, a * 0.55f);
                drawLine(builder, matrix, sqSize, 0, 0, sqSize, r, g, b, a * 0.55f);
                drawLine(builder, matrix, 0, sqSize, -sqSize, 0, r, g, b, a * 0.55f);
                drawLine(builder, matrix, -sqSize, 0, 0, -sqSize, r, g, b, a * 0.55f);
            }


        }

        // Ortak merkez noktası
        if (style != 11) {
            float d = 0.02F;
            drawLine(builder, matrix, -d, 0, d, 0, r, g, b, a);
            drawLine(builder, matrix, 0, -d, 0, d, r, g, b, a);
        }

        bufferSource.endBatch(RenderType.lines());
    }

    private static void drawLine(VertexConsumer builder, Matrix4f matrix,
                                 float x1, float y1, float x2, float y2,
                                 float r, float g, float b, float a) {
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }
}