package com.lockon.brs.client;

import com.lockon.LockOnMod;
import com.lockon.brs.camera.OrbitCameraState;
import com.lockon.camera.ShoulderCamMode;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * "/ff old cam", "/ff new cam", "/ff orbit cam" - üç kamera moduna doğrudan
 * geçiş için client-only komutlar.
 *
 * Bu komutlar, ALT tuşu döngüsünden (bkz. ClientEvents#onKeyInput) bağımsız
 * çalışır. ALT artık sadece New <-> Orbit arasında geçiş yapıyor; Old moduna
 * girmenin/çıkmanın TEK yolu bu komutlardır.
 */
@Mod.EventBusSubscriber(modid = LockOnMod.MOD_ID, value = Dist.CLIENT)
public class FFCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ff")
                        .then(Commands.literal("old")
                                .then(Commands.literal("cam")
                                        .executes(ctx -> {
                                            setOld();
                                            return 1;
                                        })))
                        .then(Commands.literal("new")
                                .then(Commands.literal("cam")
                                        .executes(ctx -> {
                                            setNew();
                                            return 1;
                                        })))
                        .then(Commands.literal("orbit")
                                .then(Commands.literal("cam")
                                        .executes(ctx -> {
                                            setOrbit();
                                            return 1;
                                        })))
        );
    }

    private static void setOld() {
        // Orbit açıksa önce yumuşak şekilde omuz pozisyonuna çıkış başlat,
        // sonra Old'a geç (aniden kesilmiş bir orbit transition'ı kalmasın).
        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            OrbitCameraState.requestExit();
        }
        ShoulderCamMode.set(ShoulderCamMode.Mode.OLD);
        notify("Old Camera");
    }

    private static void setNew() {
        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            OrbitCameraState.requestExit();
        }
        ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
        notify("New Camera (Shoulder)");
    }

    private static void setOrbit() {
        // Orbit sistemi sadece NEW omuz kamerası modunda render edildiği için
        // (bkz. VirtualCameraHandler), önce NEW moda geçip ardından orbit'i açıyoruz.
        ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
        OrbitCameraState.setMode(OrbitCameraState.CameraMode.ORBIT);
        notify("Orbit Camera");
    }

    private static void notify(String label) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("Camera Mode: " + label), true);
        }
    }
}
