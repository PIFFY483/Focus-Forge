package com.lockon.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import com.lockon.shared.config.SharedListConfig;
import java.util.List;

@Mod.EventBusSubscriber
public class LockOnConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;


    // Statik alanlar (Ayarların dışarıdan erişileceği yer)
    public static final ForgeConfigSpec.BooleanValue ENABLE_MOUSE_INPUT_WARNING;

    // Lock Mekanizması
    public static final ForgeConfigSpec.DoubleValue LOCK_SPEED;
    public static final ForgeConfigSpec.DoubleValue MAX_SMOOTHING_FACTOR;
    public static final ForgeConfigSpec.DoubleValue MAX_LOCK_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue MAX_DISENGAGEMENT_RANGE;
    public static final ForgeConfigSpec.DoubleValue MAX_VERTICAL_OFFSET;
    public static final ForgeConfigSpec.DoubleValue CAMERA_FOCUS_OFFSET;
    public static final ForgeConfigSpec.DoubleValue UNLOCK_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue TARGET_SWITCH_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue USE_PLAYER_ATTACK_RANGE;
    // YENİ ALAN: LOS Optimizasyon Mesafesi
    public static final ForgeConfigSpec.DoubleValue MAX_LOS_CHECK_DISTANCE;

    // Targeting
    public static final ForgeConfigSpec.IntValue TARGET_SCAN_FREQUENCY;
    // NOT: Entity blacklist artık OLD ve NEW kamera modları arasında ORTAK.
    // Gerçek tanım com.lockon.shared.config.SharedListConfig içinde; burada
    // sadece eski kod tabanının bozulmaması için alias tutuluyor.
    public static final ForgeConfigSpec.BooleanValue ENABLE_TARGET_BLACKLIST = SharedListConfig.ENABLE_TARGET_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TARGET_BLACKLIST = SharedListConfig.TARGET_BLACKLIST;

    // Target Scanning & Lock State
    public static final ForgeConfigSpec.BooleanValue TARGET_PLAYERS;
    public static final ForgeConfigSpec.DoubleValue MAX_LOCK_ANGLE;
    public static final ForgeConfigSpec.BooleanValue BREAK_LOCK_ON_LOS_BREAK;

    // --- CROSSHAIR/BOX AYARLARI ---

    public static final ForgeConfigSpec.DoubleValue CROSSHAIR_XZ_SIZE;
    public static final ForgeConfigSpec.DoubleValue CROSSHAIR_Y_SIZE;

    // --------------------------------------------

    // Block Lists
    // NOT: Artık OLD ve NEW kamera modları arasında ORTAK — gerçek tanım
    // com.lockon.shared.config.SharedListConfig içinde.
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> lockPreclusionBlockList = SharedListConfig.LOCK_PRECLUSION_BLOCK_LIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> lockAcquisitionBlockList = SharedListConfig.LOCK_ACQUISITION_BLOCK_LIST;

    static {
        Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        // Config'ten statik alanlara atama

        ENABLE_MOUSE_INPUT_WARNING = CLIENT.enableMouseInputWarning;
        LOCK_SPEED = CLIENT.lockSpeed;
        MAX_SMOOTHING_FACTOR = CLIENT.maxSmoothingFactor;
        MAX_LOCK_DISTANCE = CLIENT.maxLockDistance;
        MAX_DISENGAGEMENT_RANGE = CLIENT.maxDisengagementRange;
        MAX_VERTICAL_OFFSET = CLIENT.maxVerticalOffset;
        CAMERA_FOCUS_OFFSET = CLIENT.cameraFocusOffset;
        UNLOCK_COOLDOWN_SECONDS = CLIENT.unlockCooldownSeconds;
        TARGET_SWITCH_COOLDOWN_SECONDS = CLIENT.targetSwitchCooldownSeconds;
        USE_PLAYER_ATTACK_RANGE = CLIENT.usePlayerAttackRange;
        // YENİ ALAN ATAMASI
        MAX_LOS_CHECK_DISTANCE = CLIENT.maxLosCheckDistance;

        // Targeting Ayarlarının Ataması
        TARGET_SCAN_FREQUENCY = CLIENT.targetScanFrequency;
        // ENABLE_TARGET_BLACKLIST / TARGET_BLACKLIST artık SharedListConfig'ten geliyor (alan tanımında alias edildi).

        // TargetScanner için kritik olanların ataması
        TARGET_PLAYERS = CLIENT.targetPlayers;
        MAX_LOCK_ANGLE = CLIENT.maxLockAngle;
        BREAK_LOCK_ON_LOS_BREAK = CLIENT.breakLockOnLosBreak;

        // Crosshair Ayarlarının Ataması

        CROSSHAIR_XZ_SIZE = CLIENT.crosshairXZSize;
        CROSSHAIR_Y_SIZE = CLIENT.crosshairYSize;


        // Block List Atamaları artık SharedListConfig'ten geliyor (alan tanımında alias edildi).
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue enableMouseInputWarning;

        // Lock Mekanizması
        public final ForgeConfigSpec.DoubleValue lockSpeed;
        public final ForgeConfigSpec.DoubleValue maxSmoothingFactor;
        public final ForgeConfigSpec.DoubleValue maxLockDistance;
        public final ForgeConfigSpec.DoubleValue maxDisengagementRange;
        public final ForgeConfigSpec.DoubleValue maxVerticalOffset;
        public final ForgeConfigSpec.DoubleValue cameraFocusOffset;
        public final ForgeConfigSpec.DoubleValue unlockCooldownSeconds;
        public final ForgeConfigSpec.DoubleValue targetSwitchCooldownSeconds;
        public final ForgeConfigSpec.BooleanValue usePlayerAttackRange;
        // YENİ ALAN
        public final ForgeConfigSpec.DoubleValue maxLosCheckDistance;

        // Targeting Config Alanları
        public final ForgeConfigSpec.IntValue targetScanFrequency;
        // enableTargetBlacklist / targetBlacklist artık SharedListConfig'te (ortak).

        // Lock State/Scanning Alanları
        public final ForgeConfigSpec.BooleanValue targetPlayers;
        public final ForgeConfigSpec.DoubleValue maxLockAngle;
        public final ForgeConfigSpec.BooleanValue breakLockOnLosBreak;

        // icon Config Alanları

        public final ForgeConfigSpec.DoubleValue crosshairXZSize;
        public final ForgeConfigSpec.DoubleValue crosshairYSize;

        // lockPreclusionBlockList / lockAcquisitionBlockList artık SharedListConfig'te (ortak).

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("Visuals");

            this.enableMouseInputWarning = builder.comment("Kilitlenme sırasında fare girişinin geçersiz kılındığı konusunda bir uyarı gösterir.")
                    .define("enableMouseInputWarning", true);


            this.crosshairXZSize = builder.comment("Hedef Kutunun Yatay Boyutu (X ve Z ekseni).")
                    .defineInRange("crosshairXZSize", 0.5, 0.1, 5.0);
            this.crosshairYSize = builder.comment("Hedef Kutunun Dikey Boyutu (Y ekseni).")
                    .defineInRange("crosshairYSize", 1.0, 0.1, 5.0);

            builder.pop();

            builder.push("LockMechanism");
            this.lockSpeed = builder.comment("Kamera dönüşünün kilitlenmiş hedefe uyum sağlama hızı (0.01 - 1.0).")
                    .defineInRange("lockSpeed", 0.15, 0.01, 1.0);
            this.maxSmoothingFactor = builder.comment("Kamera dönüşündeki maksimum yumuşatma çarpanı (titremeyi azaltır).")
                    .defineInRange("maxSmoothingFactor", 0.6, 0.01, 1.0);
            this.maxLockDistance = builder.comment("Bir hedefin kilitlenmesi için maksimum mesafe (blok).")
                    .defineInRange("maxLockDistance", 20.0, 10.0, 128.0);
            this.maxDisengagementRange = builder.comment("Hedef bu mesafeyi aştığında kilidin otomatik olarak açılacağı maksimum mesafe. (MAX_LOCK_DISTANCE'tan büyük olmalıdır)")
                    .defineInRange("maxDisengagementRange", 30.0, 10.0, 150.0);
            this.maxVerticalOffset = builder.comment("Kilitlenme hedefini dikey olarak ne kadar ayarlayabileceğinizi belirler (blok). 0.0, hedef varlığın göz hizasına odaklanır.")
                    .defineInRange("maxVerticalOffset", 0.5, 0.0, 5.0);
            this.cameraFocusOffset = builder.comment("Kameranın odaklanacağı hedef varlığın dikey pozisyonundan (eye height) ek ofset. Negatif değerler aşağı, pozitif değerler yukarı kaydırır.")
                    .defineInRange("cameraFocusOffset", 0.0, -1.0, 2.0);
            this.unlockCooldownSeconds = builder.comment("Kilit açıldıktan sonra ne kadar süreyle yeni bir kilitlenmeye izin verilmeyeceğini belirler (saniye).")
                    .defineInRange("unlockCooldownSeconds", 0.25, 0.0, 5.0);
            this.targetSwitchCooldownSeconds = builder.comment("Hedef değiştirme tuşuna basıldıktan sonra ne kadar süreyle yeni bir hedef değiştirmeye izin verilmeyeceğini belirler (saniye).")
                    .defineInRange("targetSwitchCooldownSeconds", 0.5, 0.0, 5.0);
            this.usePlayerAttackRange = builder.comment("Hedefin kilitlenme menzili için oyuncunun saldırı menzilini kullanıp kullanmayacağını belirler. (Yanlışsa, MAX_LOCK_DISTANCE kullanılır.)")
                    .define("usePlayerAttackRange", false);

            // YENİ OPTİMİZASYON AYARI
            this.maxLosCheckDistance = builder.comment("Hedefe Görüş Hattı (LOS) kontrolü sırasında Ray Trace'in gideceği maksimum mesafe (blok). Performans optimizasyonu için kullanılır. Kilitlenme menzilinden (MAX_LOCK_DISTANCE) düşük olabilir.")
                    .defineInRange("maxLosCheckDistance", 20.0, 1.0, 128.0);

            builder.pop();

            builder.push("Targeting");

            // Lock State/Scanning ALANLARI EKLENDİ
            this.targetPlayers = builder.comment("Oyunculara kilitlenmeye izin verilip verilmeyeceğini belirler.")
                    .define("targetPlayers", false);
            this.maxLockAngle = builder.comment("Oyuncunun görüş açısından bir hedefin kilitlenmesi için maksimum açı (derece).")
                    .defineInRange("maxLockAngle", 45.0, 5.0, 180.0);
            this.breakLockOnLosBreak = builder.comment("Görüş Hattı (Line of Sight - LOS) engellendiğinde kilidin kırılıp kırılmayacağını belirler.")
                    .define("breakLockOnLosBreak", true);

            this.targetScanFrequency = builder.comment("Oyuncunun etrafındaki yeni bir hedefi ve mevcut hedefin geçerliliğini ne sıklıkla (tick cinsinden) tarayacağını belirler. (20 tick = 1 saniye)")
                    .defineInRange("targetScanFrequency", 10, 1, 60);

            // enableTargetBlacklist / targetBlacklist ve BlockLists (acquisition/preclusion)
            // artık OLD ve NEW kamera modları arasında ORTAK — bkz. com.lockon.shared.config.SharedListConfig.

            builder.pop();
        }
    }
}