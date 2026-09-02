package com.lockon.client;

import com.lockon.config.CameraViewConfig;
import com.lockon.camera.ShoulderCamMode;
import com.lockon.brs.camera.OrbitCameraState;
import com.lockon.brs.camera.SemiOrbitController;
import com.lockon.brs.client.gui.LockOnConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Consumer;

public class CameraConfigScreen extends Screen {

    // Translation keys for the crosshair mode names, kept visible to the whole class
    private static final String[] modeKeys = {
            "camera.config.crosshair_mode.hidden",
            "camera.config.crosshair_mode.shoulder_only",
            "camera.config.crosshair_mode.always"
    };

    public CameraConfigScreen() {
        super(Component.translatable("screen.lockon.camera_live_settings.title"));
    }

    @Override
    protected void init() {
        int xLeft = 10;
        int xRight = this.width - 160;
        int y = 10;
        int width = 150;
        int spacing = 25;

        this.addRenderableWidget(new FloatSlider(xLeft, y, width, "camera.config.shoulder_x", CameraViewConfig.SHOULDER_OFFSET_X.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.SHOULDER_OFFSET_X.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + spacing, width, "camera.config.shoulder_y", CameraViewConfig.SHOULDER_OFFSET_Y.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.SHOULDER_OFFSET_Y.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 2), width, "camera.config.distance", CameraViewConfig.CAMERA_DISTANCE.get(), 1.0, 10.0, (val) -> {
            CameraViewConfig.CAMERA_DISTANCE.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 3), width, "camera.config.smoothness", CameraViewConfig.CAMERA_SMOOTHNESS.get(), 0.05, 1.0, (val) -> {
            CameraViewConfig.CAMERA_SMOOTHNESS.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 5), width, "camera.config.focus_y_offset",
                CameraViewConfig.CLIENT.focusOffsetY.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.CLIENT.focusOffsetY.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 4), width, "camera.config.big_mob_focus",
                CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD.get(), 0.1, 1.0, (val) -> {
            CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD.set(val);
        }));

        this.addRenderableWidget(Button.builder(
                        Component.translatable("camera.config.mode", Component.translatable(cameraModeLabelKey(currentCameraModeStep()))),
                        (btn) -> {
                            int nextStep = (currentCameraModeStep() + 1) % 5;
                            applyCameraModeStep(nextStep);
                            btn.setMessage(Component.translatable("camera.config.mode", Component.translatable(cameraModeLabelKey(nextStep))));
                        })
                .bounds(xRight, y, width, 20).build());

        // 2. Crosshair Button
        this.addRenderableWidget(Button.builder(
                        Component.translatable("camera.config.crosshair", Component.translatable(modeKeys[CameraViewConfig.CROSSHAIR_MODE.get()])), //
                        (btn) -> {
                            int nextMode = (CameraViewConfig.CROSSHAIR_MODE.get() + 1) % 3;
                            CameraViewConfig.CROSSHAIR_MODE.set(nextMode); //
                            btn.setMessage(Component.translatable("camera.config.crosshair", Component.translatable(modeKeys[nextMode]))); //
                        })
                .bounds(xRight, y + spacing, width, 20).build());

        // 3. Zoom Toggle
        this.addRenderableWidget(Button.builder(
                        Component.translatable("camera.config.zoom", stateLabel(CameraViewConfig.CLIENT.enableZoom.get())),
                        (btn) -> {
                            boolean newState = !CameraViewConfig.CLIENT.enableZoom.get();
                            CameraViewConfig.CLIENT.enableZoom.set(newState);
                            btn.setMessage(Component.translatable("camera.config.zoom", stateLabel(newState)));
                        })
                .bounds(xRight, y + (spacing * 2), width, 20).build());

        // 4. Zoom Speed
        this.addRenderableWidget(new FloatSlider(xRight, y + (spacing * 3), width, "camera.config.zoom_speed",
                CameraViewConfig.CLIENT.zoomInSpeed.get(), 0.01, 1.0, (val) -> {
            CameraViewConfig.CLIENT.zoomInSpeed.set(val);
        }));

        // 5. Tracking Sensitivity
        this.addRenderableWidget(new FloatSlider(xRight, y + (spacing * 4), width, "camera.config.tracking_sens",
                CameraViewConfig.CLIENT.lockOnSmoothness.get(), 0.001, 0.125, (val) -> {
            CameraViewConfig.CLIENT.lockOnSmoothness.set(val);
        }));

        // 6. Vignette Toggle
        this.addRenderableWidget(Button.builder(
                        Component.translatable("camera.config.vignette", stateLabel(CameraViewConfig.CLIENT.enableVignette.get())),
                        (btn) -> {
                            boolean newState = !CameraViewConfig.CLIENT.enableVignette.get();
                            CameraViewConfig.CLIENT.enableVignette.set(newState);
                            btn.setMessage(Component.translatable("camera.config.vignette", stateLabel(newState)));
                        })
                .bounds(xRight, y + (spacing * 5), width, 20).build());


        // Crosshair shape style keys:
        String[] styleKeys = {
                "camera.config.style.arrow", "camera.config.style.classic", "camera.config.style.dot",
                "camera.config.style.circle", "camera.config.style.tactical", "camera.config.style.double_ring",
                "camera.config.style.corners"
        };

        this.addRenderableWidget(Button.builder(
                        Component.translatable("camera.config.style", Component.translatable(styleKeys[CameraViewConfig.CLIENT.crosshairStyle.get()])),
                        (btn) -> {
                            // Modulo by 7 (cycles through 0-6)
                            int nextStyle = (CameraViewConfig.CLIENT.crosshairStyle.get() + 1) % 7;
                            CameraViewConfig.CLIENT.crosshairStyle.set(nextStyle);
                            btn.setMessage(Component.translatable("camera.config.style", Component.translatable(styleKeys[nextStyle])));
                        })
                .bounds(xRight, y + (spacing * 6), width, 20).build());

        String[] colorKeys = {
                "camera.config.crosshair_color.turquoise", "camera.config.crosshair_color.green",
                "camera.config.crosshair_color.dark_blue", "camera.config.crosshair_color.red",
                "camera.config.crosshair_color.white"
        };

        this.addRenderableWidget(Button.builder(
                        Component.translatable("camera.config.crosshair_color", Component.translatable(colorKeys[CameraViewConfig.CROSSHAIR_COLOR_INDEX.get()])),
                        (btn) -> {

                            int nextColor = (CameraViewConfig.CROSSHAIR_COLOR_INDEX.get() + 1) % 5;

                            CameraViewConfig.CROSSHAIR_COLOR_INDEX.set(nextColor);

                            // Updates the text on the button
                            btn.setMessage(Component.translatable("camera.config.crosshair_color", Component.translatable(colorKeys[nextColor])));
                        })
                .bounds(xRight, y + (spacing * 7), width, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.lockon.brs_lockon_settings.button"),
                        btn -> this.minecraft.setScreen(new LockOnConfigScreen(this)))
                .bounds(xRight, y + (spacing * 8), width, 20).build());

        // Parallax Assist Button
        this.addRenderableWidget(CycleButton.onOffBuilder(CameraViewConfig.CLIENT.enableParallaxAssist.get())
                .create(xLeft, y + (spacing * 6), width, 20, Component.translatable("camera.config.parallax_assist"), (btn, val) -> {
                    CameraViewConfig.CLIENT.enableParallaxAssist.set(val);
                }));

    }

    private static int currentCameraModeStep() {
        if (ShoulderCamMode.isOld()) {
            return CameraViewConfig.ENABLE_SHOULDER_CAM.get() ? 1 : 0;
        }
        if (SemiOrbitController.isEnabled()) {
            return 3;
        }
        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            return 4;
        }
        return 2;
    }

    private static String cameraModeLabelKey(int step) {
        switch (step) {
            case 0: return "camera.config.mode.classic";
            case 1: return "camera.config.mode.old_shoulder";
            case 2: return "camera.mode.new_shoulder";
            case 3: return "camera.mode.semi_orbit";
            case 4: return "camera.mode.orbit";
            default: return "camera.config.mode.classic";
        }
    }

    private static void applyCameraModeStep(int step) {

        SemiOrbitController.setEnabled(false);
        if (OrbitCameraState.getMode() == OrbitCameraState.CameraMode.ORBIT) {
            OrbitCameraState.requestExit();
        }

        switch (step) {
            case 0: // Classic
                ShoulderCamMode.set(ShoulderCamMode.Mode.OLD);
                CameraViewConfig.ENABLE_SHOULDER_CAM.set(false);
                break;
            case 1: // Old Shoulder
                ShoulderCamMode.set(ShoulderCamMode.Mode.OLD);
                CameraViewConfig.ENABLE_SHOULDER_CAM.set(true);
                break;
            case 2: // New Shoulder
                ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
                break;
            case 3: // Semi Orbit
                ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
                SemiOrbitController.setEnabled(true);
                break;
            case 4: // Orbit
                ShoulderCamMode.set(ShoulderCamMode.Mode.NEW);
                OrbitCameraState.setMode(OrbitCameraState.CameraMode.ORBIT);
                break;
        }
    }

    private static Component stateLabel(boolean enabled) {
        return Component.translatable(enabled ? "camera.config.state.enabled" : "camera.config.state.disabled");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        CameraViewConfig.SPEC.save(); // Writes the settings to the config file
        super.onClose();
    }

    private static class FloatSlider extends AbstractSliderButton {
        private final String labelKey;
        private final double min, max;
        private final Consumer<Double> setter;

        public FloatSlider(int x, int y, int w, String labelKey, double current, double min, double max, Consumer<Double> setter) {
            super(x, y, w, 20, Component.literal(""), Mth.clamp((current - min) / (max - min), 0.0, 1.0));
            this.labelKey = labelKey;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.updateMessage();
        }

        @Override
        protected void applyValue() {
            double val = min + (max - min) * value;
            setter.accept(val);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + (max - min) * value;
            setMessage(Component.literal(I18n.get(labelKey) + ": " + String.format("%.3f", val)));
        }
    }
}
