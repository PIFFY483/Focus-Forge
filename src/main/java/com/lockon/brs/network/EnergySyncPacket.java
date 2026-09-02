package com.lockon.brs.network;

import com.lockon.brs.energy.ClientEnergyCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnergySyncPacket {

    private final float energy;
    private final float maxEnergy;
    private final boolean hasLockedEnemy;

    public EnergySyncPacket(float energy, float maxEnergy, boolean hasLockedEnemy) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.hasLockedEnemy = hasLockedEnemy;
    }

    public static void encode(EnergySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.energy);
        buf.writeFloat(msg.maxEnergy);
        buf.writeBoolean(msg.hasLockedEnemy);
    }

    public static EnergySyncPacket decode(FriendlyByteBuf buf) {
        return new EnergySyncPacket(buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(EnergySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientEnergyCache.update(msg.energy, msg.maxEnergy, msg.hasLockedEnemy))
        );
        context.setPacketHandled(true);
    }
}