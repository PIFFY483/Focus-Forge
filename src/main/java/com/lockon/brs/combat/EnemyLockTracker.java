package com.lockon.brs.combat;

import com.lockon.LockOnMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID)
public class EnemyLockTracker {

    private static final String LOCK_TAG = "brs_locked_on_player";

    /** oyuncu UUID -> o oyuncuya şu an kilitli mob UUID'leri */
    private static final Map<UUID, Set<UUID>> LOCKED_ON = new ConcurrentHashMap<>();

    private EnemyLockTracker() {
    }

    /** true ise bu oyuncuya şu an kilitlenmiş en az bir mob var. Tarama yok, direkt harita bakışı. */
    public static boolean isTargetedByEnemy(ServerPlayer player) {
        Set<UUID> mobs = LOCKED_ON.get(player.getUUID());
        return mobs != null && !mobs.isEmpty();
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        LivingEntity oldTarget = mob.getTarget();
        LivingEntity newTarget = event.getNewTarget();

        // Eski hedef bir oyuncuysa ve artık hedef değilse kilidi çöz
        if (oldTarget instanceof ServerPlayer oldPlayer) {
            unlock(oldPlayer.getUUID(), mob);
        }

        // Yeni hedef bir oyuncuysa kilitle
        if (newTarget instanceof ServerPlayer newPlayer) {
            lock(newPlayer.getUUID(), mob);
        }
    }

    // Mob öldüğünde kilidi tamamen temizle
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            clearMobEverywhere(mob);
        }
    }

    // Mob level'dan ayrıldığında (chunk unload, boyut değişimi, despawn vb.) temizle
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            clearMobEverywhere(mob);
        }
    }

    private static void lock(UUID playerId, Mob mob) {
        LOCKED_ON.computeIfAbsent(playerId, id -> ConcurrentHashMap.newKeySet()).add(mob.getUUID());
        mob.addTag(LOCK_TAG);
    }

    private static void unlock(UUID playerId, Mob mob) {
        Set<UUID> mobs = LOCKED_ON.get(playerId);
        if (mobs != null) {
            mobs.remove(mob.getUUID());
            if (mobs.isEmpty()) {
                LOCKED_ON.remove(playerId, mobs);
            }
        }
        mob.removeTag(LOCK_TAG);
    }

    /** Bu mob'u, kilitli olduğu HERHANGİ bir oyuncunun listesinden çıkar. */
    private static void clearMobEverywhere(Mob mob) {
        UUID mobId = mob.getUUID();
        for (Set<UUID> mobs : LOCKED_ON.values()) {
            mobs.remove(mobId);
        }
        LOCKED_ON.values().removeIf(Set::isEmpty);
        mob.removeTag(LOCK_TAG);
    }
}