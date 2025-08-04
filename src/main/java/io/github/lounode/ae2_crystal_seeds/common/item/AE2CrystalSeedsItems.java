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
    public static final Item pureRedstoneCrystal = make(prefix(ItemNames.PURE_REDSTONE_CRYSTAL), new Item(defaultBuilder()));
    public static final Item pureQuantumCrystal = make(prefix(ItemNames.PURE_QUANTUM_CRYSTAL), new Item(defaultBuilder()));
    public static final Item pureRoseQuartz = make(prefix(ItemNames.PURE_ROSE_QUARTZ), new Item(defaultBuilder()));
    public static final Item pureEnderQuartz = make(prefix(ItemNames.PURE_ENDER_QUARTZ), new Item(defaultBuilder()));
    public static final Item pureResonatingCrystal = make(prefix(ItemNames.PURE_RESONATING_CRYSTAL), new Item(defaultBuilder()));
    public static final Item pureMeteorCrystal = make(prefix(ItemNames.PURE_METEOR_CRYSTAL), new Item(defaultBuilder()));

    public static final CrystalSeedItem certusQuartzSeed = make(prefix(ItemNames.CERTUS_QUARTZ_SEED), new CrystalSeedItem(defaultBuilder(), pureCertusQuartzCrystal));
    public static final CrystalSeedItem fluixCrystalSeed = make(prefix(ItemNames.FLUIX_CRYSTAL_SEED), new CrystalSeedItem(defaultBuilder(), pureFluixCrystal));
    public static final CrystalSeedItem netherQuartzSeed = make(prefix(ItemNames.NETHER_QUARTZ_SEED), new CrystalSeedItem(defaultBuilder(), pureNetherQuartzCrystal));
    public static final CrystalSeedItem entroCrystalSeed = make(prefix(ItemNames.ENTRO_CRYSTAL_SEED), new CrystalSeedItem(defaultBuilder(), pureEntroCrystal));
    public static final CrystalSeedItem redstoneCrystalSeed = make(prefix(ItemNames.REDSTONE_CRYSTAL_SEED), new CrystalSeedItem(defaultBuilder(), pureRedstoneCrystal));
    public static final CrystalSeedItem quantumCrystalSeed = make(prefix(ItemNames.QUANTUM_CRYSTAL_SEED), new CrystalSeedItem(defaultBuilder(), pureQuantumCrystal));
    public static final CrystalSeedItem roseQuartzSeed = make(prefix(ItemNames.ROSE_QUARTZ_SEED), new CrystalSeedItem(defaultBuilder(), pureRoseQuartz));
    public static final CrystalSeedItem enderQuartzSeed = make(prefix(ItemNames.ENDER_QUARTZ_SEED), new CrystalSeedItem(defaultBuilder(), pureEnderQuartz));
    public static final CrystalSeedItem resonatingSeed = make(prefix(ItemNames.RESONATING_SEED), new CrystalSeedItem(defaultBuilder(), pureResonatingCrystal));
    public static final CrystalSeedItem meteorSeed = make(prefix(ItemNames.METEOR_SEED), new CrystalSeedItem(defaultBuilder(), pureMeteorCrystal));

    public static final Item crystalGrowthCard = make(prefix(ItemNames.CRYSTAL_GROWTH_CARD), new Item(defaultBuilder()));

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

    public static final Item[] CRYSTAL_SEEDS = new Item[] {
            certusQuartzSeed, fluixCrystalSeed, netherQuartzSeed,
            entroCrystalSeed,
            redstoneCrystalSeed,
            quantumCrystalSeed,
            roseQuartzSeed,
            enderQuartzSeed,
            resonatingSeed,
            meteorSeed
    };


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
