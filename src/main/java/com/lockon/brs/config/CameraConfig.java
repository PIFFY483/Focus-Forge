package com.lockon.brs.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CameraConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Client CLIENT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_SHOULDER_CAM;
    public static final ForgeConfigSpec.DoubleValue SHOULDER_OFFSET;
    public static final ForgeConfigSpec.DoubleValue HEIGHT_OFFSET;
    public static final ForgeConfigSpec.DoubleValue CAMERA_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue CAMERA_SMOOTHNESS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ORBIT_CAMERA;
    public static final ForgeConfigSpec.DoubleValue ORBIT_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue ORBIT_HEIGHT_OFFSET;
    public static final ForgeConfigSpec.DoubleValue ORBIT_TRANSITION_SPEED;
    public static final ForgeConfigSpec.DoubleValue ORBIT_AUTO_ROTATE_SPEED;
    public static final ForgeConfigSpec.DoubleValue ORBIT_SENSITIVITY;
    public static final ForgeConfigSpec.BooleanValue SKIP_FRONT_VIEW;
    public static final ForgeConfigSpec.DoubleValue TRANSITION_SPEED;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SMOOTH_TRANSITION;

    //OTO HİZALAMA (DİNAMİK Y)
    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_Y_OFFSET;
    public static final ForgeConfigSpec.DoubleValue DYNAMIC_Y_FACTOR;

    public static final ForgeConfigSpec.DoubleValue CAMERA_FOLLOW_SPEED;

    public static final ForgeConfigSpec.DoubleValue COLLISION_RECOVERY_SPEED;


    static {
        Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        ENABLE_SHOULDER_CAM   = CLIENT.enableShoulderCam;
        SHOULDER_OFFSET       = CLIENT.shoulderOffset;
        HEIGHT_OFFSET         = CLIENT.heightOffset;
        CAMERA_DISTANCE       = CLIENT.cameraDistance;
        CAMERA_SMOOTHNESS     = CLIENT.cameraSmoothness;
        ENABLE_ORBIT_CAMERA   = CLIENT.enableOrbitCamera;
        ORBIT_DISTANCE        = CLIENT.orbitDistance;
        ORBIT_HEIGHT_OFFSET   = CLIENT.orbitHeightOffset;
        ORBIT_TRANSITION_SPEED= CLIENT.orbitTransitionSpeed;
        ORBIT_AUTO_ROTATE_SPEED= CLIENT.orbitAutoRotateSpeed;
        ORBIT_SENSITIVITY     = CLIENT.orbitSensitivity;
        SKIP_FRONT_VIEW       = CLIENT.skipFrontView;
        TRANSITION_SPEED      = CLIENT.transitionSpeed;
        ENABLE_SMOOTH_TRANSITION = CLIENT.enableSmoothTransition;

        ENABLE_DYNAMIC_Y_OFFSET = CLIENT.enableDynamicYOffset;
        DYNAMIC_Y_FACTOR        = CLIENT.dynamicYFactor;

        CAMERA_FOLLOW_SPEED     = CLIENT.cameraFollowSpeed;
        COLLISION_RECOVERY_SPEED = CLIENT.collisionRecoverySpeed;
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue enableShoulderCam;
        public final ForgeConfigSpec.DoubleValue shoulderOffset;
        public final ForgeConfigSpec.DoubleValue heightOffset;
        public final ForgeConfigSpec.DoubleValue cameraDistance;
        public final ForgeConfigSpec.DoubleValue cameraSmoothness;
        public final ForgeConfigSpec.DoubleValue orbitDistance;
        public final ForgeConfigSpec.DoubleValue orbitHeightOffset;
        public final ForgeConfigSpec.DoubleValue orbitTransitionSpeed;
        public final ForgeConfigSpec.DoubleValue orbitAutoRotateSpeed;
        public final ForgeConfigSpec.BooleanValue enableOrbitCamera;
        public final ForgeConfigSpec.DoubleValue orbitSensitivity;
        public final ForgeConfigSpec.BooleanValue skipFrontView;
        public final ForgeConfigSpec.DoubleValue transitionSpeed;
        public final ForgeConfigSpec.BooleanValue enableSmoothTransition;

        public final ForgeConfigSpec.BooleanValue enableDynamicYOffset;
        public final ForgeConfigSpec.DoubleValue dynamicYFactor;

        public final ForgeConfigSpec.DoubleValue cameraFollowSpeed;
        public final ForgeConfigSpec.DoubleValue collisionRecoverySpeed;


        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("camera");

            this.enableShoulderCam = builder
                    .comment("Omuz kamerasini etkinlestir")
                    .define("enableShoulderCam", true);

            this.shoulderOffset = builder
                    .comment("Yatay omuz offset'i (pozitif=sol, negatif=sag)")
                    .defineInRange("shoulderOffset", 0.5, -5.0, 5.0);

            this.heightOffset = builder
                    .comment("Dikey kamera offset'i")
                    .defineInRange("heightOffset", 0.3, -5.0, 5.0);

            this.cameraDistance = builder
                    .comment("Kameranin oyuncudan uzakligi (blok)")
                    .defineInRange("cameraDistance", 4.0, 0.5, 5.0);

            this.cameraSmoothness = builder
                    .comment("Kamera yumusakligi (0.05: cok yumusak, 1.0: sert)")
                    .defineInRange("cameraSmoothness", 0.1, 0.05, 1.0);

            this.enableOrbitCamera = builder
                    .comment("Orbit kamera sistemini etkinlestir (Sol Alt)")
                    .define("enableOrbitCamera", true);

            this.orbitDistance = builder
                    .comment("Orbit kameranin oyuncudan uzakligi (blok)")
                    .defineInRange("orbitDistance", 5.0, 2.0, 15.0);

            this.orbitHeightOffset = builder
                    .comment("Orbit kamera yukseklik offset'i")
                    .defineInRange("orbitHeightOffset", 1.0, -2.0, 5.0);

            this.orbitTransitionSpeed = builder
                    .comment("Omuz -> Orbit gecis hizi (tick basina ilerleme)")
                    .defineInRange("orbitTransitionSpeed", 0.06, 0.01, 0.3);

            this.orbitAutoRotateSpeed = builder
                    .comment("Orbit otomatik donus hizi (0 = kapali, derece/tick)")
                    .defineInRange("orbitAutoRotateSpeed", 0.0, 0.0, 5.0);

            this.orbitSensitivity = builder
                    .comment("Orbit kamera fare hassasiyeti")
                    .defineInRange("orbitSensitivity", 0.15, 0.01, 1.0);

            this.skipFrontView = builder
                    .comment("F5 ile kamera degistirirken TPV Front'u atla (FPV -> TPV Back -> FPV)")
                    .define("skipFrontView", true);

            this.transitionSpeed = builder
                    .comment("FPV <-> TPV gecis hizi (0.05: cok yavas, 0.5: hizli)")
                    .defineInRange("transitionSpeed", 0.15, 0.01, 1.0);

            this.enableSmoothTransition = builder
                    .comment("FPV <-> TPV arasi yumusak gecis (kapatilirsa aninda gecis)")
                    .define("enableSmoothTransition", true);

            //  OTO HİZALAMA SEÇENEKLERİ
            this.enableDynamicYOffset = builder
                    .comment("Mesafe arttikca Y yuksekligini otomatik dengele (Kadraj Hizalama)")
                    .define("enableDynamicYOffset", true);

            this.dynamicYFactor = builder
                    .comment("Geriye acilma miktarina gore ekstra Y artis orani")
                    .defineInRange("dynamicYFactor", 0.20, 0.0, 1.0);

            // LOCK-ON KAMERA TAKİP HIZI
            this.cameraFollowSpeed = builder
                    .comment("Lock-on aktifken kameranin, karakterin nisan hizindan bagimsiz " +
                            "kendi takip hizi. Dusuk = agir/sinematik kamera lag'i (souls-like). " +
                            "Yuksek (1.0) = kamera nisani aninda takip eder.")
                    .defineInRange("cameraFollowSpeed", 0.15, 0.01, 1.0);

            // DUVAR ÇARPIŞMASI SONRASI GERİ TOPARLANMA
            this.collisionRecoverySpeed = builder
                    .comment("Kamera bir engele carpip geri cekildikten sonra normale donerken " +
                            "kullanilan yumusatma hizi. Engele YAKLASMA her zaman anlik kalir " +
                            "(clip onleme icin), bu deger sadece UZAKLASIRKEN etkilidir. " +
                            "Dusuk = yavas/yumusak toparlanma, yuksek = hizli toparlanma.")
                    .defineInRange("collisionRecoverySpeed", 0.25, 0.01, 1.0);

            builder.pop();
        }
    }
}