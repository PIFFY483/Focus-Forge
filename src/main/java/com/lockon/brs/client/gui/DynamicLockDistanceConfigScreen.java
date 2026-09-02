package com.lockon.brs.client.gui;

import com.lockon.brs.config.LockOnConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class DynamicLockDistanceConfigScreen extends Screen {

    private final Screen parent;

    private static final int COL_WIDTH = 220;

    public DynamicLockDistanceConfigScreen(Screen parent) {
        super(Component.literal("Dinamik Kamera Mesafesi"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int x = centerX - COL_WIDTH / 2;
        int y = 30;

        this.addRenderableWidget(CycleButton
                .onOffBuilder(LockOnConfig.ENABLE_DYNAMIC_LOCK_DISTANCE.get())
                .create(x, y, COL_WIDTH, 20,
                        Component.literal("Dinamik Mesafe"),
                        (btn, val) -> LockOnConfig.ENABLE_DYNAMIC_LOCK_DISTANCE.set(val)));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                x, y, COL_WIDTH, 20,
                "Çerçeve Payı",
                LockOnConfig.FRAME_MARGIN.get(), 0.1, 1.0, 2,
                val -> LockOnConfig.FRAME_MARGIN.set(val)));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                x, y, COL_WIDTH, 20,
                "Min Mesafe",
                LockOnConfig.MIN_LOCK_CAMERA_DISTANCE.get(), 0.5, 10.0, 1,
                val -> LockOnConfig.MIN_LOCK_CAMERA_DISTANCE.set(val)));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                x, y, COL_WIDTH, 20,
                "Max Mesafe",
                LockOnConfig.MAX_LOCK_CAMERA_DISTANCE.get(), 2.0, 30.0, 1,
                val -> LockOnConfig.MAX_LOCK_CAMERA_DISTANCE.set(val)));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                x, y, COL_WIDTH, 20,
                "Geçiş Hızı",
                LockOnConfig.LOCK_DISTANCE_SMOOTH_SPEED.get(), 0.01, 0.5, 2,
                val -> LockOnConfig.LOCK_DISTANCE_SMOOTH_SPEED.set(val)));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                x, y, COL_WIDTH, 20,
                "Maks Adım (blok/tick)",
                LockOnConfig.LOCK_DISTANCE_MAX_STEP_PER_TICK.get(), 0.01, 2.0, 2,
                val -> LockOnConfig.LOCK_DISTANCE_MAX_STEP_PER_TICK.set(val)));
        y += 30;

        // ── Lock mesafesi artışına/azalışına bağlı Y offset (genel Oto Y Hizalama'dan bağımsız) ──
        this.addRenderableWidget(CycleButton
                .onOffBuilder(LockOnConfig.ENABLE_LOCK_DISTANCE_Y_OFFSET.get())
                .create(x, y, COL_WIDTH, 20,
                        Component.literal("Mesafeye Bağlı Y"),
                        (btn, val) -> LockOnConfig.ENABLE_LOCK_DISTANCE_Y_OFFSET.set(val)));
        y += 25;

        this.addRenderableWidget(new ConfigSlider(
                x, y, COL_WIDTH, 20,
                "Y Oranı",
                LockOnConfig.LOCK_DISTANCE_Y_FACTOR.get(), 0.0, 1.0, 2,
                val -> LockOnConfig.LOCK_DISTANCE_Y_FACTOR.set(val)));
        y += 35;

        this.addRenderableWidget(Button.builder(
                Component.literal("Varsayılanlara Dön"),
                btn -> {
                    LockOnConfig.ENABLE_DYNAMIC_LOCK_DISTANCE.set(true);
                    LockOnConfig.FRAME_MARGIN.set(0.65);
                    LockOnConfig.MIN_LOCK_CAMERA_DISTANCE.set(2.0);
                    LockOnConfig.MAX_LOCK_CAMERA_DISTANCE.set(12.0);
                    LockOnConfig.LOCK_DISTANCE_SMOOTH_SPEED.set(0.08);
                    LockOnConfig.LOCK_DISTANCE_MAX_STEP_PER_TICK.set(0.12);
                    LockOnConfig.ENABLE_LOCK_DISTANCE_Y_OFFSET.set(true);
                    LockOnConfig.LOCK_DISTANCE_Y_FACTOR.set(0.20);
                    this.minecraft.setScreen(new DynamicLockDistanceConfigScreen(this.parent));
                }).bounds(x, y, COL_WIDTH, 20).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
                Component.literal("Tamam"),
                btn -> this.onClose()
        ).bounds(x, y, COL_WIDTH, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
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
        private final int decimals;
        private final Consumer<Double> onValueChange;

        public ConfigSlider(int x, int y, int w, int h,
                            String label, double value,
                            double min, double max, int decimals,
                            Consumer<Double> onValueChange) {
            super(x, y, w, h,
                    Component.literal(label + ": " + String.format("%." + decimals + "f", value)),
                    (value - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.decimals = decimals;
            this.onValueChange = onValueChange;
        }

        @Override
        protected void updateMessage() {
            double val = min + value * (max - min);
            this.setMessage(Component.literal(label + ": " + String.format("%." + decimals + "f", val)));
        }

        @Override
        protected void applyValue() {
            double val = min + value * (max - min);
            double rounded = Math.round(val * Math.pow(10, decimals)) / Math.pow(10, decimals);
            onValueChange.accept(rounded);
        }
    }
}