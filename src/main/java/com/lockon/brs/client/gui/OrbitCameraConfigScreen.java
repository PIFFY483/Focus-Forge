package com.lockon.brs.client.gui;

import com.lockon.brs.camera.CameraRig;
import com.lockon.brs.config.CameraConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class OrbitCameraConfigScreen extends Screen {

    private final Screen parent;

    public OrbitCameraConfigScreen(Screen parent) {
        super(Component.literal("BRS Orbit Kamera Ayarları"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 30;

        // ── Orbit Kamera Aç/Kapa ──
        this.addRenderableWidget(CycleButton
                .onOffBuilder(CameraConfig.ENABLE_ORBIT_CAMERA.get())
                .create(centerX - 100, y, 200, 20,
                        Component.literal("Orbit Kamera"),
                        (btn, val) -> {
                            CameraConfig.ENABLE_ORBIT_CAMERA.set(val);
                            CameraRig.loadConfig();
                        }));
        y += 30;

        // ── Orbit Uzaklık ──
        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "Orbit Uzaklık",
                CameraConfig.ORBIT_DISTANCE.get(), 2.0, 15.0,
                val -> {
                    CameraConfig.ORBIT_DISTANCE.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        // ── Orbit Yükseklik ──
        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "Orbit Yükseklik",
                CameraConfig.ORBIT_HEIGHT_OFFSET.get(), -2.0, 5.0,
                val -> {
                    CameraConfig.ORBIT_HEIGHT_OFFSET.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        // ── Geçiş Hızı ──
        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "Geçiş Hızı",
                CameraConfig.ORBIT_TRANSITION_SPEED.get(), 0.01, 0.3,
                val -> {
                    CameraConfig.ORBIT_TRANSITION_SPEED.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        // ── Otomatik Dönüş ──
        this.addRenderableWidget(new ConfigSlider(
                centerX - 100, y, 200, 20,
                "Oto Dönüş Hızı",
                CameraConfig.ORBIT_AUTO_ROTATE_SPEED.get(), 0.0, 5.0,
                val -> {
                    CameraConfig.ORBIT_AUTO_ROTATE_SPEED.set(val);
                    CameraRig.loadConfig();
                }));
        y += 35;

        // ── Bilgi Satırı ──
        this.addRenderableWidget(Button.builder(
                Component.literal("Tuş: Sol Alt (Oyun içinde değiştirilebilir)"),
                btn -> {}
        ).bounds(centerX - 100, y, 200, 20).build());
        y += 25;

        // ── Varsayılanlara Dön ──
        this.addRenderableWidget(Button.builder(
                Component.literal("Varsayılanlara Dön"),
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
                "Fare Hassasiyeti",
                CameraConfig.ORBIT_SENSITIVITY.get(), 0.01, 1.0,
                val -> {
                    CameraConfig.ORBIT_SENSITIVITY.set(val);
                    CameraRig.loadConfig();
                }));
        y += 25;

        // ── Geri ──
        this.addRenderableWidget(Button.builder(
                Component.literal("Geri"),
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
        private final String label;
        private final double min;
        private final double max;
        private final Consumer<Double> onValueChange;

        public ConfigSlider(int x, int y, int w, int h,
                            String label, double value,
                            double min, double max,
                            Consumer<Double> onValueChange) {
            super(x, y, w, h,
                    Component.literal(label + ": " + String.format("%.2f", value)),
                    (value - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onValueChange = onValueChange;
        }

        @Override
        protected void updateMessage() {
            double val = min + value * (max - min);
            this.setMessage(Component.literal(label + ": " + String.format("%.2f", val)));
        }

        @Override
        protected void applyValue() {
            double val = min + value * (max - min);
            onValueChange.accept(Math.round(val * 100.0) / 100.0);
        }
    }
}