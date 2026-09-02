package com.lockon.mixin.brs;

import com.lockon.brs.config.CameraConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void brs$interceptF5(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!CameraConfig.SKIP_FRONT_VIEW.get()) return;

        if (action != 1) return;

        if (mc.options.keyTogglePerspective.matches(key, scanCode)) {

            ci.cancel();

            if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
        }
    }
}
