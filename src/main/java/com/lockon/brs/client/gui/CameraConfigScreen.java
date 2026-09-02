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

public class CameraConfigScreen extends Screen {

    private final Screen parent;

    private static final int COL_WIDTH = 200;
    private static final int COL_GAP = 10;

    public CameraConfigScreen(Screen parent) {
        super(Component.translatable("screen.lockon.brs_camera_settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - COL_WIDTH - COL_GAP / 2;
        int rightX = this.width / 2 + COL_GAP / 2;
        int yLeft = 30;
        int yRight = 30;

        this.addRenderableWidget(CycleButton
                .onOffBuilder(CameraConfig.ENABLE_SHOULDER_CAM.get())
                .create(leftX, yLeft, COL_WIDTH, 20,
                        Component.translatable("camera.config.shoulder_cam"),
                        (btn, val) -> {
                            CameraConfig.ENABLE_SHOULDER_CAM.set(val);
                            CameraRig.loadConfig();
                        }));
        yLeft += 25;

        this.addRenderableWidget(CycleButton
                .onOffBuilder(CameraConfig.ENABLE_DYNAMIC_Y_OFFSET.get())
                .create(leftX, yLeft, COL_WIDTH, 20,
                        Component.translatable("camera.config.auto_y_align"),
                        (btn, val) -> CameraConfig.ENABLE_DYNAMIC_Y_OFFSET.set(val)));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.dynamic_y_ratio",
                CameraConfig.DYNAMIC_Y_FACTOR.get(), 0.0, 0.5,
                val -> CameraConfig.DYNAMIC_Y_FACTOR.set(val)));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.shoulder_offset",
                CameraConfig.SHOULDER_OFFSET.get(), -5.0, 5.0,
                val -> {
                    CameraConfig.SHOULDER_OFFSET.set(val);
                    CameraRig.loadConfig();
                }));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.height",
                CameraConfig.HEIGHT_OFFSET.get(), -5.0, 5.0,
                val -> {
                    CameraConfig.HEIGHT_OFFSET.set(val);
                    CameraRig.loadConfig();
                }));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.distance",
                CameraConfig.CAMERA_DISTANCE.get(), 0.5, 5.0,
                val -> {
                    CameraConfig.CAMERA_DISTANCE.set(val);
                    CameraRig.loadConfig();
                }));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.smooth",
                CameraConfig.CAMERA_SMOOTHNESS.get(), 0.05, 1.0,
                val -> CameraConfig.CAMERA_SMOOTHNESS.set(val)));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.lock_follow_speed",
                CameraConfig.CAMERA_FOLLOW_SPEED.get(), 0.01, 1.0,
                val -> CameraConfig.CAMERA_FOLLOW_SPEED.set(val)));
        yLeft += 25;

        this.addRenderableWidget(new ConfigSlider(
                leftX, yLeft, COL_WIDTH, 20,
                "camera.config.collision_recovery",
                CameraConfig.COLLISION_RECOVERY_SPEED.get(), 0.01, 1.0,
                val -> CameraConfig.COLLISION_RECOVERY_SPEED.set(val)));
        yLeft += 30;


        this.addRenderableWidget(CycleButton
                .onOffBuilder(CameraConfig.SKIP_FRONT_VIEW.get())
                .create(rightX, yRight, COL_WIDTH, 20,
                        Component.translatable("camera.config.skip_tpv_front"),
                        (btn, val) -> CameraConfig.SKIP_FRONT_VIEW.set(val)));
        yRight += 25;

        this.addRenderableWidget(CycleButton
                .onOffBuilder(CameraConfig.ENABLE_SMOOTH_TRANSITION.get())
                .create(rightX, yRight, COL_WIDTH, 20,
                        Component.translatable("camera.config.smooth_transition"),
                        (btn, val) -> {
                            CameraConfig.ENABLE_SMOOTH_TRANSITION.set(val);
                            CameraRig.loadConfig();
                        }));
        yRight += 25;

        this.addRenderableWidget(new ConfigSlider(
                rightX, yRight, COL_WIDTH, 20,
                "camera.config.transition_speed",
                CameraConfig.TRANSITION_SPEED.get(), 0.01, 1.0,
                val -> {
                    CameraConfig.TRANSITION_SPEED.set(val);
                    CameraRig.loadConfig();
                }));
        yRight += 25;

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.lockon.brs_lockon_settings.button"),
                btn -> this.minecraft.setScreen(new LockOnConfigScreen(this))
        ).bounds(rightX, yRight, COL_WIDTH, 20).build());
        yRight += 25;

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.lockon.brs_orbit_camera_settings.button"),
                btn -> this.minecraft.setScreen(new OrbitCameraConfigScreen(this))
        ).bounds(rightX, yRight, COL_WIDTH, 20).build());
        yRight += 30;

        int yBottom = Math.max(yLeft, yRight) + 10;
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.lockon.reset_to_defaults"),
                btn -> {
                    CameraConfig.ENABLE_SHOULDER_CAM.set(true);
                    CameraConfig.SHOULDER_OFFSET.set(0.5);
                    CameraConfig.HEIGHT_OFFSET.set(0.3);
                    CameraConfig.CAMERA_DISTANCE.set(4.0);
                    CameraConfig.CAMERA_SMOOTHNESS.set(0.15);
                    CameraConfig.SKIP_FRONT_VIEW.set(true);
                    CameraConfig.TRANSITION_SPEED.set(0.15);
                    CameraConfig.ENABLE_SMOOTH_TRANSITION.set(true);
                    CameraConfig.ENABLE_DYNAMIC_Y_OFFSET.set(true);
                    CameraConfig.DYNAMIC_Y_FACTOR.set(0.20);
                    CameraConfig.CAMERA_FOLLOW_SPEED.set(0.15);
                    CameraConfig.COLLISION_RECOVERY_SPEED.set(0.25);
                    CameraRig.loadConfig();
                    this.minecraft.setScreen(new CameraConfigScreen(this.parent));
                }).bounds(centerX - 100, yBottom, 200, 20).build());
        yBottom += 25;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.lockon.ok"),
                btn -> this.onClose()
        ).bounds(centerX - 100, yBottom, 200, 20).build());
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
