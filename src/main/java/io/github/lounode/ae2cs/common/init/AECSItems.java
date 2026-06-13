package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.ids.AECSItemIds;
import io.github.lounode.ae2cs.common.item.*;
import io.github.lounode.ae2cs.common.item.tools.ender.*;
import io.github.lounode.ae2cs.common.item.tools.meteor.*;
import io.github.lounode.ae2cs.common.item.tools.resonating.*;
import io.github.lounode.ae2cs.common.item.upgrades.*;

import appeng.api.upgrades.Upgrades;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class AECSItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AECSConstants.MODID);

    /**
     * 全部注册物品
     */
    private static final List<DeferredItem<? extends Item>> ALL = new ArrayList<>();

    /**
     * 水晶种子
     */
    private static final List<DeferredItem<CrystalSeedItem>> CRYSTAL_SEEDS = new ArrayList<>();

    /**
     * 高纯水晶
     */
    private static final List<DeferredItem<PureCrystalItem>> PURE_CRYSTAL = new ArrayList<>();

    /**
     * 工具类（用于手持动画）
     */
    private static final List<DeferredItem<? extends Item>> TOOLS = new ArrayList<>();

    /**
     * 杂项物品
     */
    private static final List<DeferredItem<? extends Item>> OTHERS = new ArrayList<>();

    public static final DeferredItem<PureCrystalItem> PURE_CERTUS_QUARTZ_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_CERTUS_QUARTZ_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 500d, 600));
    public static final DeferredItem<PureCrystalItem> PURE_FLUIX_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_FLUIX_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1500d, 1200));
    public static final DeferredItem<PureCrystalItem> PURE_NETHER_QUARTZ_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_NETHER_QUARTZ_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 800d, 900));
    public static final DeferredItem<PureCrystalItem> PURE_ENDER_QUARTZ = registerPureCrystalItem(AECSItemIds.PURE_ENDER_QUARTZ,
            () -> new PureCrystalItem(defaultBuilder(), 900d, 800));
    public static final DeferredItem<PureCrystalItem> PURE_METEOR_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_METEOR_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1200d, 1500));
    public static final DeferredItem<PureCrystalItem> PURE_RESONATING_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_RESONATING_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 2500d, 2400));
    public static final DeferredItem<PureCrystalItem> PURE_ENTRO_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_ENTRO_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 2000d, 2400));
    public static final DeferredItem<PureCrystalItem> PURE_REDSTONE_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_REDSTONE_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1800d, 2000));
    public static final DeferredItem<PureCrystalItem> PURE_QUANTUM_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_QUANTUM_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 2320d, 3000));
    public static final DeferredItem<PureCrystalItem> PURE_ROSE_QUARTZ = registerPureCrystalItem(AECSItemIds.PURE_ROSE_QUARTZ,
            () -> new PureCrystalItem(defaultBuilder(), 2000d, 1800));
    public static final DeferredItem<PureCrystalItem> PURE_IRRADIATED_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_IRRADIATED_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 2900d, 2400));
    public static final DeferredItem<PureCrystalItem> PURE_EMBER_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_EMBER_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 2000d, 3600));
    public static final DeferredItem<PureCrystalItem> PURE_LINK_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_LINK_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1800d, 1800));
    public static final DeferredItem<PureCrystalItem> PURE_OVERLOAD_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_OVERLOAD_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1000d, 600));
    public static final DeferredItem<PureCrystalItem> PURE_DATA_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_DATA_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1000d, 600));
    public static final DeferredItem<PureCrystalItem> PURE_ENERGIZED_FLUIX_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_ENERGIZED_FLUIX_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1000d, 600));
    public static final DeferredItem<PureCrystalItem> PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL = registerPureCrystalItem(AECSItemIds.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL,
            () -> new PureCrystalItem(defaultBuilder(), 1000d, 600));

    public static final DeferredItem<CrystalSeedItem> CERTUS_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.CERTUS_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_CERTUS_QUARTZ_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> FLUIX_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.FLUIX_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_FLUIX_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> NETHER_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.NETHER_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_NETHER_QUARTZ_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ENDER_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.ENDER_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_ENDER_QUARTZ));
    public static final DeferredItem<CrystalSeedItem> METEOR_SEED = registerCrystalSeedItem(AECSItemIds.METEOR_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_METEOR_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> RESONATING_SEED = registerCrystalSeedItem(AECSItemIds.RESONATING_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_RESONATING_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ENTRO_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.ENTRO_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_ENTRO_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> REDSTONE_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.REDSTONE_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_REDSTONE_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> QUANTUM_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.QUANTUM_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_QUANTUM_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ROSE_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.ROSE_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_ROSE_QUARTZ));
    public static final DeferredItem<CrystalSeedItem> IRRADIATED_SEED = registerCrystalSeedItem(AECSItemIds.IRRADIATED_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_IRRADIATED_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> EMBER_SEED = registerCrystalSeedItem(AECSItemIds.EMBER_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_EMBER_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> LINK_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.LINK_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_LINK_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> OVERLOAD_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.OVERLOAD_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_OVERLOAD_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> DATA_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.DATA_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_DATA_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ENERGIZED_FLUIX_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.ENERGIZED_FLUIX_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_ENERGIZED_FLUIX_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ENERGIZED_CERTUS_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.ENERGIZED_CERTUS_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL));

    public static final DeferredItem<Item> NETHER_QUARTZ_DUST = registerOtherItem(AECSItemIds.NETHER_QUARTZ_DUST, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> RESONATING_DUST = registerOtherItem(AECSItemIds.RESONATING_DUST, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> REDSTONE_CRYSTAL_DUST = registerOtherItem(AECSItemIds.REDSTONE_CRYSTAL_DUST, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> QUANTUM_CRYSTAL_DUST = registerOtherItem(AECSItemIds.QUANTUM_CRYSTAL_DUST, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> IRRADIATED_CRYSTAL_DUST = registerOtherItem(AECSItemIds.IRRADIATED_CRYSTAL_DUST, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> LINK_CRYSTAL_DUST = registerOtherItem(AECSItemIds.LINK_CRYSTAL_DUST, () -> new Item(defaultBuilder()));

    public static final DeferredItem<Item> RESONATING_PRINT_PRESS = registerOtherItem(AECSItemIds.RESONATING_PRINT_PRESS, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> RESONATING_CIRCUIT_PRINT = registerOtherItem(AECSItemIds.RESONATING_CIRCUIT_PRINT, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> RESONATING_PROCESSOR = registerOtherItem(AECSItemIds.RESONATING_PROCESSOR, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> SIMPLE_CIRCUIT_PRINT = registerOtherItem(AECSItemIds.SIMPLE_CIRCUIT_PRINT, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> SIMPLE_PROCESSOR = registerOtherItem(AECSItemIds.SIMPLE_PROCESSOR, () -> new Item(defaultBuilder()));

    public static final DeferredItem<Item> BLANK_PRINT_PRESS = registerOtherItem(AECSItemIds.BLANK_PRINT_PRESS, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> ENDER_BLANK_PRINT_PRESS = registerOtherItem(AECSItemIds.ENDER_BLANK_PRINT_PRESS, () -> new Item(defaultBuilder()));

    public static final DeferredItem<ExtendedEnderInterfaceUpgradeItem> EX_ENDER_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.EX_ENDER_INTERFACE_UPGRADE, () -> new ExtendedEnderInterfaceUpgradeItem(defaultBuilder()));
    public static final DeferredItem<ExtendedIntegratedInterfaceUpgradeItem> EX_INTEGRATED_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.EX_INTEGRATED_INTERFACE_UPGRADE, () -> new ExtendedIntegratedInterfaceUpgradeItem(defaultBuilder()));
    public static final DeferredItem<ExtendedResonatingPatternProviderUpgradeItem> EX_RESONATING_PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.EX_RESONATING_PATTERN_PROVIDER_UPGRADE, () -> new ExtendedResonatingPatternProviderUpgradeItem(defaultBuilder()));

    public static final DeferredItem<EnderInterfaceUpgradeItem> ENDER_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.ENDER_INTERFACE_UPGRADE, () -> new EnderInterfaceUpgradeItem(defaultBuilder()));
    public static final DeferredItem<IntegratedInterfaceUpgradeItem> INTEGRATED_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.INTEGRATED_INTERFACE_UPGRADE, () -> new IntegratedInterfaceUpgradeItem(defaultBuilder()));
    public static final DeferredItem<ResonatingPatternProviderUpgradeItem> RESONATING_PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.RESONATING_PATTERN_PROVIDER_UPGRADE, () -> new ResonatingPatternProviderUpgradeItem(defaultBuilder()));
    public static final DeferredItem<PatternProviderUpgradeItem> PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.PATTERN_PROVIDER_UPGRADE, () -> new PatternProviderUpgradeItem(defaultBuilder()));
    public static final DeferredItem<MeteorPatternProviderUpgradeItem> METEOR_PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.METEOR_PATTERN_PROVIDER_UPGRADE, () -> new MeteorPatternProviderUpgradeItem(defaultBuilder()));

    public static final DeferredItem<Item> crystalGrowthCard = registerOtherItem(AECSItemIds.CRYSTAL_GROWTH_CARD, () -> Upgrades.createUpgradeCardItem(defaultBuilder()));
    public static final DeferredItem<Item> OVERLOAD_CARD = registerOtherItem(AECSItemIds.OVERLOAD_CARD, () -> Upgrades.createUpgradeCardItem(defaultBuilder()));
    public static final DeferredItem<EnderLinkerItem> enderLink = registerOtherItem(AECSItemIds.ENDER_LINKER, () -> new EnderLinkerItem(defaultBuilder()));
    public static final DeferredItem<ResonatingLinkerItem> RESONATING_LINKER = registerOtherItem(AECSItemIds.RESONATING_LINKER, () -> new ResonatingLinkerItem(defaultBuilder()));
    public static final DeferredItem<MirrorLinkerItem> MIRROR_LINKER = registerOtherItem(AECSItemIds.MIRROR_LINKER, () -> new MirrorLinkerItem(defaultBuilder()));
    public static final DeferredItem<ResonatingMemoryCardItem> RESONATING_MEMORY_CARD = registerOtherItem(AECSItemIds.RESONATING_MEMORY_CARD, () -> new ResonatingMemoryCardItem(defaultBuilder()));
    public static final DeferredItem<ResonatingPatternItem> RESONATING_PATTERN = registerOtherItem(AECSItemIds.RESONATING_PATTERN, () -> new ResonatingPatternItem(defaultBuilder()));
    public static final DeferredItem<ResonatingPatternConverterItem> RESONATING_PATTERN_CONVERTER = registerOtherItem(AECSItemIds.RESONATING_PATTERN_CONVERTER, () -> new ResonatingPatternConverterItem(defaultBuilder().stacksTo(1)));

    public static final DeferredItem<EnderSwordItem> ENDER_CRYSTAL_SWORD = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_SWORD, () -> new EnderSwordItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<EnderAxeItem> ENDER_CRYSTAL_AXE = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_AXE, () -> new EnderAxeItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<EnderPickaxeItem> ENDER_CRYSTAL_PICKAXE = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_PICKAXE, () -> new EnderPickaxeItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<EnderShovelItem> ENDER_CRYSTAL_SHOVEL = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_SHOVEL, () -> new EnderShovelItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<EnderHoeItem> ENDER_CRYSTAL_HOE = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_HOE, () -> new EnderHoeItem(defaultBuilder().stacksTo(1)));

    public static final DeferredItem<MeteorSwordItem> METEOR_CRYSTAL_SWORD = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_SWORD, () -> new MeteorSwordItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<MeteorAxeItem> METEOR_CRYSTAL_AXE = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_AXE, () -> new MeteorAxeItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<MeteorPickaxeItem> METEOR_CRYSTAL_PICKAXE = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_PICKAXE, () -> new MeteorPickaxeItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<MeteorShovelItem> METEOR_CRYSTAL_SHOVEL = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_SHOVEL, () -> new MeteorShovelItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<MeteorHoeItem> METEOR_CRYSTAL_HOE = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_HOE, () -> new MeteorHoeItem(defaultBuilder().stacksTo(1)));

    public static final DeferredItem<ResonatingSwordItem> RESONATING_CRYSTAL_SWORD = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_SWORD, () -> new ResonatingSwordItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<ResonatingAxeItem> RESONATING_CRYSTAL_AXE = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_AXE, () -> new ResonatingAxeItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<ResonatingPickaxeItem> RESONATING_CRYSTAL_PICKAXE = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_PICKAXE, () -> new ResonatingPickaxeItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<ResonatingShovelItem> RESONATING_CRYSTAL_SHOVEL = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_SHOVEL, () -> new ResonatingShovelItem(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<ResonatingHoeItem> RESONATING_CRYSTAL_HOE = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_HOE, () -> new ResonatingHoeItem(defaultBuilder().stacksTo(1)));

    public static final DeferredItem<Item> FLOUR = registerOtherItem(AECSItemIds.FLOUR, () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> WOODEN_GEAR = registerOtherItem(AECSItemIds.WOODEN_GEAR, () -> new Item(defaultBuilder()));

    // 快速物品状态
    public static Item.Properties defaultBuilder() {
        return new Item.Properties();
    }

    // getter
    public static List<DeferredItem<? extends Item>> getALL() {
        return Collections.unmodifiableList(ALL);
    }

    public static List<DeferredItem<CrystalSeedItem>> getCrystalSeeds() {
        return Collections.unmodifiableList(CRYSTAL_SEEDS);
    }

    public static List<DeferredItem<PureCrystalItem>> getPureCrystal() {
        return Collections.unmodifiableList(PURE_CRYSTAL);
    }

    public static List<DeferredItem<? extends Item>> getTools() {
        return Collections.unmodifiableList(TOOLS);
    }

    public static List<DeferredItem<? extends Item>> getOthers() {
        return Collections.unmodifiableList(OTHERS);
    }

    // 工具方法
    private static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> supplier) {
        DeferredItem<T> obj = ITEMS.register(name, () -> {
            T t = supplier.get();
            return t instanceof Item ? t : (T) new Item(new Item.Properties());
        });
        ALL.add(obj);
        return obj;
    }

    private static DeferredItem<CrystalSeedItem> registerCrystalSeedItem(String name, Supplier<CrystalSeedItem> sup) {
        DeferredItem<CrystalSeedItem> obj = registerItem(name, sup);
        CRYSTAL_SEEDS.add(obj);
        return obj;
    }

    private static DeferredItem<PureCrystalItem> registerPureCrystalItem(String name, Supplier<PureCrystalItem> sup) {
        DeferredItem<PureCrystalItem> obj = registerItem(name, sup);
        PURE_CRYSTAL.add(obj);
        return obj;
    }

    private static <T extends Item> DeferredItem<T> registerToolsItem(String name, Supplier<T> sup) {
        DeferredItem<T> obj = registerItem(name, sup);
        TOOLS.add(obj);
        return obj;
    }

    private static <T extends Item> DeferredItem<T> registerOtherItem(String name, Supplier<T> sup) {
        DeferredItem<T> obj = registerItem(name, sup);
        OTHERS.add(obj);
        return obj;
    }

    // 注册监听
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
