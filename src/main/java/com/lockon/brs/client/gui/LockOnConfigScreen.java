package com.lockon.brs.client.gui;

import com.lockon.brs.config.LockOnConfig;
import com.lockon.shared.gui.LockOnBlockListScreen;
import com.lockon.shared.gui.LockOnEntityListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class LockOnConfigScreen extends Screen {
    private final Screen parent;

    private static final String[] CROSSHAIR_STYLES =
            {"Crosshair", "Hexagon", "Star", "Vanguard", "Trinity",
                    "Hunter", "Chronos", "Black Star", "Cannon Sight",
                    "Blade Mark", "Death Skull", "BRS Sigil"};
    private static final String[] CROSSHAIR_COLORS =
            {"Turquoise", "Green", "Blue", "Red", "White"};

    // ── Kompakt boyutlar ──
    private static final int BUTTON_WIDTH = 80;   // 95 → 80
    private static final int BUTTON_HEIGHT = 18;  // 20 → 18
    private static final int ROW_HEIGHT = 20;     // 22 → 20
    private static final int COL_GAP = 3;         // 4 → 3
    private static final int TITLE_GAP = 4;       // başlık sonrası boşluk

    private int xCol1, xCol2, xCol3, xCol4;

    public LockOnConfigScreen(Screen parent) {
        super(Component.literal("BRS Lock-On Ayarları"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int totalWidth = 4 * BUTTON_WIDTH + 3 * COL_GAP;
        this.xCol1 = (this.width - totalWidth) / 2;
        this.xCol2 = this.xCol1 + BUTTON_WIDTH + COL_GAP;
        this.xCol3 = this.xCol2 + BUTTON_WIDTH + COL_GAP;
        this.xCol4 = this.xCol3 + BUTTON_WIDTH + COL_GAP;
        this.clearWidgets();

        int y = 22; // 30 → 22 (üst boşluk azaltıldı)

        // ── KİLİT MEKANİZMASI ──
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.literal("KİLİT MEKANİZMASI")));
        y += ROW_HEIGHT - TITLE_GAP;

        // Row 1
        this.addRenderableWidget(new FloatSlider(xCol1, y, BUTTON_WIDTH, "Kilit Hızı",
                LockOnConfig.LOCK_SPEED.get(), 0.01, 1.0, 2, v -> LockOnConfig.LOCK_SPEED.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol2, y, BUTTON_WIDTH, "Yumuşatma",
                LockOnConfig.MAX_SMOOTHING_FACTOR.get(), 0.01, 1.0, 2, v -> LockOnConfig.MAX_SMOOTHING_FACTOR.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol3, y, BUTTON_WIDTH, "Kilit Mesafesi",
                LockOnConfig.MAX_LOCK_DISTANCE.get(), 5.0, 100.0, 0, v -> LockOnConfig.MAX_LOCK_DISTANCE.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol4, y, BUTTON_WIDTH, "Kopma Mesafesi",
                LockOnConfig.MAX_DISENGAGEMENT_RANGE.get(), 5.0, 150.0, 0, v -> LockOnConfig.MAX_DISENGAGEMENT_RANGE.set(v)));
        y += ROW_HEIGHT;

        // Row 2
        this.addRenderableWidget(new FloatSlider(xCol1, y, BUTTON_WIDTH, "Kilit Açısı",
                LockOnConfig.MAX_LOCK_ANGLE.get(), 10.0, 180.0, 0, v -> LockOnConfig.MAX_LOCK_ANGLE.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol2, y, BUTTON_WIDTH, "Dikey Tolerans",
                LockOnConfig.MAX_VERTICAL_OFFSET.get(), 5.0, 90.0, 0, v -> LockOnConfig.MAX_VERTICAL_OFFSET.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol3, y, BUTTON_WIDTH, "Odak Offset",
                LockOnConfig.CAMERA_FOCUS_OFFSET.get(), -5.0, 5.0, 2, v -> LockOnConfig.CAMERA_FOCUS_OFFSET.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol4, y, BUTTON_WIDTH, "Odak Oranı",
                LockOnConfig.DYNAMIC_FOCUS_HEIGHT_RATIO.get(), 0.1, 1.0, 2, v -> LockOnConfig.DYNAMIC_FOCUS_HEIGHT_RATIO.set(v)));
        y += ROW_HEIGHT;

        // Row 3
        this.addRenderableWidget(new FloatSlider(xCol1, y, BUTTON_WIDTH, "Açma CD",
                LockOnConfig.UNLOCK_COOLDOWN_SECONDS.get(), 0.0, 5.0, 2, v -> LockOnConfig.UNLOCK_COOLDOWN_SECONDS.set(v)));
        this.addRenderableWidget(new FloatSlider(xCol2, y, BUTTON_WIDTH, "Değiş. CD",
                LockOnConfig.TARGET_SWITCH_COOLDOWN_SECONDS.get(), 0.0, 10.0, 2, v -> LockOnConfig.TARGET_SWITCH_COOLDOWN_SECONDS.set(v)));
        y += ROW_HEIGHT + 6;

        // ── HEDEFLEME ──
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.literal("HEDEFLEME")));
        y += ROW_HEIGHT - TITLE_GAP;

        // Row 4
        this.addRenderableWidget(CycleButton.onOffBuilder(LockOnConfig.TARGET_PLAYERS.get())
                .create(xCol1, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Oyuncuları Hedefle"),
                        (b, v) -> LockOnConfig.TARGET_PLAYERS.set(v)));
        this.addRenderableWidget(CycleButton.onOffBuilder(LockOnConfig.ENABLE_TARGET_BLACKLIST.get())
                .create(xCol2, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Blacklist"),
                        (b, v) -> LockOnConfig.ENABLE_TARGET_BLACKLIST.set(v)));
        this.addRenderableWidget(new IntSlider(xCol3, y, BUTTON_WIDTH, "Tarama Sıklığı",
                LockOnConfig.TARGET_SCAN_FREQUENCY.get(), 1, 60, v -> LockOnConfig.TARGET_SCAN_FREQUENCY.set(v)));
        this.addRenderableWidget(CycleButton.onOffBuilder(LockOnConfig.BREAK_LOCK_ON_LOS_BREAK.get())
                .create(xCol4, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("LOS Break"),
                        (b, v) -> LockOnConfig.BREAK_LOCK_ON_LOS_BREAK.set(v)));
        y += ROW_HEIGHT + 6;

        // ── BLOK LİSTELERİ ──
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.literal("BLOK LİSTELERİ")));
        y += ROW_HEIGHT - TITLE_GAP;

        // Row 5
        this.addRenderableWidget(Button.builder(Component.literal("Acquisition"), button -> {
            this.minecraft.setScreen(new LockOnBlockListScreen(this,
                    Component.literal("Acquisition List (Kilit Edinme)"),
                    LockOnConfig.LOCK_ACQUISITION_BLOCK_LIST, false));
        }).bounds(xCol1, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("Preclusion"), button -> {
            this.minecraft.setScreen(new LockOnBlockListScreen(this,
                    Component.literal("Preclusion List (Kilit Koparma)"),
                    LockOnConfig.LOCK_PRECLUSION_BLOCK_LIST, true));
        }).bounds(xCol2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("Entity Blacklist"), button -> {
            this.minecraft.setScreen(new LockOnEntityListScreen(this,
                    LockOnConfig.TARGET_BLACKLIST, Component.literal("Entity Blacklist")));
        }).bounds(xCol3, y, BUTTON_WIDTH + BUTTON_WIDTH + COL_GAP, BUTTON_HEIGHT).build());
        // Entity Blacklist 2 sütun genişliğinde (daha okunabilir)

        y += ROW_HEIGHT + 6;

        // ── GÖRSEL ──
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.literal("GÖRSEL")));
        y += ROW_HEIGHT - TITLE_GAP;

        // Row 6
        this.addRenderableWidget(Button.builder(
                Component.literal("Şekil: " + CROSSHAIR_STYLES[LockOnConfig.CROSSHAIR_STYLE.get()]),
                btn -> {
                    int next = (LockOnConfig.CROSSHAIR_STYLE.get() + 1) % CROSSHAIR_STYLES.length;
                    LockOnConfig.CROSSHAIR_STYLE.set(next);
                    btn.setMessage(Component.literal("Şekil: " + CROSSHAIR_STYLES[next]));
                }).bounds(xCol1, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(new FloatSlider(xCol2, y, BUTTON_WIDTH, "İkon Boyutu",
                LockOnConfig.CROSSHAIR_SIZE.get(), 0.1, 5.0, 2,
                v -> LockOnConfig.CROSSHAIR_SIZE.set(v)));

        this.addRenderableWidget(Button.builder(
                Component.literal("Renk: " + CROSSHAIR_COLORS[LockOnConfig.CROSSHAIR_COLOR_INDEX.get()]),
                btn -> {
                    int next = (LockOnConfig.CROSSHAIR_COLOR_INDEX.get() + 1) % CROSSHAIR_COLORS.length;
                    LockOnConfig.CROSSHAIR_COLOR_INDEX.set(next);
                    btn.setMessage(Component.literal("Renk: " + CROSSHAIR_COLORS[next]));
                }).bounds(xCol3, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(new FloatSlider(xCol4, y, BUTTON_WIDTH, "İkon Y",
                LockOnConfig.ICON_Y_OFFSET.get(), -2.0, 2.0, 2,
                v -> LockOnConfig.ICON_Y_OFFSET.set(v)));
        y += ROW_HEIGHT + 6;

        // ── KAMERA (Lock-On sırasında dinamik mesafe ayarları - ayrı alt ekranda) ──
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.literal("KAMERA")));
        y += ROW_HEIGHT - TITLE_GAP;

        // Row 7 — tek buton, alt ekrana yönlendirir (Block List / Entity Blacklist ile aynı desen)
        this.addRenderableWidget(Button.builder(Component.literal("Dinamik Kamera Mesafesi"), button -> {
            this.minecraft.setScreen(new DynamicLockDistanceConfigScreen(this));
        }).bounds(xCol1, y, BUTTON_WIDTH + BUTTON_WIDTH + COL_GAP, BUTTON_HEIGHT).build());
        y += ROW_HEIGHT + 12;

        // ── Done Button — sabit alt konum yerine içerik sonuna göre; pencere küçükse üst üste binmeyi engeller ──
        int doneY = Math.max(y, this.height - 30);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, doneY, 200, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 6, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    // ── Custom Widgets ──
    private static class TitleWidget extends AbstractWidget {
        public TitleWidget(int x, int y, Component title) {
            super(x - 80, y, 160, 14, title);
        }

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            g.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                    this.getX() + this.width / 2, this.getY() + 2, 0xAAAAAA);
            // Başlık altı çizgi
            int lineY = this.getY() + this.height - 2;
            g.fill(this.getX() - 20, lineY, this.getX() + this.width + 20, lineY + 1, 0x44FFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput neo) {}
    }

    private class FloatSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final int decimals;
        private final Consumer<Double> consumer;

        public FloatSlider(int x, int y, int w, String label, double init, double min, double max, int decimals, Consumer<Double> consumer) {
            super(x, y, w, 18, Component.literal(""), Mth.clamp((init - min) / (max - min), 0.0, 1.0));
            this.label = label;
            this.min = min;
            this.max = max;
            this.decimals = decimals;
            this.consumer = consumer;
            this.updateMessage();
        }

        @Override
        protected void applyValue() {
            consumer.accept(min + (max - min) * value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double v = min + (max - min) * value;
            setMessage(Component.literal(label).append(": ").append(String.format("%." + decimals + "f", v)));
        }
    }

    private class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min, max;
        private final Consumer<Integer> consumer;

        public IntSlider(int x, int y, int w, String label, int init, int min, int max, Consumer<Integer> consumer) {
            super(x, y, w, 18, Component.literal(""), Mth.clamp((double)(init - min) / (max - min), 0.0, 1.0));
            this.label = label;
            this.min = min;
            this.max = max;
            this.consumer = consumer;
            this.updateMessage();
        }

        @Override
        protected void applyValue() {
            consumer.accept((int)Math.round(min + (max - min) * value));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int v = (int)Math.round(min + (max - min) * value);
            setMessage(Component.literal(label).append(": ").append(String.valueOf(v)));
        }
    }
}