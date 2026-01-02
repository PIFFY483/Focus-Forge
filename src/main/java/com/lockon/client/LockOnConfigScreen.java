package com.lockon.client;

import com.lockon.config.LockOnConfig;
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
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.function.Consumer;

public class LockOnConfigScreen extends Screen {

    private final Screen parent;
    private boolean showVisualSettings = false;

    // --- LAYOUT CONSTANTS (4 Sütun için) ---
    private static final int BUTTON_WIDTH = 95;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_HEIGHT = 22;
    private static final int COL_GAP = 4;

    // 4 Sütun Konumları
    private int xCol1;
    private int xCol2;
    private int xCol3;
    private int xCol4;

    // --- TEMPORARY LOCK SETTINGS ---
    private boolean tempEnableMouseInputWarning;
    private double tempCrosshairXZSize;
    private double tempCrosshairYSize;


    private double tempLockSpeed;
    private double tempMaxSmoothingFactor;
    private double tempMaxLockDistance;
    private double tempMaxDisengagementRange;
    private double tempMaxVerticalOffset;
    private double tempCameraFocusOffset;
    private double tempUnlockCooldownSeconds;
    private double tempTargetSwitchCooldownSeconds;
    private boolean tempUsePlayerAttackRange;
    private double tempMaxLosCheckDistance;

    private int tempTargetScanFrequency;
    private boolean tempEnableTargetBlacklist;
    private boolean tempTargetPlayers;
    private double tempMaxLockAngle;
    private boolean tempBreakLockOnLosBreak;
    // ------------------------------------------------------------------------------------------------

    public LockOnConfigScreen(Screen parent) {
        super(Component.translatable("lockon.config.title"));
        this.parent = parent;
        this.loadCurrentConfig();
    }

    private void loadCurrentConfig() {

        this.tempEnableMouseInputWarning = LockOnConfig.ENABLE_MOUSE_INPUT_WARNING.get();

        this.tempCrosshairXZSize = LockOnConfig.CROSSHAIR_XZ_SIZE.get();
        this.tempCrosshairYSize = LockOnConfig.CROSSHAIR_Y_SIZE.get();
        this.tempLockSpeed = LockOnConfig.LOCK_SPEED.get();
        this.tempMaxSmoothingFactor = LockOnConfig.MAX_SMOOTHING_FACTOR.get();
        this.tempMaxLockDistance = LockOnConfig.MAX_LOCK_DISTANCE.get();
        this.tempMaxDisengagementRange = LockOnConfig.MAX_DISENGAGEMENT_RANGE.get();
        this.tempMaxVerticalOffset = LockOnConfig.MAX_VERTICAL_OFFSET.get();
        this.tempCameraFocusOffset = LockOnConfig.CAMERA_FOCUS_OFFSET.get();
        this.tempUnlockCooldownSeconds = LockOnConfig.UNLOCK_COOLDOWN_SECONDS.get();
        this.tempTargetSwitchCooldownSeconds = LockOnConfig.TARGET_SWITCH_COOLDOWN_SECONDS.get();
        this.tempUsePlayerAttackRange = LockOnConfig.USE_PLAYER_ATTACK_RANGE.get();
        this.tempMaxLosCheckDistance = LockOnConfig.MAX_LOS_CHECK_DISTANCE.get();
        this.tempTargetScanFrequency = LockOnConfig.TARGET_SCAN_FREQUENCY.get();
        this.tempEnableTargetBlacklist = LockOnConfig.ENABLE_TARGET_BLACKLIST.get();
        this.tempTargetPlayers = LockOnConfig.TARGET_PLAYERS.get();
        this.tempMaxLockAngle = LockOnConfig.MAX_LOCK_ANGLE.get();
        this.tempBreakLockOnLosBreak = LockOnConfig.BREAK_LOCK_ON_LOS_BREAK.get();
    }

    @Override
    protected void init() {
        int totalWidth = 4 * BUTTON_WIDTH + 3 * COL_GAP;
        this.xCol1 = (this.width - totalWidth) / 2;
        this.xCol2 = this.xCol1 + BUTTON_WIDTH + COL_GAP;
        this.xCol3 = this.xCol2 + BUTTON_WIDTH + COL_GAP;
        this.xCol4 = this.xCol3 + BUTTON_WIDTH + COL_GAP;

        this.clearWidgets();

        if (this.showVisualSettings) {
            this.initVisualSettings();
        } else {
            this.initMainSettings();
        }


        this.addRenderableWidget(new IntSlider(xCol4, 40, BUTTON_WIDTH, "ICON SIZE", (int)(LockOnConfig.CROSSHAIR_XZ_SIZE.get() * 100), 10, 500, (val) -> {
            LockOnConfig.CROSSHAIR_XZ_SIZE.set(val / 100.0);
        }));

        // DONE BUTONU
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 29, 200, BUTTON_HEIGHT).build());
    }

    private void initMainSettings() {
        int y = 30;

        // Lock Mechanism Title
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.translatable("lockon.config.title.lock_mechanism")));
        y += ROW_HEIGHT;

        // Row 1: 4 Short Options
        this.addRenderableWidget(new FloatSlider(this.xCol1, y, BUTTON_WIDTH, "lockon.config.lock_speed", this.tempLockSpeed, 0.01, 1.0, 2, (v) -> this.tempLockSpeed = v));
        this.addRenderableWidget(new FloatSlider(this.xCol2, y, BUTTON_WIDTH, "lockon.config.max_smoothing_factor", this.tempMaxSmoothingFactor, 0.01, 1.0, 2, (v) -> this.tempMaxSmoothingFactor = v));
        this.addRenderableWidget(new FloatSlider(this.xCol3, y, BUTTON_WIDTH, "lockon.config.max_lock_distance", this.tempMaxLockDistance, 10.0, 128.0, 0, (v) -> this.tempMaxLockDistance = v));
        this.addRenderableWidget(new FloatSlider(this.xCol4, y, BUTTON_WIDTH, "lockon.config.max_disengagement_range", this.tempMaxDisengagementRange, 10.0, 150.0, 0, (v) -> this.tempMaxDisengagementRange = v));
        y += ROW_HEIGHT;

        // Row 2: 4 Short Options
        this.addRenderableWidget(new FloatSlider(this.xCol1, y, BUTTON_WIDTH, "lockon.config.max_vertical_offset", this.tempMaxVerticalOffset, 0.0, 5.0, 1, (v) -> this.tempMaxVerticalOffset = v));
        this.addRenderableWidget(new FloatSlider(this.xCol2, y, BUTTON_WIDTH, "lockon.config.camera_focus_offset", this.tempCameraFocusOffset, -1.0, 2.0, 2, (v) -> this.tempCameraFocusOffset = v));
        this.addRenderableWidget(new FloatSlider(this.xCol3, y, BUTTON_WIDTH, "lockon.config.max_los_check_distance", this.tempMaxLosCheckDistance, 1.0, 128.0, 0, (v) -> this.tempMaxLosCheckDistance = v));
        this.addRenderableWidget(this.createShortCycleButton(this.xCol4, y, "lockon.config.use_player_attack_range", this.tempUsePlayerAttackRange, (v) -> this.tempUsePlayerAttackRange = v));
        y += ROW_HEIGHT;

        // Row 3: 2 Short Options (Ortalanmış)
        this.addRenderableWidget(new FloatSlider(this.xCol1, y, BUTTON_WIDTH, "lockon.config.unlock_cooldown", this.tempUnlockCooldownSeconds, 0.0, 5.0, 2, (v) -> this.tempUnlockCooldownSeconds = v));
        this.addRenderableWidget(new FloatSlider(this.xCol2, y, BUTTON_WIDTH, "lockon.config.switch_cooldown", this.tempTargetSwitchCooldownSeconds, 0.0, 5.0, 2, (v) -> this.tempTargetSwitchCooldownSeconds = v));
        y += ROW_HEIGHT;

        y += 10;

        // Targeting Title
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.translatable("lockon.config.title.targeting")));
        y += ROW_HEIGHT;

        // Row 4: 4 Short Options
        this.addRenderableWidget(this.createShortCycleButton(this.xCol1, y, "lockon.config.target_players", this.tempTargetPlayers, (v) -> this.tempTargetPlayers = v));
        this.addRenderableWidget(this.createShortCycleButton(this.xCol2, y, "lockon.config.enable_blacklist", this.tempEnableTargetBlacklist, (v) -> this.tempEnableTargetBlacklist = v));
        this.addRenderableWidget(new FloatSlider(this.xCol3, y, BUTTON_WIDTH, "lockon.config.max_lock_angle", this.tempMaxLockAngle, 5.0, 180.0, 0, (v) -> this.tempMaxLockAngle = v));
        this.addRenderableWidget(new IntSlider(this.xCol4, y, BUTTON_WIDTH, "lockon.config.scan_frequency", this.tempTargetScanFrequency, 1, 60, (v) -> this.tempTargetScanFrequency = v));
        y += ROW_HEIGHT;

        // Row 5: 2 Sütun Genişliğinde Uzun Seçenek (Ortalanmış)
        int wideButtonWidth = 2 * BUTTON_WIDTH + COL_GAP;
        int wideButtonX = this.xCol1;
        this.addRenderableWidget(CycleButton.booleanBuilder(CommonComponents.OPTION_ON, CommonComponents.OPTION_OFF)
                .withInitialValue(this.tempBreakLockOnLosBreak)
                .create(wideButtonX, y, wideButtonWidth, BUTTON_HEIGHT, getShortKeyComponent("lockon.config.break_lock_on_los_break"), (b, v) -> this.tempBreakLockOnLosBreak = v));
        y += ROW_HEIGHT;

        y += 10;

        // Block Lists Title
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.translatable("lockon.config.title.block_lists")));
        y += ROW_HEIGHT;

        // Row 6: 4 Button Options (Listeler ve Sayfa Geçiş)

        // ACQUISITION LIST
        this.addRenderableWidget(Button.builder(getShortKeyComponent("lockon.config.button.acquisition_list"), (button) -> {
            this.minecraft.setScreen(new LockOnBlockListScreen(
                    this,
                    Component.translatable("lockon.config.title.acquisition_list"),
                    LockOnConfig.lockAcquisitionBlockList,
                    false
            ));
        }).bounds(this.xCol1, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        // PRECLUSION LIST
        this.addRenderableWidget(Button.builder(getShortKeyComponent("lockon.config.button.preclusion_list"), (button) -> {
            this.minecraft.setScreen(new LockOnBlockListScreen(
                    this,
                    Component.translatable("lockon.config.title.preclusion_list"),
                    LockOnConfig.lockPreclusionBlockList,
                    true
            ));
        }).bounds(this.xCol2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        // ENTITY BLACKLIST
        this.addRenderableWidget(Button.builder(getShortKeyComponent("lockon.config.button.entity_blacklist"), (button) -> {
            this.minecraft.setScreen(new LockOnEntityListScreen(
                    this,
                    LockOnConfig.TARGET_BLACKLIST,
                    Component.translatable("lockon.config.title.entity_blacklist")
            ));
        }).bounds(this.xCol3, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        // Visual Settings Toggle
        this.addRenderableWidget(Button.builder(Component.translatable("lockon.config.button.visual_settings"), (button) -> {
            this.showVisualSettings = true;
            this.init(this.minecraft, this.width, this.height);
        }).bounds(this.xCol4, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        y += ROW_HEIGHT;
    }

    private void initVisualSettings() {
        int y = 30;

        // Visuals Title
        this.addRenderableWidget(new TitleWidget(this.width / 2, y, Component.translatable("lockon.config.title.visuals")));
        y += ROW_HEIGHT + 10; // Başlıktan sonra biraz daha fazla boşluk

        // Row 1: Temel Görsel Uyarılar ve Boyut
        this.addRenderableWidget(this.createShortCycleButton(this.xCol1, y, "lockon.config.mouse_warning", this.tempEnableMouseInputWarning, (v) -> this.tempEnableMouseInputWarning = v));

        // ICON SIZE (XZ) - xCol2
        this.addRenderableWidget(new FloatSlider(this.xCol2, y, BUTTON_WIDTH, "ICON SIZE XZ", this.tempCrosshairXZSize, 0.1, 5.0, 1, (v) -> this.tempCrosshairXZSize = v));

        // ICON Y SIZE - xCol3
        this.addRenderableWidget(new FloatSlider(this.xCol3, y, BUTTON_WIDTH, "ICON SIZE Y", this.tempCrosshairYSize, 0.1, 5.0, 1, (v) -> this.tempCrosshairYSize = v));

        y += ROW_HEIGHT + 20; // Alt bölüme geçmeden önce belirgin boşluk


        // Main Settings Toggle (Alt kısma ortalanmış buton)
        int toggleButtonWidth = 2 * BUTTON_WIDTH + COL_GAP;
        int toggleButtonX = (this.width / 2) - toggleButtonWidth / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("lockon.config.button.main_settings"), (button) -> {
            this.showVisualSettings = false;
            this.init(this.minecraft, this.width, this.height);
        }).bounds(toggleButtonX, y, toggleButtonWidth, BUTTON_HEIGHT).build());
    }

    // Yardımcı Metot: Key'den kısa, büyük harfli Component oluşturur
    private Component getShortKeyComponent(String key) {
        String shortKey = key.substring(key.lastIndexOf('.') + 1).replace('_', ' ').toUpperCase();
        return Component.literal(shortKey);
    }

    // Yardımcı Metot: Kısa Başlıklı Boolean CycleButton oluşturur
    private CycleButton<Boolean> createShortCycleButton(int x, int y, String key, boolean initialValue, Consumer<Boolean> consumer) {
        return CycleButton.booleanBuilder(CommonComponents.OPTION_ON, CommonComponents.OPTION_OFF)
                .withInitialValue(initialValue)
                .create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, getShortKeyComponent(key), (b, v) -> consumer.accept(v));
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.saveConfig();
        LockOnConfig.CLIENT_SPEC.save();
        this.minecraft.setScreen(this.parent);
    }

    private void saveConfig() {
        LockOnConfig.ENABLE_MOUSE_INPUT_WARNING.set(this.tempEnableMouseInputWarning);

        LockOnConfig.CROSSHAIR_XZ_SIZE.set(this.tempCrosshairXZSize);
        LockOnConfig.CROSSHAIR_Y_SIZE.set(this.tempCrosshairYSize);
        LockOnConfig.LOCK_SPEED.set(this.tempLockSpeed);
        LockOnConfig.MAX_SMOOTHING_FACTOR.set(this.tempMaxSmoothingFactor);
        LockOnConfig.MAX_LOCK_DISTANCE.set(this.tempMaxLockDistance);
        LockOnConfig.MAX_DISENGAGEMENT_RANGE.set(this.tempMaxDisengagementRange);
        LockOnConfig.MAX_VERTICAL_OFFSET.set(this.tempMaxVerticalOffset);
        LockOnConfig.CAMERA_FOCUS_OFFSET.set(this.tempCameraFocusOffset);
        LockOnConfig.UNLOCK_COOLDOWN_SECONDS.set(this.tempUnlockCooldownSeconds);
        LockOnConfig.TARGET_SWITCH_COOLDOWN_SECONDS.set(this.tempTargetSwitchCooldownSeconds);
        LockOnConfig.USE_PLAYER_ATTACK_RANGE.set(this.tempUsePlayerAttackRange);
        LockOnConfig.MAX_LOS_CHECK_DISTANCE.set(this.tempMaxLosCheckDistance);
        LockOnConfig.TARGET_SCAN_FREQUENCY.set(this.tempTargetScanFrequency);
        LockOnConfig.ENABLE_TARGET_BLACKLIST.set(this.tempEnableTargetBlacklist);
        LockOnConfig.TARGET_PLAYERS.set(this.tempTargetPlayers);
        LockOnConfig.MAX_LOCK_ANGLE.set(this.tempMaxLockAngle);
        LockOnConfig.BREAK_LOCK_ON_LOS_BREAK.set(this.tempBreakLockOnLosBreak);
    }

    // --- ÖZEL WIDGET'LAR ---

    // Başlık Widget'ı (Çeviri kullanır)
    private static class TitleWidget extends AbstractWidget {
        public TitleWidget(int x, int y, Component title) {
            super(x - 100, y, 200, 20, title);
        }
        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            g.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput neo) {}
    }

    // Float Slider (Kısa anahtar gösterir)
    private class FloatSlider extends AbstractSliderButton {
        private final String key;
        private final Consumer<Double> consumer;
        private final double min, max;
        private final int dec;

        public FloatSlider(int x, int y, int w, String k, double init, double min, double max, int d, Consumer<Double> c) {
            super(x, y, w, 20, Component.literal(""), Mth.clamp((init - min) / (max - min), 0.0, 1.0));
            this.key = k; this.min = min; this.max = max; this.dec = d; this.consumer = c;
            this.updateMessage();
        }
        @Override
        protected void applyValue() { consumer.accept(min + (max - min) * value); updateMessage(); }
        @Override
        protected void updateMessage() {
            double v = min + (max - min) * value;
            String shortKey = key.substring(key.lastIndexOf('.') + 1).replace('_', ' ').toUpperCase();
            setMessage(Component.literal(shortKey).append(": ").append(String.format("%." + dec + "f", v)));
        }
    }

    // Int Slider (Kısa anahtar gösterir)
    private class IntSlider extends AbstractSliderButton {
        private final String key;
        private final Consumer<Integer> consumer;
        private final int min, max;

        public IntSlider(int x, int y, int w, String k, int init, int min, int max, Consumer<Integer> c) {
            super(x, y, w, 20, Component.literal(""), Mth.clamp((double)(init - min) / (max - min), 0.0, 1.0));
            this.key = k; this.min = min; this.max = max; this.consumer = c;
            this.updateMessage();
        }
        @Override
        protected void applyValue() { consumer.accept((int)Math.round(min + (max - min) * value)); updateMessage(); }
        @Override
        protected void updateMessage() {
            int v = (int)Math.round(min + (max - min) * value);
            String shortKey = key.substring(key.lastIndexOf('.') + 1).replace('_', ' ').toUpperCase();
            setMessage(Component.literal(shortKey).append(": ").append(String.valueOf(v)));
        }
    }

    // Color Slider (Kısa anahtar gösterir)
    private class ColorSlider extends AbstractSliderButton {
        private final String key;
        private final Consumer<Integer> consumer;

        public ColorSlider(int x, int y, int w, String k, int init, Consumer<Integer> c) {
            super(x, y, w, 20, Component.literal(""), Mth.clamp(init / 255.0, 0.0, 1.0));
            this.key = k; this.consumer = c;
            this.updateMessage();
        }
        @Override
        protected void applyValue() { consumer.accept((int)Math.round(value * 255.0)); updateMessage(); }
        @Override
        protected void updateMessage() {
            int v = (int)Math.round(value * 255.0);
            String shortKey = key.substring(key.lastIndexOf('.') + 1).replace('_', ' ').toUpperCase();
            setMessage(Component.literal(shortKey).append(": ").append(String.valueOf(v)));
        }
    }
}