package com.lockon.brs.client;

import com.lockon.brs.energy.ClientEnergyCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class EnergyBarOverlay implements IGuiOverlay {

    public static final EnergyBarOverlay INSTANCE = new EnergyBarOverlay();

    private static final int BAR_WIDTH = 200;   // önceki 182'den genişletildi
    private static final int BAR_HEIGHT = 10;   // yazı sığsın diye kalınlaştırıldı
    private static final int Y_OFFSET_FROM_BOTTOM = 36; // hotbar'ın hemen üstü

    private static final int COLOR_BACKGROUND = 0x80000000;
    private static final int COLOR_FILLED = 0xFF3FA8FF;
    private static final int COLOR_FLASH = 0xFFFFFFFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_USAGE_LABEL = 0xFFFF5555;
    private static final int COLOR_ENEMY_WARNING = 0xFFFF3333;

    private EnergyBarOverlay() {
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int centerX = screenWidth / 2;
        int left = centerX - BAR_WIDTH / 2;
        int top = screenHeight - Y_OFFSET_FROM_BOTTOM - BAR_HEIGHT;

        float percent = ClientEnergyCache.getPercentage();
        float flashPercent = ClientEnergyCache.getFlashPercentage();

        int filledWidth = Math.round(BAR_WIDTH * percent);
        int flashWidth = Math.round(BAR_WIDTH * flashPercent);

        // Arkaplan
        guiGraphics.fill(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, COLOR_BACKGROUND);

        // Eski (daha yüksek) seviye -> anlık beyaz flaş
        if (flashWidth > filledWidth) {
            guiGraphics.fill(left, top, left + flashWidth, top + BAR_HEIGHT, COLOR_FLASH);
        }

        // Güncel dolu kısım, flaşın üstüne çizilir
        if (filledWidth > 0) {
            guiGraphics.fill(left, top, left + filledWidth, top + BAR_HEIGHT, COLOR_FILLED);
        }

        String valueText = String.format("%.0f/%.0f",
                ClientEnergyCache.getEnergy(), ClientEnergyCache.getMaxEnergy());
        int textWidth = mc.font.width(valueText);
        int textX = left + (BAR_WIDTH - textWidth) / 2;
        int textY = top + (BAR_HEIGHT - mc.font.lineHeight) / 2;
        guiGraphics.drawString(mc.font, valueText, textX, textY, COLOR_TEXT, true);

        // Barın sağında: en son harcanan enerji (bir süre sonra kaybolur)
        if (ClientEnergyCache.hasRecentUsage()) {
            String usageText = String.format("-%.0f", ClientEnergyCache.getLastUsedAmount());
            int usageY = top + (BAR_HEIGHT - mc.font.lineHeight) / 2;
            guiGraphics.drawString(mc.font, usageText, left + BAR_WIDTH + 4, usageY, COLOR_USAGE_LABEL, true);
        }

        // Barın solunda: bize kilitlenmiş düşman varsa kırmızı uyarı
        if (ClientEnergyCache.hasLockedEnemy()) {
            String enemyText = I18n.get("hud.lockon.enemy_warning");
            int enemyY = top + (BAR_HEIGHT - mc.font.lineHeight) / 2;
            int enemyWidth = mc.font.width(enemyText);
            guiGraphics.drawString(mc.font, enemyText, left - enemyWidth - 4, enemyY, COLOR_ENEMY_WARNING, true);
        }
    }
}