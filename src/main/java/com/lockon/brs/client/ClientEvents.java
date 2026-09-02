package com.lockon.brs.client;

import com.lockon.LockOnMod;
import com.lockon.brs.camera.*;
import com.lockon.brs.client.gui.CameraConfigScreen;
import com.lockon.brs.config.CameraConfig;
import com.lockon.brs.lock.LockState;
import com.lockon.brs.lock.TargetScanner;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null) return;

        CameraRig.update(mc, mc.getFrameTime());
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null || mc.level == null) return;

        // FPS bağımsız delta (ms)
        float frameDeltaTicks = mc.getDeltaFrameTime();
        float deltaMs = frameDeltaTicks * 50.0f;

        float timeScale = HitStopController.tick(deltaMs);

        float scaledDeltaSeconds = (deltaMs / 1000.0f) * timeScale;
        ScreenShakeController.tick(scaledDeltaSeconds);
        FovController.tick(scaledDeltaSeconds);

        if (com.lockon.camera.ShoulderCamMode.isNew()) {
            SemiOrbitController.tick(mc, frameDeltaTicks);
        }

        // Kamera update
        if (LockState.isLocked() || CameraStateManager.lockedTarget != null) {
            float partialTick = mc.getFrameTime();
            CameraStateManager.updateCameraFPS(mc, partialTick, frameDeltaTicks);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (KeyBindings.CAMERA_CONFIG_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new CameraConfigScreen(null));
            }
        }

        if (KeyBindings.ORBIT_TOGGLE_KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                if (com.lockon.camera.ShoulderCamMode.isOld()) {
                    // OLD moddayken ALT artık devre dışı - OLDdan çıkış sadece komutla olur.
                    return;
                }

                // 3 adımlı döngü: 1) New (Shoulder)  2) Semi Orbit  3) Orbit  -> tekrar 1)
                String labelKey;
                if (SemiOrbitController.isEnabled()) {
                    // 2) Semi Orbit -> 3) Orbit
                    SemiOrbitController.setEnabled(false);
                    OrbitCameraState.setMode(OrbitCameraState.CameraMode.ORBIT);
                    labelKey = "camera.mode.orbit";
                } else if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
                    // 3) Orbit -> 1) New (Shoulder)
                    OrbitCameraState.requestExit();
                    labelKey = "camera.mode.new_shoulder";
                } else {
                    // 1) New (Shoulder) -> 2) Semi Orbit
                    SemiOrbitController.setEnabled(true);
                    labelKey = "camera.mode.semi_orbit";
                }
                mc.player.displayClientMessage(Component.translatable("hud.lockon.camera_mode", Component.translatable(labelKey)), true);
            }
        }
    }

    //  Config yüklendiğinde / değiştiğinde güncelle
    @Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onConfigLoad(ModConfigEvent.Loading event) {
            if (event.getConfig().getModId().equals(LockOnMod.MOD_ID)) {
                String fileName = event.getConfig().getFileName();

                // Sadece ilgili config yüklendiğinde ilgili kodu çalıştır
                if (fileName.equals("brs-camera.toml")) {
                    CameraRig.loadConfig();
                } else if (fileName.equals("brs-lockon.toml") || fileName.equals("lockon-shared-lists.toml")) {

                    TargetScanner.refreshCache();
                }
            }
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            if (event.getConfig().getModId().equals(LockOnMod.MOD_ID)) {
                String fileName = event.getConfig().getFileName();

                if (fileName.equals("brs-camera.toml")) {
                    CameraRig.loadConfig();
                } else if (fileName.equals("brs-lockon.toml") || fileName.equals("lockon-shared-lists.toml")) {
                    TargetScanner.refreshCache();
                }
            }
        }
    }
}