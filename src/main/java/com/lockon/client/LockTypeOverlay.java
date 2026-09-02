package com.lockon.client;

import com.lockon.lock.LockType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Sol üst köşede hangi kilitlenme (lock-on) sisteminin (Type 1: Focus Forge /
 * Type 2: eski BRS) aktif olduğunu gösteren küçük bir gösterge.
 * L tuşuyla değiştirilir (bkz. KeyBindings.LOCK_TYPE_TOGGLE_KEY).
 */
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
        String text = "Lock Type: " + (isType1 ? "1" : "2");
        int color = isType1 ? COLOR_TYPE_1 : COLOR_TYPE_2;

        // Shoulder Cam göstergesinin (satır 6) hemen altına.
        guiGraphics.drawString(mc.font, text, 6, 16, color, true);
    }
}
