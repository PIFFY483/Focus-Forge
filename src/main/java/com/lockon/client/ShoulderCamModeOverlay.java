package com.lockon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ShoulderCamModeOverlay implements IGuiOverlay {

    public static final ShoulderCamModeOverlay INSTANCE = new ShoulderCamModeOverlay();

    // Her kamera modunun kendi rengi
    private static final int COLOR_CLASSIC    = 0xFFAAAAAA; // Classic (vanilla TPV)
    private static final int COLOR_OLD        = 0xFF55AAFF; // Old Shoulder
    private static final int COLOR_NEW        = 0xFFFFAA00; // New Shoulder
    private static final int COLOR_SEMI_ORBIT = 0xFF55FF55; // Semi Orbit
    private static final int COLOR_ORBIT      = 0xFFFF5555; // Orbit

    private ShoulderCamModeOverlay() {
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        int step = CameraConfigScreen.currentCameraModeStep();
        String text = I18n.get(CameraConfigScreen.cameraModeLabelKey(step));

        int color = switch (step) {
            case 0  -> COLOR_CLASSIC;
            case 1  -> COLOR_OLD;
            case 2  -> COLOR_NEW;
            case 3  -> COLOR_SEMI_ORBIT;
            case 4  -> COLOR_ORBIT;
            default -> COLOR_CLASSIC;
        };

        int y = screenHeight - 6 - (mc.font.lineHeight * 2) - 1;
        guiGraphics.drawString(mc.font, text, 6, y, color, true);
    }
}