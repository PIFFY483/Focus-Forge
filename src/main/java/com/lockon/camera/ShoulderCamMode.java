package com.lockon.camera;

public final class ShoulderCamMode {

    public enum Mode {
        OLD,
        NEW
    }

    private static Mode current = Mode.OLD;

    private ShoulderCamMode() {
    }

    public static Mode current() {
        return current;
    }

    public static boolean isOld() {
        return current == Mode.OLD;
    }

    public static boolean isNew() {
        return current == Mode.NEW;
    }

    public static void set(Mode mode) {
        current = mode;
    }

    public static void toggle() {
        current = (current == Mode.OLD) ? Mode.NEW : Mode.OLD;
    }
}
