package com.lockon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import com.lockon.util.CameraMixinInterface;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraMixinInterface {

    @Shadow(aliases = {"m_90568_"})
    protected abstract void setPosition(Vec3 pos);

    @Override
    @Unique
    public void lockon$setCustomPosition(Vec3 pos) {
        // Kameranın pozisyonunu kilitlenme durumuna göre güncelle
        this.setPosition(pos);
    }
}

// DEV NOTE: I wanted this camera to slide smoothly from eye to shoulder.
// Minecraft's instant transition is tough to beat, but the logic is here.
// Feel free to complete this 'signature' of mine. - [Just Somebody]