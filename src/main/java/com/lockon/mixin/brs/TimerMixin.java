package com.lockon.mixin.brs;

import com.lockon.brs.camera.HitStopController;
import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Timer.class)
public abstract class TimerMixin {

    @Shadow public float tickDelta;
    @Shadow public float partialTick;
    @Shadow private long lastMs;
    @Shadow @Final private float msPerTick;

    @Inject(method = "advanceTime", at = @At("HEAD"), cancellable = true)
    private void brs$scaledAdvanceTime(long l, CallbackInfoReturnable<Integer> cir) {
        float scale = HitStopController.peek();

        if (scale >= 0.999f) {
            return; // normal hız — orijinal metod aynen çalışsın
        }

        // ── Orijinal mantığın aynısı, sadece tickDelta ölçeklenmiş ──
        float rawDelta = (float) (l - this.lastMs) / this.msPerTick;
        this.tickDelta = rawDelta * scale;
        this.lastMs = l;
        this.partialTick += this.tickDelta;

        int i = (int) this.partialTick;
        this.partialTick -= (float) i;

        cir.setReturnValue(i);
    }
}