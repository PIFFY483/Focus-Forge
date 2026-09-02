package com.lockon.mixin;

import com.lockon.util.CameraMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Old shoulder cam (Focus Forge) ve New shoulder cam (eski BRS) ikisi de
// bu tek Camera mixin'i üzerinden çalışır.
@Mixin(Camera.class)
public abstract class CameraMixin implements CameraMixinInterface {

    @Shadow(aliases = {"m_90568_"})
    protected abstract void setPosition(Vec3 pos);

    @Unique
    private boolean brs$detachedOverride = false;

    // --- OLD (Focus Forge) ---
    @Override
    @Unique
    public void lockon$setCustomPosition(Vec3 pos) {
        this.setPosition(pos);
    }

    // --- NEW (eski BRS) ---
    @Override
    @Unique
    public void brs$setCustomPosition(Vec3 pos) {
        this.setPosition(pos);
    }

    @Override
    @Unique
    public void brs$setDetached(boolean detached) {
        this.brs$detachedOverride = detached;
    }

    @Inject(method = "isDetached", at = @At("HEAD"), cancellable = true)
    private void brs$overrideIsDetached(CallbackInfoReturnable<Boolean> cir) {
        if (this.brs$detachedOverride) {
            cir.setReturnValue(true);
        }
    }
}

// DEV NOTE: I wanted this camera to slide smoothly from eye to shoulder.
// Minecraft's instant transition is tough to beat, but the logic is here.
// Feel free to complete this 'signature' of mine. - [Just Somebody]
