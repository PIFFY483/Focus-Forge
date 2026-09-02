package com.lockon.brs.network;

import com.lockon.LockOnMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class BRSPackets {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LockOnMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId = 0;

    private static int id() {
        return nextId++;
    }

    public static void register() {
        CHANNEL.registerMessage(id(),
                EnergySyncPacket.class,
                EnergySyncPacket::encode,
                EnergySyncPacket::decode,
                EnergySyncPacket::handle);
    }
}