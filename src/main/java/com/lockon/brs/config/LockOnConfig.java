package com.lockon.brs.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import com.lockon.shared.config.SharedListConfig;

import java.util.List;

public class LockOnConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Client CLIENT;

    // Lock Mechanism
    public static final ForgeConfigSpec.DoubleValue MAX_LOCK_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue MAX_DISENGAGEMENT_RANGE;
    public static final ForgeConfigSpec.DoubleValue MAX_LOCK_ANGLE;
    public static final ForgeConfigSpec.DoubleValue MAX_VERTICAL_OFFSET;
    public static final ForgeConfigSpec.DoubleValue CAMERA_FOCUS_OFFSET;
    // Dinamik odak yukseklik orani artik ayri bir BRS ayari degil; Focus Forge'un
    // kendi lock sisteminin (TYPE_1) "o" tusuyla acilan kamera ayarlari HUD'undaki
    // ortak degeri kullaniliyor, boylece iki lock type de ayni yuzde ile buyuk
    // moblara odaklaniyor. Bkz. com.lockon.config.CameraViewConfig.DYNAMIC_FOCUS_THRESHOLD
    public static final ForgeConfigSpec.DoubleValue LOCK_SPEED;
    public static final ForgeConfigSpec.DoubleValue MAX_SMOOTHING_FACTOR;
    public static final ForgeConfigSpec.DoubleValue UNLOCK_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue TARGET_SWITCH_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue BREAK_LOCK_ON_LOS_BREAK;

    // Targeting
    public static final ForgeConfigSpec.BooleanValue TARGET_PLAYERS;
    // NOT: Entity blacklist ve blok listeleri artık OLD ve NEW kamera modları
    // arasında ORTAK. Gerçek tanım com.lockon.shared.config.SharedListConfig
    // içinde; burada sadece eski kod tabanının bozulmaması için alias tutuluyor.
    public static final ForgeConfigSpec.BooleanValue ENABLE_TARGET_BLACKLIST = SharedListConfig.ENABLE_TARGET_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TARGET_BLACKLIST = SharedListConfig.TARGET_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCK_ACQUISITION_BLOCK_LIST = SharedListConfig.LOCK_ACQUISITION_BLOCK_LIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCK_PRECLUSION_BLOCK_LIST = SharedListConfig.LOCK_PRECLUSION_BLOCK_LIST;
    public static final ForgeConfigSpec.IntValue TARGET_SCAN_FREQUENCY;

    // Visual
    public static final ForgeConfigSpec.BooleanValue ENABLE_VIGNETTE;
    public static final ForgeConfigSpec.IntValue CROSSHAIR_STYLE;
    public static final ForgeConfigSpec.DoubleValue CROSSHAIR_SIZE;
    public static final ForgeConfigSpec.IntValue CROSSHAIR_COLOR_INDEX;
    public static final ForgeConfigSpec.DoubleValue ICON_Y_OFFSET;

    // ── DİNAMİK LOCK-ON KAMERA MESAFESİ (FOV Framing) ──
    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_LOCK_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue FRAME_MARGIN;
    public static final ForgeConfigSpec.DoubleValue MIN_LOCK_CAMERA_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue MAX_LOCK_CAMERA_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue LOCK_DISTANCE_SMOOTH_SPEED;
    public static final ForgeConfigSpec.DoubleValue LOCK_DISTANCE_MAX_STEP_PER_TICK;

    // ── LOCK MESAFESİ ARTIŞINA/AZALIŞINA BAĞLI Y OFFSET ──
    // Genel "Oto Y Hizalama" (CameraConfig.ENABLE_DYNAMIC_Y_OFFSET) sisteminden BAĞIMSIZ:
    // sadece lock-on'un kamerayı normal mesafenin ötesine ittiği KADAR (delta) Y'yi ayarlar.
    public static final ForgeConfigSpec.BooleanValue ENABLE_LOCK_DISTANCE_Y_OFFSET;
    public static final ForgeConfigSpec.DoubleValue LOCK_DISTANCE_Y_FACTOR;


    static {
        Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        MAX_LOCK_DISTANCE = CLIENT.maxLockDistance;
        MAX_DISENGAGEMENT_RANGE = CLIENT.maxDisengagementRange;
        MAX_LOCK_ANGLE = CLIENT.maxLockAngle;
        MAX_VERTICAL_OFFSET = CLIENT.maxVerticalOffset;
        CAMERA_FOCUS_OFFSET = CLIENT.cameraFocusOffset;
        LOCK_SPEED = CLIENT.lockSpeed;
        MAX_SMOOTHING_FACTOR = CLIENT.maxSmoothingFactor;
        UNLOCK_COOLDOWN_SECONDS = CLIENT.unlockCooldownSeconds;
        TARGET_SWITCH_COOLDOWN_SECONDS = CLIENT.targetSwitchCooldownSeconds;
        BREAK_LOCK_ON_LOS_BREAK = CLIENT.breakLockOnLosBreak;
        TARGET_PLAYERS = CLIENT.targetPlayers;
        // ENABLE_TARGET_BLACKLIST / TARGET_BLACKLIST / LOCK_ACQUISITION_BLOCK_LIST / LOCK_PRECLUSION_BLOCK_LIST
        // artık SharedListConfig'ten geliyor (alan tanımında alias edildi).
        TARGET_SCAN_FREQUENCY = CLIENT.targetScanFrequency;
        ENABLE_VIGNETTE = CLIENT.enableVignette;
        CROSSHAIR_STYLE = CLIENT.crosshairStyle;
        CROSSHAIR_SIZE = CLIENT.crosshairSize;
        CROSSHAIR_COLOR_INDEX = CLIENT.crosshairColorIndex;
        ICON_Y_OFFSET = CLIENT.iconYOffset;

        ENABLE_DYNAMIC_LOCK_DISTANCE = CLIENT.enableDynamicLockDistance;
        FRAME_MARGIN = CLIENT.frameMargin;
        MIN_LOCK_CAMERA_DISTANCE = CLIENT.minLockCameraDistance;
        MAX_LOCK_CAMERA_DISTANCE = CLIENT.maxLockCameraDistance;
        LOCK_DISTANCE_SMOOTH_SPEED = CLIENT.lockDistanceSmoothSpeed;
        LOCK_DISTANCE_MAX_STEP_PER_TICK = CLIENT.lockDistanceMaxStepPerTick;

        ENABLE_LOCK_DISTANCE_Y_OFFSET = CLIENT.enableLockDistanceYOffset;
        LOCK_DISTANCE_Y_FACTOR = CLIENT.lockDistanceYFactor;
    }

    public static class Client {
        public final ForgeConfigSpec.DoubleValue maxLockDistance;
        public final ForgeConfigSpec.DoubleValue maxDisengagementRange;
        public final ForgeConfigSpec.DoubleValue maxLockAngle;
        public final ForgeConfigSpec.DoubleValue maxVerticalOffset;
        public final ForgeConfigSpec.DoubleValue cameraFocusOffset;
        public final ForgeConfigSpec.DoubleValue lockSpeed;
        public final ForgeConfigSpec.DoubleValue maxSmoothingFactor;
        public final ForgeConfigSpec.DoubleValue unlockCooldownSeconds;
        public final ForgeConfigSpec.DoubleValue targetSwitchCooldownSeconds;
        public final ForgeConfigSpec.BooleanValue breakLockOnLosBreak;
        public final ForgeConfigSpec.BooleanValue targetPlayers;
        // enableTargetBlacklist / targetBlacklist / lockAcquisitionBlockList / lockPreclusionBlockList
        // artık SharedListConfig'te (ortak).
        public final ForgeConfigSpec.IntValue targetScanFrequency;
        public final ForgeConfigSpec.BooleanValue enableVignette;
        public final ForgeConfigSpec.IntValue crosshairStyle;
        public final ForgeConfigSpec.DoubleValue crosshairSize;
        public final ForgeConfigSpec.IntValue crosshairColorIndex;
        public final ForgeConfigSpec.DoubleValue iconYOffset;

        public final ForgeConfigSpec.BooleanValue enableDynamicLockDistance;
        public final ForgeConfigSpec.DoubleValue frameMargin;
        public final ForgeConfigSpec.DoubleValue minLockCameraDistance;
        public final ForgeConfigSpec.DoubleValue maxLockCameraDistance;
        public final ForgeConfigSpec.DoubleValue lockDistanceSmoothSpeed;
        public final ForgeConfigSpec.DoubleValue lockDistanceMaxStepPerTick;

        public final ForgeConfigSpec.BooleanValue enableLockDistanceYOffset;
        public final ForgeConfigSpec.DoubleValue lockDistanceYFactor;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("lock_mechanism");

            this.maxLockDistance = builder
                    .comment("Maksimum kilit mesafesi (blok)")
                    .defineInRange("maxLockDistance", 20.0, 5.0, 100.0);

            this.maxDisengagementRange = builder
                    .comment("Kilit kopma mesafesi")
                    .defineInRange("maxDisengagementRange", 25.0, 5.0, 150.0);

            this.maxLockAngle = builder
                    .comment("Maksimum yatay kilit açısı (derece)")
                    .defineInRange("maxLockAngle", 45.0, 10.0, 180.0);

            this.maxVerticalOffset = builder
                    .comment("Dikey açı toleransı (derece)")
                    .defineInRange("maxVerticalOffset", 30.0, 5.0, 90.0);

            this.cameraFocusOffset = builder
                    .comment("Kamera odak noktası dikey offset")
                    .defineInRange("cameraFocusOffset", 0.0, -5.0, 5.0);

            this.lockSpeed = builder
                    .comment("Kilit hızı")
                    .defineInRange("lockSpeed", 0.1, 0.01, 1.0);

            this.maxSmoothingFactor = builder
                    .comment("Maksimum yumuşatma faktörü")
                    .defineInRange("maxSmoothingFactor", 0.1, 0.01, 1.0);

            this.unlockCooldownSeconds = builder
                    .comment("Kilit açma cooldown'u (saniye)")
                    .defineInRange("unlockCooldownSeconds", 0.5, 0.0, 5.0);

            this.targetSwitchCooldownSeconds = builder
                    .comment("Hedef değiştirme cooldown'u (saniye)")
                    .defineInRange("targetSwitchCooldownSeconds", 2.0, 0.0, 10.0);

            this.breakLockOnLosBreak = builder
                    .comment("Görüş hattı kesilince kilidi kopar")
                    .define("breakLockOnLosBreak", true);

            builder.pop();

            builder.push("targeting");

            this.targetPlayers = builder
                    .comment("Oyuncuları hedef olarak kabul et")
                    .define("targetPlayers", true);

            // enableTargetBlacklist / targetBlacklist / lockAcquisitionBlockList / lockPreclusionBlockList
            // artık OLD ve NEW kamera modları arasında ORTAK — bkz. com.lockon.shared.config.SharedListConfig.

            this.targetScanFrequency = builder
                    .comment("Hedef tarama frekansı (tick)")
                    .defineInRange("targetScanFrequency", 5, 1, 60);

            builder.pop();

            builder.push("visual");

            this.enableVignette = builder
                    .comment("Kilit sırasında vinyet efekti")
                    .define("enableVignette", true);

            this.crosshairStyle = builder
                    .comment("0: Crosshair, 1: Hexagon, 2: Star, 3: Vanguard, 4: Trinity, " +
                            "5: Hunter, 6: Chronos, 7: Black Star, 8: Cannon Sight, " +
                            "9: Blade Mark, 10: Death Skull, 11: BRS Sigil")
                    .defineInRange("crosshairStyle", 0, 0, 11);

            this.crosshairSize = builder
                    .comment("İkon boyutu")
                    .defineInRange("crosshairSize", 1.0, 0.1, 5.0);

            this.crosshairColorIndex = builder
                    .comment("0: Turquoise, 1: Green, 2: Blue, 3: Red, 4: White")
                    .defineInRange("crosshairColorIndex", 0, 0, 4);

            this.iconYOffset = builder
                    .comment("Ikonun odak noktasından dikey kaydırması")
                    .defineInRange("iconYOffset", 0.0, -2.0, 2.0);

            builder.pop();

            // ── DİNAMİK LOCK-ON KAMERA MESAFESİ ──
            builder.push("dynamic_lock_distance");

            this.enableDynamicLockDistance = builder
                    .comment("Lock-on sırasında hedefin boyuna göre kamera mesafesini otomatik ayarla " +
                            "(büyük moblarda geri çekilir, küçük moblarda normal mesafeye yaklaşır)")
                    .define("enableDynamicLockDistance", true);

            this.frameMargin = builder
                    .comment("Hedefin ekran (dikey FOV konisi) içinde kaplayacağı maksimum oran. " +
                            "1.0 = tam ekran kenarına dayanır, 0.6-0.75 arası önerilir (biraz boşluk bırakır)")
                    .defineInRange("frameMargin", 0.65, 0.1, 1.0);

            this.minLockCameraDistance = builder
                    .comment("Dinamik hesaplamada izin verilen minimum kamera mesafesi (blok). " +
                            "Bu değer normal CAMERA_DISTANCE'ın altına inmemeli.")
                    .defineInRange("minLockCameraDistance", 2.0, 0.5, 10.0);

            this.maxLockCameraDistance = builder
                    .comment("Dinamik hesaplamada izin verilen maksimum kamera mesafesi (blok). " +
                            "Çok büyük bosslarda kameranın ne kadar geri gidebileceğinin sınırı.")
                    .defineInRange("maxLockCameraDistance", 12.0, 2.0, 30.0);

            this.lockDistanceSmoothSpeed = builder
                    .comment("Kamera mesafesinin hedef değere yumuşak geçiş hızı " +
                            "(0.02: çok yavaş/sinematik, 0.3: hızlı tepki)")
                    .defineInRange("lockDistanceSmoothSpeed", 0.08, 0.01, 0.5);

            this.lockDistanceMaxStepPerTick = builder
                    .comment("Kamera mesafesinin bir tick'te değişebileceği maksimum miktar (blok). " +
                            "Hedef ne kadar aniden değişirse değişsin (yeni büyük mob kilitlendi, hedef " +
                            "değişti vb.) kamera bu hızdan daha fazla sıçrayamaz — odağın savaş sırasında " +
                            "ani bir 'tak' hissiyle bozulmasını engeller. Düşük değer: çok yumuşak/yavaş, " +
                            "yüksek değer: daha hızlı ama daha az yumuşak tepki.")
                    .defineInRange("lockDistanceMaxStepPerTick", 0.12, 0.01, 2.0);

            // ── LOCK MESAFESİ ARTIŞINA/AZALIŞINA BAĞLI Y OFFSET ──
            this.enableLockDistanceYOffset = builder
                    .comment("Lock-on kamerayı geri ittikçe (veya normal mesafeye yaklaştırdıkça) " +
                            "Y yüksekliğini de aynı oranda otomatik ayarla. " +
                            "enableDynamicLockDistance kapalıysa bu da etkisiz olur (fark zaten 0'dır).")
                    .define("enableLockDistanceYOffset", true);

            this.lockDistanceYFactor = builder
                    .comment("Lock-on mesafesindeki her 1 bloklik artış/azalış başına Y'ye eklenecek/çıkarılacak miktar")
                    .defineInRange("lockDistanceYFactor", 0.20, 0.0, 1.0);

            builder.pop();
        }
    }
}