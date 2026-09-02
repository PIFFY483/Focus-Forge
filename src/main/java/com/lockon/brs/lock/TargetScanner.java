package com.lockon.brs.lock;

import com.lockon.brs.config.LockOnConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class TargetScanner {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Map<UUID, Long> ignoredTargets = new HashMap<>();
    private static final TagKey<Block> PASS_THROUGH_BLOCKS_TAG =
            TagKey.create(Registries.BLOCK, new ResourceLocation("brs", "pass_through"));

    private static Set<String> acquisitionCache = new HashSet<>();
    private static Set<String> preclusionCache = new HashSet<>();
    private static Set<String> entityBlacklistCache = new HashSet<>();
    private static boolean isCacheInitialized = false;

    public static void refreshCache() {
        acquisitionCache = new HashSet<>((List<String>) LockOnConfig.LOCK_ACQUISITION_BLOCK_LIST.get());
        preclusionCache = new HashSet<>((List<String>) LockOnConfig.LOCK_PRECLUSION_BLOCK_LIST.get());
        entityBlacklistCache = new HashSet<>((List<String>) LockOnConfig.TARGET_BLACKLIST.get());
        isCacheInitialized = true;
    }

    public static LivingEntity findTarget() {
        return findTarget(null);
    }

    public static LivingEntity findTarget(LivingEntity targetToIgnore) {
        LocalPlayer player = mc.player;
        if (player == null) return null;

        if (!isCacheInitialized) refreshCache();

        double maxDist = LockOnConfig.MAX_LOCK_DISTANCE.get();
        double maxAngle = LockOnConfig.MAX_LOCK_ANGLE.get() + LockOnConfig.MAX_VERTICAL_OFFSET.get();

        AABB scanArea = player.getBoundingBox().inflate(maxDist);

        List<LivingEntity> potentialTargets = player.level().getEntitiesOfClass(
                LivingEntity.class, scanArea, (e) -> {
                    if (e == player || !e.isAlive()) return false;
                    if (LockOnConfig.ENABLE_TARGET_BLACKLIST.get() && isEntityBlacklisted(e)) return false;
                    if (e instanceof Player && !LockOnConfig.TARGET_PLAYERS.get()) return false;
                    if (targetToIgnore != null && e.getUUID().equals(targetToIgnore.getUUID())) return false;
                    if (ignoredTargets.containsKey(e.getUUID())) {
                        double cooldown = LockOnConfig.TARGET_SWITCH_COOLDOWN_SECONDS.get();
                        if (System.currentTimeMillis() - ignoredTargets.get(e.getUUID()) < (cooldown * 1000L))
                            return false;
                    }
                    return true;
                });

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity entity : potentialTargets) {
            double distance = player.distanceTo(entity);
            double angleDiff = getAngleDifference(player, entity);

            if (distance <= maxDist && angleDiff <= maxAngle) {
                if (checkLineOfSight(player, entity, LockOnConfig.CAMERA_FOCUS_OFFSET.get())) {
                    double currentScore = distance + (angleDiff * 2.5);
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestTarget = entity;
                    }
                }
            }
        }

        return bestTarget;
    }

    public static LivingEntity findReacquireTarget(LivingEntity deadTarget) {
        LocalPlayer player = mc.player;
        if (player == null) return null;
        if (!isCacheInitialized) refreshCache();

        final double REACQUIRE_HALF_ANGLE = 90.0; // ±90° = 180° ön alan
        double maxDist = LockOnConfig.MAX_LOCK_DISTANCE.get();

        AABB scanArea = player.getBoundingBox().inflate(maxDist);

        List<LivingEntity> potentialTargets = player.level().getEntitiesOfClass(
                LivingEntity.class, scanArea, (e) -> {
                    if (e == player || !e.isAlive()) return false;
                    if (deadTarget != null && e.getUUID().equals(deadTarget.getUUID())) return false;
                    if (LockOnConfig.ENABLE_TARGET_BLACKLIST.get() && isEntityBlacklisted(e)) return false;
                    if (e instanceof Player && !LockOnConfig.TARGET_PLAYERS.get()) return false;
                    return true;
                });

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : potentialTargets) {
            double distance = player.distanceTo(entity);
            if (distance > maxDist || distance >= nearestDistance) continue;

            double angleDiff = getAngleDifference(player, entity);
            if (angleDiff > REACQUIRE_HALF_ANGLE) continue;

            if (!checkLineOfSight(player, entity, LockOnConfig.CAMERA_FOCUS_OFFSET.get())) continue;

            nearest = entity;
            nearestDistance = distance;
        }

        return nearest;
    }

    private static boolean performRayTrace(LocalPlayer player, LivingEntity target,
                                           double verticalOffset, Set<String> cache) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 targetPos = new Vec3(target.getX(),
                target.getY() + target.getEyeHeight() + verticalOffset, target.getZ());

        HitResult result = player.level().clip(new ClipContext(
                start, targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));

        if (result.getType() == HitResult.Type.MISS) return true;

        if (result instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState hitState = player.level().getBlockState(pos);
            return hitState.getBlock().builtInRegistryHolder().is(PASS_THROUGH_BLOCKS_TAG)
                    || isBlockInCache(hitState.getBlock(), cache);
        }
        return true;
    }

    private static boolean checkLineOfSight(LocalPlayer player, LivingEntity target, double verticalOffset) {
        return performRayTrace(player, target, verticalOffset, acquisitionCache);
    }

    private static boolean isBlockInCache(Block block, Set<String> cache) {
        ResourceLocation blockRl = BuiltInRegistries.BLOCK.getKey(block);
        if (blockRl == null) return false;
        String blockId = blockRl.toString();
        if (cache.contains(blockId)) return true;

        for (String entry : cache) {
            if (entry.startsWith("#")) {
                ResourceLocation tagRl = ResourceLocation.tryParse(entry.substring(1));
                if (tagRl != null &&
                        block.builtInRegistryHolder().is(TagKey.create(Registries.BLOCK, tagRl))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEntityBlacklisted(LivingEntity entity) {
        ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return rl != null && entityBlacklistCache.contains(rl.toString());
    }

    private static double getAngleDifference(LocalPlayer player, LivingEntity target) {
        double dX = target.getX() - player.getX();
        double dZ = target.getZ() - player.getZ();
        double targetYaw = Mth.wrapDegrees(
                (float) (Mth.atan2(dZ, dX) * (180F / Math.PI)) - 90.0F);
        float playerYaw = Mth.wrapDegrees(player.getYRot());
        return Math.abs(Mth.wrapDegrees(targetYaw - playerYaw));
    }

    public static void markTargetIgnored(LivingEntity target) {
        if (target != null)
            ignoredTargets.put(target.getUUID(), System.currentTimeMillis());
    }

    public static void resetIgnoredTargets() {
        ignoredTargets.clear();
    }

    public static boolean isTargetStillValid(LivingEntity target) {
        LocalPlayer player = mc.player;
        if (player == null || !target.isAlive() || target.level() != player.level()) return false;

        // 1. MESAFE KONTROLÜ
        if (player.distanceTo(target) > LockOnConfig.MAX_DISENGAGEMENT_RANGE.get()) return false;

        // 2. KÜRESEL TUTMA MANTIĞI
        if (!LockState.isLocked()) {
            double currentAngle = getAngleDifference(player, target);
            double maxAllowedAngle = LockOnConfig.MAX_LOCK_ANGLE.get() + LockOnConfig.MAX_VERTICAL_OFFSET.get();
            if (currentAngle > maxAllowedAngle) return false;
        }

        if (LockOnConfig.BREAK_LOCK_ON_LOS_BREAK.get()) {
            if (!performRayTrace(player, target,
                    LockOnConfig.CAMERA_FOCUS_OFFSET.get(), preclusionCache)) {
                return false;
            }
        }

        return true;
    }
}