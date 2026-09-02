package com.lockon.client;

import com.lockon.camera.ShoulderCamMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Ekranın sol üst köşesinde hangi omuz kamerasının (OLD: Focus Forge / NEW: eski BRS)
 * aktif olduğunu gösteren küçük bir gösterge.
 */
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
        String text = "Shoulder Cam: " + (isOld ? "OLD" : "NEW");
        int color = isOld ? COLOR_OLD : COLOR_NEW;

        guiGraphics.drawString(mc.font, text, 6, 6, color, true);
    }
}
