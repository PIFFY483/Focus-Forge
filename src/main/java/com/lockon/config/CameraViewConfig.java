package com.lockon.config;

import com.google.common.primitives.Booleans;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;


public class CameraViewConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Client CLIENT;

    // Static variables for easy access
    public static final ForgeConfigSpec.DoubleValue SHOULDER_OFFSET_X;
    public static final ForgeConfigSpec.DoubleValue SHOULDER_OFFSET_Y;
    public static final ForgeConfigSpec.DoubleValue CAMERA_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue CAMERA_SMOOTHNESS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SHOULDER_CAM;
    public static final ForgeConfigSpec.IntValue CROSSHAIR_MODE;
    public static final ForgeConfigSpec.IntValue CROSSHAIR_STYLE;
    public static final ForgeConfigSpec.IntValue CROSSHAIR_COLOR_INDEX;

    static {
        Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        // Pulling values from Client class
        SHOULDER_OFFSET_X = CLIENT.shoulderOffsetX;
        SHOULDER_OFFSET_Y = CLIENT.shoulderOffsetY;
        CAMERA_DISTANCE = CLIENT.cameraDistance;
        CAMERA_SMOOTHNESS = CLIENT.cameraSmoothness;
        ENABLE_SHOULDER_CAM = CLIENT.enableShoulderCam;
        CROSSHAIR_MODE = CLIENT.crosshairMode;
        CROSSHAIR_STYLE = CLIENT.crosshairStyle;
        CROSSHAIR_COLOR_INDEX = CLIENT.crosshairColorIndex;
    }

    public static class Client {
        public final ForgeConfigSpec.DoubleValue shoulderOffsetX;
        public final ForgeConfigSpec.DoubleValue shoulderOffsetY;
        public final ForgeConfigSpec.DoubleValue cameraDistance;
        public final ForgeConfigSpec.DoubleValue cameraSmoothness;
        public final ForgeConfigSpec.BooleanValue enableShoulderCam;
        public final ForgeConfigSpec.IntValue crosshairMode;
        public final ForgeConfigSpec.DoubleValue lockOnSmoothness;
        public final ForgeConfigSpec.DoubleValue zoomInSpeed;
        public final ForgeConfigSpec.BooleanValue enableZoom;
        public final ForgeConfigSpec.BooleanValue enableVignette;
        public final ForgeConfigSpec.IntValue crosshairStyle;
        public final ForgeConfigSpec.IntValue crosshairColorIndex;



        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("CameraViewSettings");

            this.shoulderOffsetX = builder
                    .comment("Horizontal offset of the camera")
                    .defineInRange("shoulderOffsetX", 0.7, -2.0, 2.0);

            this.shoulderOffsetY = builder
                    .comment("Vertical offset of the camera")
                    .defineInRange("shoulderOffsetY", 0.0, -2.0, 2.0);

            this.cameraDistance = builder
                    .comment("Distance of the camera from the player")
                    .defineInRange("cameraDistance", 2.5, 1.0, 10.0);

            this.cameraSmoothness = builder
                    .comment("Camera movement smoothness (0.1: Very Slow, 1.0: Rigid)")
                    .defineInRange("cameraSmoothness", 0.4, 0.05, 1.0);

            this.enableShoulderCam = builder
                    .comment("Enable or disable the shoulder camera system")
                    .define("enableShoulderCam", true);

            this.crosshairMode = builder.comment("0: Kapalı, 1: Sadece Omuz, 2: Her zaman").defineInRange("crosshairMode", 1, 0, 2);

            this.lockOnSmoothness = builder
                    .comment("Target tracking smoothness during lock-on (0.001: Very Fluid, 0.60: Very Rigid)")
                    .defineInRange("lockOnSmoothness", 0.10, 0.001, 0.60);

            this.zoomInSpeed = builder
                    .comment("Speed of the zoom effect (0.01: Very Slow, 1.0: Instant)")
                    .defineInRange("zoomInSpeed", 0.1, 0.01, 1.0);


            this.enableZoom = builder
                    .comment("Enables the FOV zoom effect during lock-on.")
                    .define("enableZoomEffect", true);

            this.enableVignette = builder
                    .comment("Enables the vignette shadow effect on screen edges during lock-on.")
                    .define("enableVignette", true);

            this.crosshairStyle = builder
                    .comment("0: Ok Uçları (< >), 1: Klasik Artı (+), 2: Nokta (.), 3: Daire (o), 4: Tactical (T), 5: Double Ring, 6: Corners")
                    .defineInRange("crosshairStyle", 0, 0, 6);

            // Client sınıfı içinde constructor kısmını bul ve açıklamayı güncelle:
            this.crosshairColorIndex = builder
                    .comment("0: Turquoise, 1: Green, 2: Dark Blue, 3: Red, 4: White,") // Renkleri güncelledik
                    .defineInRange("crosshairColorIndex", 0, 0, 4);
            builder.pop();
        }
    }
}