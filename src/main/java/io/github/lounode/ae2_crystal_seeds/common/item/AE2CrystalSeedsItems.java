package io.github.lounode.ae2_crystal_seeds.common.item;

import io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.ItemNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.ResourceLocationUtil.prefix;

public final class AE2CrystalSeedsItems {
    public static final Map<ResourceLocation, Item> ITEMS = new LinkedHashMap<>();
    public static final Item pureCertusQuartzCrystal = make(prefix(ItemNames.PURE_CERTUS_QUARTZ_CRYSTAL), new Item(defaultBuilder()));
    public static final Item pureFluixCrystal = make(prefix(ItemNames.PURE_FLUIX_CRYSTAL), new Item(defaultBuilder()));
    public static final Item pureNetherQuartzCrystal = make(prefix(ItemNames.PURE_NETHER_QUARTZ_CRYSTAL), new Item(defaultBuilder()));
    public static final Item pureEntroCrystal = make(prefix(ItemNames.PURE_ENTRO_CRYSTAL), new Item(defaultBuilder()));

    public static final CrystalSeedItem certusQuartzSeed = make(prefix(ItemNames.CERTUS_QUARTZ_SEED), new CrystalSeedItem(defaultBuilder(), pureCertusQuartzCrystal));
    public static final CrystalSeedItem fluixCrystalSeed = make(prefix(ItemNames.FLUIX_CRYSTAL_SEED), new CrystalSeedItem(defaultBuilder(), pureFluixCrystal));
    public static final CrystalSeedItem netherQuartzSeed = make(prefix(ItemNames.NETHER_QUARTZ_SEED), new CrystalSeedItem(defaultBuilder(), pureNetherQuartzCrystal));
    public static final CrystalSeedItem entroCrystalSeed = make(prefix(ItemNames.ENTRO_CRYSTAL_SEED), new CrystalSeedItem(defaultBuilder(), pureEntroCrystal));


    public static Item.Properties defaultBuilder() {
        return new Item.Properties();
    }

    private static Item.Properties stackTo16() {
        return defaultBuilder().stacksTo(16);
    }

    private static Item.Properties stackTo4() {
        return defaultBuilder().stacksTo(4);
    }

    private static Item.Properties unstackable() {
        return defaultBuilder().stacksTo(1);
    }


    private static <T extends Item> T make(ResourceLocation id, T item) {
        var old = ITEMS.put(id, item);
        if (old != null) {
            throw new IllegalArgumentException("Duplicate id " + id);
        }
        return item;
    }

    public static void registerItems(BiConsumer<Item, ResourceLocation> r) {
        for (var e : ITEMS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }
}
