package com.lockon.brs.camera;

import com.lockon.LockOnMod;
import com.lockon.camera.ShoulderCamMode;

import com.lockon.brs.config.CameraConfig;
import com.lockon.brs.config.LockOnConfig;
import com.lockon.brs.lock.LockState;
import com.lockon.client.CrosshairHandler;
import com.lockon.config.CameraViewConfig;
import com.lockon.util.CameraMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VirtualCameraHandler {

    private static final TagKey<Block> PASS_THROUGH_BLOCKS_TAG =
            TagKey.create(Registries.BLOCK, new ResourceLocation("brs", "camera_pass_through"));

    private static Vec3 lerpPos = Vec3.ZERO;

    // ── DUVAR ÇARPIŞMASI SONRASI YUMUŞAK TOPARLANMA ──
    private static final SmoothDamp.State1D collisionDistanceState = new SmoothDamp.State1D();

    // ── FPV ↔ TPV SMOOTH TRANSITION STATE ──
    private static float transitionProgress = 0.0f; // 0 = FPV, 1 = TPV
    private static CameraType lastCameraType = CameraType.FIRST_PERSON;
    private static boolean transitioningToFpv = false;

    // ── Saf FPV'de shake'in pozisyon değil açı-jitter'ı olarak uygulanma çarpanı ──
    private static final float FPV_SHAKE_ANGLE_SCALE = 6.0f;

    // ── NİŞAN ALMA KAMERA KAYMASI (Parallax Assist ile uyumlu) ──
    // Elde yay/arbalet/mızrak gibi nişan alınabilen bir silah varken New Camera'nın
    // omuz offseti azaltılır; kamera ekranın ortasına doğru "biraz" kayar, böylece
    // gerçek atış yönü (bkz. ProjectileMixin/ServerLevelMixin) çizilen crosshair'le
    // (bkz. CrosshairHandler) daha tutarlı hizalanır. 0 = normal omuz pozisyonu,
    // 1 = tam hedef kayma miktarı (bkz. AIM_ASSIST_SHIFT_STRENGTH).
    private static float aimAssistShift = 0.0f;
    private static final float AIM_ASSIST_SHIFT_STRENGTH = 0.5f; // side offsetin en fazla %50'si kadar merkeze kayar
    private static final float AIM_ASSIST_SHIFT_SMOOTHING = 0.12f; // per-frame lerp faktörü

    /**
     * Geçiş animasyonu aktif mi?
     */
    public static boolean isTransitioning() {
        return transitioningToFpv || transitionProgress > 0.01f;
    }

    public static float getTransitionProgress() {
        return transitionProgress;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!ShoulderCamMode.isNew()) return;
        if (isTransitioning()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (!ShoulderCamMode.isNew()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Camera camera = event.getCamera();
        float pt = (float) event.getPartialTick();

        // ── HER ZAMAN taze göz pozisyonu ──
        // camera.getPosition() detached modda bir önceki custom pozisyonu döndürdüğü
        // için (feedback loop / drift bugu), göz pozisyonunu doğrudan player'dan
        // hesaplıyoruz. Bu değer metodun geri kalanında da (shoulder/orbit) kullanılıyor.
        double px = Mth.lerp(pt, mc.player.xo, mc.player.getX());
        double py = Mth.lerp(pt, mc.player.yo, mc.player.getY()) + mc.player.getEyeHeight();
        double pz = Mth.lerp(pt, mc.player.zo, mc.player.getZ());
        Vec3 eyePos = new Vec3(px, py, pz);

        // ── Screen Shake: kamera modundan bağımsız, en başta hesapla ──
        boolean shakeActive = ScreenShakeController.isActive();
        Vec3 shakeOffset = shakeActive
                ? new Vec3(ScreenShakeController.getOffsetX(), ScreenShakeController.getOffsetY(), 0.0)
                : Vec3.ZERO;
        float shakeRoll = shakeActive ? ScreenShakeController.getRollOffset() : 0.0f;

        // ── Temel kontroller (saf FPV / shoulder cam kapalı) ──
        if (!CameraConfig.ENABLE_SHOULDER_CAM.get()) {
            lerpPos = Vec3.ZERO;
            OrbitCameraState.reset();
            transitionProgress = 0.0f;
            transitioningToFpv = false;
            lastCameraType = mc.options.getCameraType();
            collisionDistanceState.initialized = false;
            PlayerTransparencyController.reset();

            // ── FPV'de HER ZAMAN detached=false, vanilla self-render culling korunsun ──
            // (detached=true + neredeyse eyePos'a eşit pozisyon = motor 3rd person sanıp
            //  kafa modelini render ediyor, kamera "kafanın içine giriyor" hissi veriyordu)
            if (camera instanceof CameraMixinInterface mixinCamera) {
                mixinCamera.brs$setDetached(false);
            }

            // ── Shake'i pozisyon değil, açı jitter'ı olarak uygula ──
            if (shakeActive) {
                float yawPunch = (float) shakeOffset.x * FPV_SHAKE_ANGLE_SCALE;
                float pitchPunch = (float) shakeOffset.y * FPV_SHAKE_ANGLE_SCALE;
                event.setYaw(event.getYaw() + yawPunch);
                event.setPitch(event.getPitch() + pitchPunch);
                event.setRoll(shakeRoll);
            }
            return;
        }

        float frameDelta = mc.getDeltaFrameTime();
        CameraType currentType = mc.options.getCameraType();

        if (lastCameraType != CameraType.FIRST_PERSON && currentType == CameraType.FIRST_PERSON) {
            transitioningToFpv = true;
            if (OrbitCameraState.isOrbitActive()) {
                // Anında reset atmak yerine Orbit'ten Omuz pozisyonuna yumuşak dönüş başlat
                OrbitCameraState.requestExit();
            }
        }
        lastCameraType = currentType;

        boolean waitingForOrbitExit = currentType.isFirstPerson() && OrbitCameraState.isOrbitActive();
        float targetProgress = (currentType.isFirstPerson() && !waitingForOrbitExit) ? 0.0f : 1.0f;

        // Smooth transition kapalıysa anında snap
        if (!CameraRig.enableSmoothTransition) {
            transitionProgress = targetProgress;
            transitioningToFpv = false;
        } else {
            // Frame-rate bağımsız smooth transition
            float speed = (float) CameraRig.transitionSpeed;
            float frameLerp = 1.0f - (float) Math.pow(1.0 - Mth.clamp(speed, 0.001f, 0.999f), frameDelta);
            transitionProgress = Mth.lerp(frameLerp, transitionProgress, targetProgress);

            if (Math.abs(transitionProgress - targetProgress) < 0.001f) {
                transitionProgress = targetProgress;
            }
        }

        if (transitionProgress < 0.01f) {
            transitionProgress = 0.0f;
            transitioningToFpv = false;
        }

        // ── FPV'ye yakın / orbit kapalı durum ──
        if (transitionProgress < 0.01f && !OrbitCameraState.isOrbitActive() && !transitioningToFpv) {
            lerpPos = Vec3.ZERO;
            collisionDistanceState.initialized = false;
            PlayerTransparencyController.reset();

            if (camera instanceof CameraMixinInterface mixinCamera) {
                mixinCamera.brs$setDetached(false);
            }

            if (shakeActive) {
                float yawPunch = (float) shakeOffset.x * FPV_SHAKE_ANGLE_SCALE;
                float pitchPunch = (float) shakeOffset.y * FPV_SHAKE_ANGLE_SCALE;
                event.setYaw(event.getYaw() + yawPunch);
                event.setPitch(event.getPitch() + pitchPunch);
                event.setRoll(shakeRoll);
            }
            return;
        }

        // ── Orbit geçişlerini güncelle ──
        OrbitCameraState.updateTransition(frameDelta,
                (float) CameraRig.orbitTransitionSpeed);
        OrbitCameraState.updateAutoRotate(frameDelta,
                (float) (double) CameraRig.orbitAutoRotateSpeed);

        double baseDistance = CameraConfig.CAMERA_DISTANCE.get();
        double effectiveShoulderDistance = CameraStateManager.computeAndSmoothLockDistance(
                mc, baseDistance, frameDelta);

        double lockDistanceDelta = effectiveShoulderDistance - baseDistance;

        float shoulderYaw, shoulderPitch;
        if (LockState.isLocked() && CameraStateManager.lockedTarget != null) {
            shoulderYaw = CameraStateManager.getCameraYaw();
            shoulderPitch = CameraStateManager.getCameraPitch();
        } else {
            shoulderYaw = camera.getYRot();
            shoulderPitch = camera.getXRot();
        }

        // ── Nişan alma kamera kayması: Parallax Assist açıkken ve elde nişan
        // alınabilen bir silah varken hedefi 1'e, aksi halde 0'a yumuşakça çeker ──
        boolean aimAssistActive = !LockState.isLocked()
                && CameraViewConfig.CLIENT.enableParallaxAssist.get()
                && !currentType.isFirstPerson()
                && CrosshairHandler.isAimableHeld(mc.player);
        aimAssistShift = Mth.lerp(AIM_ASSIST_SHIFT_SMOOTHING, aimAssistShift, aimAssistActive ? 1.0f : 0.0f);

        Vec3 shoulderPos = calculateShoulderPosition(mc, shoulderYaw, shoulderPitch, eyePos, transitionProgress,
                effectiveShoulderDistance, lockDistanceDelta, aimAssistShift);
        Vec3 orbitPos = calculateOrbitPosition(eyePos);

        // ── Orbit geçiş lerp'i (smoothstep easing ile) ──
        float t = OrbitCameraState.getEasedProgress();
        Vec3 targetPos = new Vec3(
                Mth.lerp(t, shoulderPos.x, orbitPos.x),
                Mth.lerp(t, shoulderPos.y, orbitPos.y),
                Mth.lerp(t, shoulderPos.z, orbitPos.z)
        );

        Vec3 camDir = targetPos.subtract(eyePos);
        double fullDistance = camDir.length();
        double allowedDistance = computeProbedAllowedDistance(mc, eyePos, targetPos, fullDistance);

        float deltaSeconds = frameDelta / 20.0f;

        if (!collisionDistanceState.initialized) {
            collisionDistanceState.snapTo((float) allowedDistance);
        } else if (collisionDistanceState.value > allowedDistance + 0.001f) {

            float enterSmoothTime = 0.02f;
            float enterMaxSpeedPerSecond = 60.0f; // blok/sn — pratikte 1 frame'de yakalar
            SmoothDamp.smoothDamp(collisionDistanceState, (float) allowedDistance,
                    enterSmoothTime, enterMaxSpeedPerSecond, deltaSeconds);
        } else if (collisionDistanceState.value < allowedDistance - 0.001f) {
            // ── UZAKLAŞMA (toparlanma) ──
            float recSpeed = Mth.clamp(CameraConfig.COLLISION_RECOVERY_SPEED.get().floatValue(), 0.01f, 1.0f);
            float smoothTime = 0.03f + 0.5f * (1.0f - recSpeed);
            SmoothDamp.smoothDamp(collisionDistanceState, (float) allowedDistance, smoothTime,
                    Float.MAX_VALUE, deltaSeconds);
        }
        // else: zaten hedef mesafedeyiz

        if (fullDistance > 1.0e-4) {
            targetPos = eyePos.add(camDir.scale(collisionDistanceState.value / fullDistance));
        }

        // ── Yumuşak takip ──
        if (lerpPos.equals(Vec3.ZERO)) lerpPos = targetPos;
        double lerpFactor = CameraConfig.CAMERA_SMOOTHNESS.get();
        lerpPos = new Vec3(
                Mth.lerp(lerpFactor, lerpPos.x, targetPos.x),
                Mth.lerp(lerpFactor, lerpPos.y, targetPos.y),
                Mth.lerp(lerpFactor, lerpPos.z, targetPos.z)
        );

        if (OrbitCameraState.isOrbitActive() && t > 0.01f) {
            double dx = px - lerpPos.x;
            double dy = py - lerpPos.y;
            double dz = pz - lerpPos.z;
            float orbitYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0f);
            float orbitPitch = (float) (-Math.toDegrees(
                    Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));

            event.setYaw(Mth.rotLerp(t, event.getYaw(), orbitYaw));
            event.setPitch(Mth.lerp(t, event.getPitch(), orbitPitch));
        }

        PlayerTransparencyController.update(lerpPos, eyePos, frameDelta);

        // ── Kameraya gönder (shake offset + roll dahil) ──
        if (camera instanceof CameraMixinInterface mixinCamera) {
            Vec3 finalPos = shakeActive ? lerpPos.add(shakeOffset) : lerpPos;
            mixinCamera.brs$setCustomPosition(finalPos);
            mixinCamera.brs$setDetached(true);
            if (shakeActive) {
                event.setRoll(shakeRoll);
            }
        }
    }

    private static double computeProbedAllowedDistance(Minecraft mc, Vec3 eyePos, Vec3 targetPos,
                                                       double fullDistance) {
        if (fullDistance <= 1.0e-4) return fullDistance;

        Vec3 dir = targetPos.subtract(eyePos).scale(1.0 / fullDistance);

        // Yöne dik iki eksen bul
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = dir.cross(worldUp);
        right = (right.lengthSqr() < 1.0e-6) ? new Vec3(1, 0, 0) : right.normalize();
        Vec3 up = right.cross(dir).normalize();

        final double PROBE_RADIUS = 0.14; // kamera "hacmi" yarıçapı (blok) — ~near plane payı
        final double MARGIN = 0.15;       // yüzeyden ekstra güvenlik boşluğu

        Vec3[] offsets = new Vec3[]{
                Vec3.ZERO,
                right.scale(PROBE_RADIUS),
                right.scale(-PROBE_RADIUS),
                up.scale(PROBE_RADIUS),
                up.scale(-PROBE_RADIUS)
        };

        double minAllowed = fullDistance;
        for (Vec3 off : offsets) {
            Vec3 probeStart = eyePos.add(off);
            Vec3 probeEnd = targetPos.add(off);
            HitResult hit = clipIgnoringPassThrough(mc, probeStart, probeEnd);
            if (hit.getType() != HitResult.Type.MISS) {
                double d = probeStart.distanceTo(hit.getLocation()) - MARGIN;
                if (d < minAllowed) minAllowed = d;
            }
        }

        return Math.max(0.0, minAllowed);
    }

    private static final int PASS_THROUGH_MAX_ITER = 6;

    private static HitResult clipIgnoringPassThrough(Minecraft mc, Vec3 start, Vec3 end) {
        Vec3 currentStart = start;

        for (int i = 0; i < PASS_THROUGH_MAX_ITER; i++) {
            HitResult hit = mc.level.clip(new ClipContext(
                    currentStart, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player));

            if (hit.getType() == HitResult.Type.MISS || !(hit instanceof BlockHitResult blockHit)) {
                return hit;
            }

            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            if (!state.getBlock().builtInRegistryHolder().is(PASS_THROUGH_BLOCKS_TAG)) {
                return hit; // Gerçek engel — burada dur
            }

            // İnce blok: hit noktasının hemen ötesinden taramaya devam et
            Vec3 dir = end.subtract(currentStart);
            double len = dir.length();
            if (len < 1.0e-4) return hit;
            currentStart = blockHit.getLocation().add(dir.scale(1.0e-3 / len));
        }

        return mc.level.clip(new ClipContext(
                currentStart, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player));
    }

    private static Vec3 calculateShoulderPosition(Minecraft mc, float yaw, float pitch, Vec3 eyePos,
                                                  float progress, double targetDistance,
                                                  double lockDistanceDelta, float aimAssistShift) {
        if (progress <= 0.001f) {
            return eyePos;
        }

        // Smoothstep easing
        float smoothProgress = progress * progress * (3.0f - 2.0f * progress);

        double minBack = Math.min(0.38, targetDistance);

        double back = Mth.lerp(smoothProgress, minBack, targetDistance);
        double side = CameraConfig.SHOULDER_OFFSET.get() * smoothProgress;
        // ── Nişan alma kayması: elde yay/arbalet/mızrak varken kamera ekranın
        // ortasına doğru "biraz" kayar (side offseti azaltılır), crosshair'in
        // gerçek atış yönüyle daha tutarlı hizalanması için ──
        side *= (1.0 - aimAssistShift * AIM_ASSIST_SHIFT_STRENGTH);
        double vert = CameraConfig.HEIGHT_OFFSET.get() * smoothProgress;

        // ── DİNAMİK Y YÜKSEKLİĞİ (GENEL OTO HİZALAMA) ──
        if (CameraConfig.ENABLE_DYNAMIC_Y_OFFSET.get()) {
            double extraDistance = Math.max(0.0, back - 2.0);
            vert += extraDistance * CameraConfig.DYNAMIC_Y_FACTOR.get();
        }


        if (LockOnConfig.ENABLE_LOCK_DISTANCE_Y_OFFSET.get() && lockDistanceDelta > 0.0) {
            vert += lockDistanceDelta * LockOnConfig.LOCK_DISTANCE_Y_FACTOR.get() * smoothProgress;
        }

        float yawRad = yaw * Mth.DEG_TO_RAD;

        float positionPitch = Mth.clamp(pitch, -20.0f, 40.0f);
        float positionPitchRad = positionPitch * Mth.DEG_TO_RAD;

        double offX = -Math.cos(yawRad) * side + Math.sin(yawRad) * Math.cos(positionPitchRad) * back;
        double offZ = -Math.sin(yawRad) * side - Math.cos(yawRad) * Math.cos(positionPitchRad) * back;
        double offY = vert + Math.sin(positionPitchRad) * back;

        return eyePos.add(offX, offY, offZ);
    }

    /**
     * Orbit pozisyon hesabı.
     */
    private static Vec3 calculateOrbitPosition(Vec3 eyePos) {
        float yaw = OrbitCameraState.getOrbitYaw();
        float pitch = OrbitCameraState.getOrbitPitch();
        float yawRad = yaw * Mth.DEG_TO_RAD;
        float pitchRad = pitch * Mth.DEG_TO_RAD;

        double distance = CameraRig.orbitDistance;
        double heightOff = CameraRig.orbitHeightOffset;

        double offX = Math.sin(yawRad) * Math.cos(pitchRad) * distance;
        double offY = Math.sin(pitchRad) * distance + heightOff;
        double offZ = -Math.cos(yawRad) * Math.cos(pitchRad) * distance;

        return eyePos.add(offX, offY, offZ);
    }
}