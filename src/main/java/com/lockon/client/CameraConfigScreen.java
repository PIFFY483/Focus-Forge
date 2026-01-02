package com.lockon.client;

import com.lockon.config.CameraViewConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import com.lockon.config.CameraViewConfig.VisualStyle;

import java.util.function.Consumer;

public class CameraConfigScreen extends Screen {

    // modeNames'i buraya alarak tüm sınıf için görünür kılar
    private static final String[] modeNames = {"Hidden", "Shoulder Only", "Always"};

    public CameraConfigScreen() {
        super(Component.literal("Camera Live Settings"));
    }

    @Override
    protected void init() {
        int xLeft = 10;
        int xRight = this.width - 160;
        int y = 10;
        int width = 150;
        int spacing = 25;

        // --- LEFT SIDE (Basic Camera Settings) ---
        this.addRenderableWidget(new FloatSlider(xLeft, y, width, "Shoulder X", CameraViewConfig.SHOULDER_OFFSET_X.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.SHOULDER_OFFSET_X.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + spacing, width, "Shoulder Y", CameraViewConfig.SHOULDER_OFFSET_Y.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.SHOULDER_OFFSET_Y.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 2), width, "Distance", CameraViewConfig.CAMERA_DISTANCE.get(), 1.0, 10.0, (val) -> {
            CameraViewConfig.CAMERA_DISTANCE.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 3), width, "Smoothness", CameraViewConfig.CAMERA_SMOOTHNESS.get(), 0.05, 1.0, (val) -> {
            CameraViewConfig.CAMERA_SMOOTHNESS.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 5), width, "Focus Y Offset",
                CameraViewConfig.CLIENT.focusOffsetY.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.CLIENT.focusOffsetY.set(val);
        }));

        this.addRenderableWidget(new FloatSlider(xLeft, y + (spacing * 4), width, "Big Mob Focus %",
                CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD.get(), 0.1, 1.0, (val) -> {
            CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD.set(val);
        }));

        // --- RIGHT SIDE (Lock-On and Zoom Settings) ---
        // 1. Mode Button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Mode: " + (CameraViewConfig.ENABLE_SHOULDER_CAM.get() ? "Shoulder" : "Classic")),
                        (btn) -> {
                            boolean newState = !CameraViewConfig.ENABLE_SHOULDER_CAM.get();
                            CameraViewConfig.ENABLE_SHOULDER_CAM.set(newState);
                            btn.setMessage(Component.literal("Mode: " + (newState ? "Shoulder" : "Classic")));
                        })
                .bounds(xRight, y, width, 20).build());

        // 2. Crosshair Button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Crosshair: " + modeNames[CameraViewConfig.CROSSHAIR_MODE.get()]), //
                        (btn) -> {
                            int nextMode = (CameraViewConfig.CROSSHAIR_MODE.get() + 1) % 3;
                            CameraViewConfig.CROSSHAIR_MODE.set(nextMode); //
                            btn.setMessage(Component.literal("Crosshair: " + modeNames[nextMode])); //
                        })
                .bounds(xRight, y + spacing, width, 20).build());

        // 3. Zoom Toggle
        this.addRenderableWidget(Button.builder(
                        Component.literal("Zoom: " + (CameraViewConfig.CLIENT.enableZoom.get() ? "Enabled" : "Disabled")),
                        (btn) -> {
                            boolean newState = !CameraViewConfig.CLIENT.enableZoom.get();
                            CameraViewConfig.CLIENT.enableZoom.set(newState);
                            btn.setMessage(Component.literal("Zoom: " + (newState ? "Enabled" : "Disabled")));
                        })
                .bounds(xRight, y + (spacing * 2), width, 20).build());

        // 4. Zoom Speed
        this.addRenderableWidget(new FloatSlider(xRight, y + (spacing * 3), width, "Zoom Speed",
                CameraViewConfig.CLIENT.zoomInSpeed.get(), 0.01, 1.0, (val) -> {
            CameraViewConfig.CLIENT.zoomInSpeed.set(val);
        }));

        // 5. Tracking Sensitivity
        this.addRenderableWidget(new FloatSlider(xRight, y + (spacing * 4), width, "Tracking Sens.",
                CameraViewConfig.CLIENT.lockOnSmoothness.get(), 0.001, 0.125, (val) -> {
            CameraViewConfig.CLIENT.lockOnSmoothness.set(val);
        }));

        // 6. Vignette Toggle
        this.addRenderableWidget(Button.builder(
                        Component.literal("Vignette: " + (CameraViewConfig.CLIENT.enableVignette.get() ? "Enabled" : "Disabled")),
                        (btn) -> {
                            boolean newState = !CameraViewConfig.CLIENT.enableVignette.get();
                            CameraViewConfig.CLIENT.enableVignette.set(newState);
                            btn.setMessage(Component.literal("Vignette: " + (newState ? "Enabled" : "Disabled")));
                        })
                .bounds(xRight, y + (spacing * 5), width, 20).build());


        // init() metodu içindeki renk listesi:
        String[] styleNames = {"Arrow", "Classic", "Dot", "Circle", "Tactical", "Double Ring", "Corners"};

        this.addRenderableWidget(Button.builder(
                        Component.literal("Style: " + styleNames[CameraViewConfig.CLIENT.crosshairStyle.get()]),
                        (btn) -> {
                            // 2. Modulo değerini % 7 yap (0-6 arası dönsün)
                            int nextStyle = (CameraViewConfig.CLIENT.crosshairStyle.get() + 1) % 7;
                            CameraViewConfig.CLIENT.crosshairStyle.set(nextStyle);
                            btn.setMessage(Component.literal("Style: " + styleNames[nextStyle]));
                        })
                .bounds(xRight, y + (spacing * 6), width, 20).build());

        // --- RENK LİSTESİ VE BUTONU ---
        String[] colorNames = {"Turquoise", "Green", "Dark Blue", "Red", "White"};

        this.addRenderableWidget(Button.builder(
                        // Mevcut rengin ismini gösterir
                        Component.literal("Color: " + colorNames[CameraViewConfig.CROSSHAIR_COLOR_INDEX.get()]),
                        (btn) -> {
                            // Renk indeksini bir artırır, 5'e gelince (0,1,2,3,4) başa döner
                            int nextColor = (CameraViewConfig.CROSSHAIR_COLOR_INDEX.get() + 1) % 5;

                            // ÖNEMLİ: Burada mutlaka CROSSHAIR_COLOR_INDEX kullanılmalı!
                            CameraViewConfig.CROSSHAIR_COLOR_INDEX.set(nextColor);

                            // Buton üzerindeki metni günceller
                            btn.setMessage(Component.literal("Color: " + colorNames[nextColor]));
                        })
                .bounds(xRight, y + (spacing * 7), width, 20).build());

        this.addRenderableWidget(CycleButton.builder((CameraViewConfig.VisualStyle style) ->
                        Component.literal(style.name().charAt(0) + style.name().substring(1).toLowerCase()))
                .withValues(CameraViewConfig.VisualStyle.values())
                .withInitialValue(CameraViewConfig.CLIENT.visualStyle.get())
                .displayOnlyValue()
                .create(xRight, y + (spacing * 8), width, 20, Component.literal("Icon Style"), (button, value) -> {
                    CameraViewConfig.CLIENT.visualStyle.set(value);
                }));

// İkon Y-Offset Slider (Kameradan bağımsız yükseklik ayarı)
        this.addRenderableWidget(new FloatSlider(xRight, y + (spacing * 9), width, "Icon Y-Offset", CameraViewConfig.CLIENT.iconYOffset.get(), -2.0, 2.0, (val) -> {
            CameraViewConfig.CLIENT.iconYOffset.set(val);
        }));

        // YENİ: Parallax Assist Butonu
        this.addRenderableWidget(CycleButton.onOffBuilder(CameraViewConfig.CLIENT.enableParallaxAssist.get())
                .create(xLeft, y + (spacing * 6), width, 20, Component.literal("Parallax Assist"), (btn, val) -> {
                    CameraViewConfig.CLIENT.enableParallaxAssist.set(val);
                }));

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        CameraViewConfig.SPEC.save(); // Ayarları dosyaya yazar
        super.onClose();
    }

    private static class FloatSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final Consumer<Double> setter;

        public FloatSlider(int x, int y, int w, String label, double current, double min, double max, Consumer<Double> setter) {
            super(x, y, w, 20, Component.literal(""), Mth.clamp((current - min) / (max - min), 0.0, 1.0));
            this.label = label;
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
            setMessage(Component.literal(label + ": " + String.format("%.3f", val)));
        }
    }
}