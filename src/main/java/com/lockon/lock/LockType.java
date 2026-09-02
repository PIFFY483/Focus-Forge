package com.lockon.lock;

/**
 * Focus Forge'un kendi kilitlenme (lock-on) sistemi ile eski BRS projesinden
 * taşınan kilitlenme sistemi aynı anda çalışamaz (ikisi de aynı hedefe farklı
 * mantıkla kilitleniyor). LOCK tuşu ikisinde de aynı fiziksel tuş, ama hangi
 * sistemin gerçekten tepki vereceğini bu seçim belirler.
 */
public final class LockType {

    public enum Type {
        TYPE_1, // Focus Forge'un kendi lock-on sistemi
        TYPE_2  // Eski BRS projesinin lock-on sistemi
    }

    private static Type current = Type.TYPE_1;

    private LockType() {
    }

    public static Type current() {
        return current;
    }

    public static boolean isType1() {
        return current == Type.TYPE_1;
    }

    public static boolean isType2() {
        return current == Type.TYPE_2;
    }

    public static void set(Type type) {
        current = type;
    }

    public static void toggle() {
        current = (current == Type.TYPE_1) ? Type.TYPE_2 : Type.TYPE_1;
    }
}
