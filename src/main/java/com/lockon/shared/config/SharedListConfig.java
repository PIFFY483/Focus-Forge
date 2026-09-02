package com.lockon.shared.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * OLD (Focus Forge) ve NEW (eski BRS) kamera/kilit sistemleri, entity
 * blacklist'i ve blok listelerini (acquisition/preclusion) neredeyse birebir
 * aynı şekilde kullanıyordu — her biri kendi ForgeConfigSpec'inde ayrı ayrı
 * tanımlıyor, ayrı ayrı .toml dosyasına yazıyor ve ayrı GUI ekranlarıyla
 * (LockOnEntityListScreen / LockOnBlockListScreen) düzenleniyordu.
 * <p>
 * Bu sınıf o üç listeyi TEK bir yerde, TEK bir config dosyasında
 * (lockon-shared-lists.toml) tutar. Hem {@code com.lockon.config.LockOnConfig}
 * (OLD) hem de {@code com.lockon.brs.config.LockOnConfig} (NEW) kendi statik
 * alanlarını buraya yönlendirir (alias) — böylece iki kamera modu da AYNI
 * listeyi okur/yazar ve GUI'de yapılan bir değişiklik her iki modda da
 * geçerli olur.
 */
public final class SharedListConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Client CLIENT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_TARGET_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TARGET_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCK_ACQUISITION_BLOCK_LIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOCK_PRECLUSION_BLOCK_LIST;

    private SharedListConfig() {
    }

    static {
        Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        ENABLE_TARGET_BLACKLIST = CLIENT.enableTargetBlacklist;
        TARGET_BLACKLIST = CLIENT.targetBlacklist;
        LOCK_ACQUISITION_BLOCK_LIST = CLIENT.lockAcquisitionBlockList;
        LOCK_PRECLUSION_BLOCK_LIST = CLIENT.lockPreclusionBlockList;
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue enableTargetBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> targetBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> lockAcquisitionBlockList;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> lockPreclusionBlockList;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("EntityBlacklist");

            this.enableTargetBlacklist = builder
                    .comment("Varlık kara listesini etkinleştirir/devre dışı bırakır. " +
                            "OLD ve NEW kamera modları bu ayarı ortak kullanır.")
                    .define("enableTargetBlacklist", true);

            this.targetBlacklist = builder
                    .comment("Kilitlenemeyecek varlık adlarının listesi (örn: 'minecraft:creeper'). " +
                            "OLD ve NEW kamera modları bu listeyi ortak kullanır.")
                    .defineList("targetBlacklist",
                            List.of("minecraft:enderman", "minecraft:iron_golem", "minecraft:armor_stand"),
                            SharedListConfig::validateEntityId);

            builder.pop();

            builder.push("BlockLists");

            this.lockAcquisitionBlockList = builder
                    .comment("YENİ BİR KİLİTLENMENİN bu blokların arkasından veya içinden geçmesine izin " +
                            "veren blokların Whitelist'i. OLD ve NEW kamera modları bu listeyi ortak kullanır. " +
                            "Örnek: minecraft:tall_grass, #minecraft:mineable/hoe")
                    .defineList("lockAcquisitionBlockList",
                            List.of(
                                    "#brs:pass_through",
                                    "minecraft:grass",
                                    "minecraft:tall_grass",
                                    "minecraft:fern",
                                    "minecraft:large_fern",
                                    "minecraft:dead_bush",
                                    "minecraft:dandelion",
                                    "minecraft:poppy",
                                    "minecraft:sugar_cane"
                            ),
                            SharedListConfig::validateBlockId);

            this.lockPreclusionBlockList = builder
                    .comment("KİLİDİN KIRILMASINI ENGELLEYEN blokların Whitelist'i. Hedef bu blokların " +
                            "arkasına geçerse kilit KIRILMAZ. OLD ve NEW kamera modları bu listeyi ortak kullanır.")
                    .defineList("lockPreclusionBlockList",
                            List.of("minecraft:tall_grass", "minecraft:dandelion", "minecraft:poppy", "minecraft:sugar_cane"),
                            SharedListConfig::validateBlockId);

            builder.pop();
        }
    }

    public static boolean validateBlockId(Object o) {
        if (o instanceof String blockId && !blockId.isEmpty()) {
            if (blockId.startsWith("#")) return true;
            ResourceLocation rl = ResourceLocation.tryParse(blockId);
            return rl != null && BuiltInRegistries.BLOCK.containsKey(rl);
        }
        return false;
    }

    public static boolean validateEntityId(Object o) {
        if (o instanceof String entityId && !entityId.isEmpty()) {
            if (entityId.startsWith("#")) return true;
            ResourceLocation rl = ResourceLocation.tryParse(entityId);
            return rl != null && BuiltInRegistries.ENTITY_TYPE.containsKey(rl);
        }
        return false;
    }
}
