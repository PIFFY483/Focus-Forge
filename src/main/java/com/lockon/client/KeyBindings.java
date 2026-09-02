package com.lockon.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    public static final String KEY_CATEGORY = "key.categories.lockon";

    public static final KeyMapping LOCK_KEY = new KeyMapping(
            "key.lockon.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            KEY_CATEGORY
    );

    public static final KeyMapping TARGET_SWITCH_KEY = new KeyMapping(
            "key.lockon.switch_target",
            InputConstants.Type.MOUSE,
            2,
            KEY_CATEGORY
    );

    // Kamera Ayar Ekranını Açan Tuş (Varsayılan: O)
    public static final KeyMapping CAMERA_CONFIG_KEY = new KeyMapping(
            "key.lockon.camera_config",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_O,
            KEY_CATEGORY
    );

    // Lock Type 1 <-> Lock Type 2 geçişi (Varsayılan: L)
    public static final KeyMapping LOCK_TYPE_TOGGLE_KEY = new KeyMapping(
            "key.lockon.lock_type_toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_L,
            KEY_CATEGORY
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(LOCK_KEY);
        event.register(TARGET_SWITCH_KEY);
        event.register(CAMERA_CONFIG_KEY);
        event.register(LOCK_TYPE_TOGGLE_KEY);
    }

    // Tuşa basıldığında  CameraConfigScreen ekranını açan iç sınıf
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientKeyHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (CAMERA_CONFIG_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new CameraConfigScreen());
            }

            if (LOCK_TYPE_TOGGLE_KEY.consumeClick()) {
                com.lockon.lock.LockType.toggle();
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "Lock Type: " + (com.lockon.lock.LockType.isType1() ? "1" : "2")),
                            true);
                }
            }
        }
    }
}