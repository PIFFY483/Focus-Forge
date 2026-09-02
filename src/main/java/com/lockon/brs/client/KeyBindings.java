package com.lockon.brs.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

public class KeyBindings {

    public static final String CATEGORY_BRS = "category.brs";

    public static final KeyMapping CAMERA_CONFIG_KEY = new KeyMapping(
            "key.brs.camera_config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            CATEGORY_BRS
    );

    public static final KeyMapping ORBIT_TOGGLE_KEY = new KeyMapping(
            "key.brs.orbit_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LALT,
            CATEGORY_BRS
    );

    // NOT: DASH_SKILL_KEY ve dash sistemi kaldırıldı.
}