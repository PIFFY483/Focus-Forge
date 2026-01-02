package com.lockon.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;

import java.util.HashMap;
import java.util.Map;

public class SoundFixer {
    // Mermi sınıflarını seslerle eşleştiren merkezi harita
    private static final Map<Class<? extends Entity>, SoundEvent> PROJECTILE_SOUNDS = new HashMap<>();

    static {
        // Temel eşleşmeler
        PROJECTILE_SOUNDS.put(ThrownTrident.class, SoundEvents.TRIDENT_THROW);
        PROJECTILE_SOUNDS.put(Arrow.class, SoundEvents.ARROW_SHOOT);
        PROJECTILE_SOUNDS.put(SpectralArrow.class, SoundEvents.ARROW_SHOOT);
        PROJECTILE_SOUNDS.put(Snowball.class, SoundEvents.SNOWBALL_THROW);
        PROJECTILE_SOUNDS.put(ThrownEgg.class, SoundEvents.EGG_THROW);
        PROJECTILE_SOUNDS.put(ThrownExperienceBottle.class, SoundEvents.EXPERIENCE_BOTTLE_THROW);
        PROJECTILE_SOUNDS.put(ThrownPotion.class, SoundEvents.SPLASH_POTION_THROW);
    }

    public static void playLaunchSound(Player player, Entity originalProjectile) {
        if (player == null || originalProjectile == null) return;


        SoundEvent sound = PROJECTILE_SOUNDS.get(originalProjectile.getClass());

        //  Eğer tam eşleşme yoksa, genel sınıflara bak (Modlu oklar veya fırlatılabilirler için)
        if (sound == null) {
            if (originalProjectile instanceof AbstractArrow) {
                sound = SoundEvents.ARROW_SHOOT;
            } else if (originalProjectile instanceof ThrowableItemProjectile) {
                sound = SoundEvents.SNOWBALL_THROW; // Genel fırlatma sesi
            }
        }

        // Ses bulunduysa çal
        if (sound != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, 1.0F, 1.0F / (player.level().getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }
}