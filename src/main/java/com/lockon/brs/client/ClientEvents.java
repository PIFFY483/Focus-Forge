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

    // ── Her client tick (20/sn) — mantık/config güncellemeleri burada kalıyor ──
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null) return;

        CameraRig.update(mc, mc.getFrameTime());
    }

    // ── Her render frame'de — kilit kamerası burada, FPS'e bağlı olarak smooth çalışır ──
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null || mc.level == null) return;

        // FPS bağımsız delta (ms)
        float frameDeltaTicks = mc.getDeltaFrameTime();
        float deltaMs = frameDeltaTicks * 50.0f;

        // ── HitStop'u güncelle ──
        float timeScale = HitStopController.tick(deltaMs);

        // ── Diğer sistemler scaledDelta ile çalışır ──
        float scaledDeltaSeconds = (deltaMs / 1000.0f) * timeScale;
        ScreenShakeController.tick(scaledDeltaSeconds);
        FovController.tick(scaledDeltaSeconds);

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
        // ALT: artık SADECE 2 adımlı bir döngü - New Camera (shoulder, BRS) <-> Orbit.
        // Old Camera bu döngünün tamamen dışında tutuluyor; Old'a sadece "/ff old cam"
        // komutuyla geçilebiliyor ve Old moddayken ALT hiçbir şeyi değiştirmiyor
        // (bkz. com.lockon.brs.client.FFCommand - /ff old cam, /ff new cam, /ff orbit cam).
        if (KeyBindings.ORBIT_TOGGLE_KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                if (com.lockon.camera.ShoulderCamMode.isOld()) {
                    // OLD moddayken ALT artık devre dışı - OLD'dan çıkış sadece komutla olur.
                    return;
                }

                boolean isOrbitMode = OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT;

                String label;
                if (!isOrbitMode) {
                    OrbitCameraState.toggle(); // NEW -> ORBIT
                    label = "Orbit Camera";
                } else {
                    OrbitCameraState.toggle(); // ORBIT -> NEW (smooth çıkış)
                    label = "New Camera (Shoulder)";
                }
                mc.player.displayClientMessage(Component.literal("Camera Mode: " + label), true);
            }
        }
    }

    // ── Config yüklendiğinde / değiştiğinde güncelle ──
    @Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onConfigLoad(ModConfigEvent.Loading event) {
            if (event.getConfig().getModId().equals(LockOnMod.MOD_ID)) {
                String fileName = event.getConfig().getFileName();

                // Sadece ilgili config yüklendiğinde ilgili kodu çalıştır
                if (fileName.equals("brs-camera.toml")) {
                    CameraRig.loadConfig();
                } else if (fileName.equals("brs-lockon.toml")) {
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
                } else if (fileName.equals("brs-lockon.toml")) {
                    TargetScanner.refreshCache();
                }
            }
        }
    }
}