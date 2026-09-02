package com.lockon.util;

import net.minecraft.world.phys.Vec3;

public interface CameraMixinInterface {
    // Old shoulder cam (Focus Forge) pozisyon override'ı
    void lockon$setCustomPosition(Vec3 pos);

    // New shoulder cam (eski BRS) pozisyon/detached override'ı
    void brs$setCustomPosition(Vec3 pos);
    void brs$setDetached(boolean detached);
}