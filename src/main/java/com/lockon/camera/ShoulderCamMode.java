package com.lockon.camera;

/**
 * Focus Forge'un kendi omuz kamerası (OLD) ile eski BRS projesinden
 * taşınan omuz/orbit kamerası (NEW) arasındaki aktif modu tutar.
 *
 * ALT tuşu (eski "key.brs.orbit_toggle") artık SADECE NEW (omuz) ile
 * ORBIT arasında geçiş yapıyor. OLD bu döngünün tamamen dışında tutuluyor;
 * OLD moduna sadece "/ff old cam" komutuyla geçilebilir ve ALT tuşu OLD
 * moddayken hiçbir şeyi değiştirmez. NEW ve ORBIT modlarına da sırasıyla
 * "/ff new cam" ve "/ff orbit cam" komutlarıyla doğrudan geçilebilir
 * (bkz. com.lockon.brs.client.FFCommand).
 */
public final class ShoulderCamMode {

    public enum Mode {
        OLD, // Focus Forge'un mevcut CameraStateManager/VirtualCameraHandler sistemi
        NEW  // Eski BRS projesinin CameraRig/OrbitCameraState sistemi
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
