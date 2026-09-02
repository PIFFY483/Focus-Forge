package com.lockon.brs.client;

import com.lockon.LockOnMod;
import com.lockon.client.ShoulderCamModeOverlay;
import com.lockon.client.LockTypeOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.CAMERA_CONFIG_KEY);
        event.register(KeyBindings.ORBIT_TOGGLE_KEY); // Not: önceden buraya register edilmemişti, eklendi
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        // Enerji barı overlay'i kullanıcı isteğiyle devre dışı bırakıldı (EnergyBarOverlay.java hâlâ mevcut,
        // sadece HUD'a register edilmiyor). Geri istersen aşağıdaki satırı tekrar aç:
        // event.registerAboveAll("brs_energy_bar", EnergyBarOverlay.INSTANCE);
        event.registerAboveAll("lockon_shoulder_cam_mode", ShoulderCamModeOverlay.INSTANCE);
        event.registerAboveAll("lockon_lock_type", LockTypeOverlay.INSTANCE);
    }
}