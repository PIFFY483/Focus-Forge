package com.lockon.mixin;

import com.lockon.client.CrosshairTargetHelper;
import com.lockon.config.CameraViewConfig;
import com.lockon.lock.LockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "use", at = @At("HEAD"))
    private void lockon$prepareItemLaunch(net.minecraft.world.level.Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!level.isClientSide) return;

        Minecraft mc = Minecraft.getInstance();
        // Sadece omuz kamerasında ve kilitli değilken hedefi önceden hesaplar
        if (CameraViewConfig.ENABLE_SHOULDER_CAM.get() && !LockState.isLocked() && !mc.options.getCameraType().isFirstPerson()) {
            // Bu kısım CrosshairTargetHelper'ın güncel veriyi hazır tutmasını sağlar
            CrosshairTargetHelper.getCrosshairTarget(128.0);
        }
    }
}