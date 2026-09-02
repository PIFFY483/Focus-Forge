package com.lockon.brs.energy;

public class ClientEnergyCache {

    private static final float USAGE_FLASH_THRESHOLD = 1.0f; // regen tick'lerini saymamak için
    private static final long FLASH_DURATION_MS = 350;
    private static final long USAGE_LABEL_DURATION_MS = 1200;

    private static float energy = EnergyManager.MAX_ENERGY;
    private static float maxEnergy = EnergyManager.MAX_ENERGY;

    private static float flashFromEnergy = EnergyManager.MAX_ENERGY; // flaşın başladığı (eski/daha yüksek) değer
    private static long flashEndTime = 0L;

    private static float lastUsedAmount = 0f;
    private static long usageLabelEndTime = 0L;

    /** Server'dan gelen en son "bize kilitlenmiş düşman var mı" bilgisi. */
    private static boolean hasLockedEnemy = false;

    private ClientEnergyCache() {
    }

    public static void update(float newEnergy, float newMaxEnergy, boolean newHasLockedEnemy) {
        float delta = newEnergy - energy;

        if (delta < -USAGE_FLASH_THRESHOLD) {
            float spent = -delta;

            flashFromEnergy = energy; // eski, daha yüksek değeri sakla
            flashEndTime = System.currentTimeMillis() + FLASH_DURATION_MS;

            lastUsedAmount = spent;
            usageLabelEndTime = System.currentTimeMillis() + USAGE_LABEL_DURATION_MS;
        }

        energy = newEnergy;
        maxEnergy = newMaxEnergy;
        hasLockedEnemy = newHasLockedEnemy;
    }

    public static float getEnergy() {
        return energy;
    }

    public static float getMaxEnergy() {
        return maxEnergy;
    }

    /** 0.0 - 1.0 arası güncel doluluk oranı. */
    public static float getPercentage() {
        return maxEnergy <= 0f ? 0f : Math.max(0f, Math.min(1f, energy / maxEnergy));
    }

    /** Flaş aktifse eski (daha yüksek) doluluk oranı; değilse normal orana eşit. */
    public static float getFlashPercentage() {
        if (System.currentTimeMillis() >= flashEndTime) return getPercentage();
        return maxEnergy <= 0f ? 0f : Math.max(0f, Math.min(1f, flashFromEnergy / maxEnergy));
    }

    public static boolean hasRecentUsage() {
        return System.currentTimeMillis() < usageLabelEndTime;
    }

    public static float getLastUsedAmount() {
        return lastUsedAmount;
    }

    /** true ise şu an bize kilitlenmiş en az bir düşman var (HUD'daki kırmızı "! ENEMY" etiketi için). */
    public static boolean hasLockedEnemy() {
        return hasLockedEnemy;
    }
}