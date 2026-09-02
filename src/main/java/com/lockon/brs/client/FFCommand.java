package com.lockon.brs.client;

import com.lockon.LockOnMod;
import com.lockon.brs.camera.OrbitCameraState;
import com.lockon.brs.camera.SemiOrbitController;
import com.lockon.brs.config.CameraConfig;
import com.lockon.camera.ShoulderCamMode;
import com.lockon.config.CameraViewConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
                        .then(Commands.literal("semiorbit")
                                .then(Commands.literal("cam")
                                        .executes(ctx -> {
                                            setSemiOrbit();
                                            return 1;
                                        })))
                        .then(Commands.literal("close")
                                .executes(ctx -> {
                                    closeAll();
                                    return 1;
                                }))
        );
    }

    private static void setOld() {

        SemiOrbitController.setEnabled(false);

        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            OrbitCameraState.requestExit();
        }
        ShoulderCamMode.set(ShoulderCamMode.Mode.OLD);
        notify("camera.mode.old");
    }

    private static void setNew() {
        SemiOrbitController.setEnabled(false);
        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            OrbitCameraState.requestExit();
        }
        ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
        notify("camera.mode.new_shoulder");
    }

    private static void setOrbit() {
        SemiOrbitController.setEnabled(false);

        ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
        OrbitCameraState.setMode(OrbitCameraState.CameraMode.ORBIT);
        notify("camera.mode.orbit");
    }

    private static void setSemiOrbit() {
        ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);

        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            OrbitCameraState.requestExit();
        }
        SemiOrbitController.setEnabled(true);
        notify("camera.mode.semi_orbit");
    }

    private static void closeAll() {
        SemiOrbitController.setEnabled(false);

        // Orbiti anında ve tamamen kapat
        OrbitCameraState.reset();

        if (CameraConfig.ENABLE_SHOULDER_CAM.get()) {
            CameraConfig.ENABLE_SHOULDER_CAM.set(false);
        }

        if (CameraViewConfig.ENABLE_SHOULDER_CAM.get()) {
            CameraViewConfig.ENABLE_SHOULDER_CAM.set(false);
        }

        notify("camera.mode.closed");
    }

    private static void notify(String labelKey) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.translatable("hud.lockon.camera_mode", Component.translatable(labelKey)), true);
        }
    }
}
