package com.lockon.brs.client.gui;

import com.lockon.brs.camera.CameraRig;
import com.lockon.brs.config.CameraConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class OrbitCameraConfigScreen extends Screen {

    private final Screen parent;

    public OrbitCameraConfigScreen(Screen parent) {
        super(Component.translatable("screen.lockon.brs_orbit_camera_settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 30;

        //  Orbit Camera
        this.addRenderableWidget(CycleButton
                .onOffBuilder(CameraConfig.ENABLE_ORBIT_CAMERA.get())
                .create(centerX - 100, y, 200, 20,
                        Component.translatable("camera.config.orbit_camera"),
                        (btn, val) -> {
                            CameraConfig.ENABLE_ORBIT_CAMERA.set(val);
                            CameraRig.loadConfig();
                        }));
        y += 30;

        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "camera.config.orbit_distance",
                CameraConfig.ORBIT_DISTANCE.get(), 2.0, 15.0,
                val -> {
                    CameraConfig.ORBIT_DISTANCE.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "camera.config.orbit_height",
                CameraConfig.ORBIT_HEIGHT_OFFSET.get(), -2.0, 5.0,
                val -> {
                    CameraConfig.ORBIT_HEIGHT_OFFSET.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        //Transition Speed
        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "camera.config.transition_speed",
                CameraConfig.ORBIT_TRANSITION_SPEED.get(), 0.01, 0.3,
                val -> {
                    CameraConfig.ORBIT_TRANSITION_SPEED.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        //Auto Rotate
        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "camera.config.auto_rotate_speed",
                CameraConfig.ORBIT_AUTO_ROTATE_SPEED.get(), 0.0, 5.0,
                val -> {
                    CameraConfig.ORBIT_AUTO_ROTATE_SPEED.set(val);
                    CameraRig.loadConfig();
                }));
        y += 35;

        this.addRenderableWidget(Button.builder(
                Component.translatable("camera.config.orbit_key_hint"),
                btn -> {}
        ).bounds(centerX - 100, y, 200, 20).build());
        y += 25;

        // Reset to Defaults
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.lockon.reset_to_defaults"),
                btn -> {
                    CameraConfig.ENABLE_ORBIT_CAMERA.set(true);
                    CameraConfig.ORBIT_DISTANCE.set(5.0);
                    CameraConfig.ORBIT_HEIGHT_OFFSET.set(1.0);
                    CameraConfig.ORBIT_TRANSITION_SPEED.set(0.06);
                    CameraConfig.ORBIT_AUTO_ROTATE_SPEED.set(0.0);
                    CameraConfig.ORBIT_SENSITIVITY.set(0.15);
                    CameraRig.loadConfig();
                    this.minecraft.setScreen(new OrbitCameraConfigScreen(this.parent));
                }).bounds(centerX - 100, y, 200, 20).build());
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "camera.config.mouse_sensitivity",
                CameraConfig.ORBIT_SENSITIVITY.get(), 0.01, 1.0,
                val -> {
                    CameraConfig.ORBIT_SENSITIVITY.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        //  Back
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.lockon.back"),
                btn -> this.onClose()
        ).bounds(centerX - 100, y, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        CameraRig.loadConfig();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static class ConfigSlider extends AbstractSliderButton {
        private final String labelKey;
        private final double min;
        private final double max;
        private final Consumer<Double> onValueChange;

        public ConfigSlider(int x, int y, int w, int h,
                            String labelKey, double value,
                            double min, double max,
                            Consumer<Double> onValueChange) {
            super(x, y, w, h,
                    Component.literal(I18n.get(labelKey) + ": " + String.format("%.2f", value)),
                    (value - min) / (max - min));
            this.labelKey = labelKey;
            this.min = min;
            this.max = max;
            this.onValueChange = onValueChange;
        }

        @Override
        protected void updateMessage() {
            double val = min + value * (max - min);
            this.setMessage(Component.literal(I18n.get(labelKey) + ": " + String.format("%.2f", val)));
        }

        @Override
        protected void applyValue() {
            double val = min + value * (max - min);
            onValueChange.accept(Math.round(val * 100.0) / 100.0);
        }
    }
}
