package com.lockon.brs.energy;

import com.lockon.LockOnMod;
import com.lockon.brs.combat.EnemyLockTracker;
import com.lockon.brs.network.BRSPackets;
import com.lockon.brs.network.EnergySyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID)
public class EnergyManager {

    // ── Ayarlanabilir değerler ──────────────────────────────────────────
    public static final float MAX_ENERGY = 120f;
    public static final float REGEN_PER_SECOND = 8f;
    // NOT: DASH_COST_* sabitleri ve dashCostFor() dash sistemiyle birlikte kaldırıldı.
    // ─────────────────────────────────────────────────────────────────

    private static final Map<UUID, Float> ENERGY = new ConcurrentHashMap<>();

    private EnergyManager() {
    }

    public static float get(ServerPlayer player) {
        return ENERGY.computeIfAbsent(player.getUUID(), id -> MAX_ENERGY);
    }

    public static boolean has(ServerPlayer player, float amount) {
        return get(player) >= amount;
    }

    public static void spend(ServerPlayer player, float amount) {
        set(player, get(player) - amount);
    }

    public static void set(ServerPlayer player, float value) {
        float clamped = Math.max(0f, Math.min(MAX_ENERGY, value));
        ENERGY.put(player.getUUID(), clamped);
        sync(player, clamped);
    }

    private static void sync(ServerPlayer player, float value) {

        boolean hasLockedEnemy = EnemyLockTracker.isTargetedByEnemy(player);
        BRSPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new EnergySyncPacket(value, MAX_ENERGY, hasLockedEnemy));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        float regenPerTick = REGEN_PER_SECOND / 20f;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            float current = get(player);
            if (current < MAX_ENERGY) {

                set(player, current + regenPerTick);
            } else {

                sync(player, current);
            }
        }
    }

    // ── Oyuncu ayrılınca hafızayı temizle ──
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ENERGY.remove(event.getEntity().getUUID());
    }
}