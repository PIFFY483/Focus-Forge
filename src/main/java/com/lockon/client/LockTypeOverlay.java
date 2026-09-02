package com.lockon.client;

import com.lockon.lock.LockType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class LockTypeOverlay implements IGuiOverlay {

    public static final LockTypeOverlay INSTANCE = new LockTypeOverlay();

    private static final int COLOR_TYPE_1 = 0xFF55AAFF;
    private static final int COLOR_TYPE_2 = 0xFFFFAA00;

    private LockTypeOverlay() {
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        boolean isType1 = LockType.isType1();
        String text = I18n.get("hud.lockon.lock_type", isType1 ? "1" : "2");
        int color = isType1 ? COLOR_TYPE_1 : COLOR_TYPE_2;

        // Sol ALT: en alttaki satır
        int y = screenHeight - 6 - mc.font.lineHeight;
        guiGraphics.drawString(mc.font, text, 6, y, color, true);
    }
}