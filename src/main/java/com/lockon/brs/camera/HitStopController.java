package com.lockon.brs.camera;

import net.minecraft.util.Mth;

public class HitStopController {

    // ── Durum ──
    private static float totalDuration = 0.0f;
    private static float elapsed = 0.0f;
    private static boolean active = false;

    // ── Tepe noktasında ulaşılan minimum hız ──
    // 0.0 = tamamen donmuş, 0.05 = neredeyse donmuş
    private static final float MIN_TIME_SCALE = 0.02f;

    // ── 1 salise = kaç milisaniye? ──
    // 1 Minecraft tick = 50ms
    // 1 salise = 1 tick olarak tanımlı
    private static final float SALISE_TO_MS = 50.0f;

    public static void request(float durationSalise) {
        if (durationSalise <= 0.0f) return;

        float durationMs = durationSalise * SALISE_TO_MS;

        // Zaten aktifse ve kalan süre yeni istekten uzunsa yoksay
        if (active) {
            float remainingMs = totalDuration - elapsed;
            if (remainingMs >= durationMs) return;
        }

        totalDuration = durationMs;
        elapsed = 0.0f;
        active = true;
    }

    /**
     * Her render frame'de çağır.
     * Geçen süreyi işler ve mevcut timeScale'i döndürür.
     *
     * @param deltaMs Bu frame'de geçen süre (ms)
     * @return timeScale: 1.0 = normal, 0.0 = donmuş
     */
    public static float tick(float deltaMs) {
        if (!active) return 1.0f;

        elapsed += deltaMs;

        if (elapsed >= totalDuration) {
            cancel();
            return 1.0f;
        }

        return computeScale();
    }

    /**
     * Zamanı ilerletmeden mevcut scale'i oku.
     * Birden fazla sistemin aynı frame'de okuması gerekiyorsa bunu kullan.
     */
    public static float peek() {
        if (!active || totalDuration <= 0.0f) return 1.0f;
        return computeScale();
    }

    /**
     * Aktif mi?
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * İlerleme oranı (0..1). Debug overlay için.
     */
    public static float getProgress() {
        if (!active || totalDuration <= 0.0f) return 0.0f;
        return Mth.clamp(elapsed / totalDuration, 0.0f, 1.0f);
    }

    /**
     * Zorla iptal et. (Arena reset, death, vb.)
     */
    public static void cancel() {
        active = false;
        elapsed = 0.0f;
        totalDuration = 0.0f;
    }

    // ── İç hesaplama ──

    private static float computeScale() {
        // Normalize ilerleme: 0 → 1
        float t = Mth.clamp(elapsed / totalDuration, 0.0f, 1.0f);

        if (t < 0.5f) {
            // İlk yarı: 1.0 → MIN_TIME_SCALE (ease-in)
            float phase = t / 0.5f;           // 0 → 1
            float eased = smoothstep(phase);   // S-eğrisi
            return Mth.lerp(eased, 1.0f, MIN_TIME_SCALE);
        } else {
            // İkinci yarı: MIN_TIME_SCALE → 1.0 (ease-out)
            float phase = (t - 0.5f) / 0.5f;  // 0 → 1
            float eased = smoothstep(phase);   // S-eğrisi
            return Mth.lerp(eased, MIN_TIME_SCALE, 1.0f);
        }
    }

    /**
     * Klasik smoothstep: sert değil, S-eğrisi geçiş.
     * t=0 → 0, t=1 → 1, ortada yumuşak.
     */
    private static float smoothstep(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}