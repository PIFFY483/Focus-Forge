package com.lockon.client;

import com.lockon.camera.ShoulderCamMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ShoulderCamModeOverlay implements IGuiOverlay {

    public static final ShoulderCamModeOverlay INSTANCE = new ShoulderCamModeOverlay();

    private static final int COLOR_OLD = 0xFF55AAFF;
    private static final int COLOR_NEW = 0xFFFFAA00;

    private ShoulderCamModeOverlay() {
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        boolean isOld = ShoulderCamMode.isOld();
        String modeLabel = I18n.get(isOld ? "hud.lockon.shoulder_cam.old" : "hud.lockon.shoulder_cam.new");
        String text = I18n.get("hud.lockon.shoulder_cam", modeLabel);
        int color = isOld ? COLOR_OLD : COLOR_NEW;

        // Sol ALT: üstteki satır (LockType'ın hemen üstü)
        int y = screenHeight - 6 - (mc.font.lineHeight * 2) - 1;
        guiGraphics.drawString(mc.font, text, 6, y, color, true);
    }
}