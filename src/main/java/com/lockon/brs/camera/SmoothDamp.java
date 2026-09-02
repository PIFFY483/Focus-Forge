package com.lockon.brs.camera;

import net.minecraft.util.Mth;

public class SmoothDamp {

    public static class State1D {
        public float value;
        public float velocity;
        public boolean initialized = false;

        public void snapTo(float v) {
            this.value = v;
            this.velocity = 0f;
            this.initialized = true;
        }
    }

    public static float smoothDamp(State1D state, float target, float smoothTime,
                                   float maxSpeed, float deltaTimeSeconds) {
        if (!state.initialized) {
            state.snapTo(target);
            return state.value;
        }
        if (deltaTimeSeconds <= 0f) {
            return state.value;
        }

        smoothTime = Math.max(0.0001f, smoothTime);
        float omega = 2.0f / smoothTime;

        float x = omega * deltaTimeSeconds;
        float exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x);

        float change = state.value - target;
        float originalTo = target;

        // Maksimum hız sınırlaması
        float maxChange = maxSpeed * smoothTime;
        change = Mth.clamp(change, -maxChange, maxChange);
        float clampedTarget = state.value - change;

        float temp = (state.velocity + omega * change) * deltaTimeSeconds;
        state.velocity = (state.velocity - omega * temp) * exp;
        float output = clampedTarget + (change + temp) * exp;

        // Overshoot önleme
        if ((originalTo - state.value > 0.0f) == (output > originalTo)) {
            output = originalTo;
            state.velocity = (output - originalTo) / deltaTimeSeconds;
        }

        state.value = output;
        return output;
    }

    public static float smoothDampAngle(State1D state, float targetDegrees, float smoothTime,
                                        float maxSpeed, float deltaTimeSeconds) {
        if (!state.initialized) {
            state.snapTo(targetDegrees);
            return state.value;
        }
        float delta = Mth.wrapDegrees(targetDegrees - state.value);
        float unwrappedTarget = state.value + delta;
        return smoothDamp(state, unwrappedTarget, smoothTime, maxSpeed, deltaTimeSeconds);
    }
}