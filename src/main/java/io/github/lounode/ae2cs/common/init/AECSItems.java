package io.github.lounode.ae2cs.common.init;

import appeng.api.upgrades.Upgrades;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.ids.AECSItemIds;
import io.github.lounode.ae2cs.common.item.*;
import io.github.lounode.ae2cs.common.item.tools.ender.*;
import io.github.lounode.ae2cs.common.item.tools.meteor.*;
import io.github.lounode.ae2cs.common.item.tools.resonating.*;
import io.github.lounode.ae2cs.common.item.upgrades.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class AECSItems
{
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

    public static final DeferredItem<PureCrystalItem> PURE_CERTUS_QUARTZ_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_CERTUS_QUARTZ_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 500d, 600));
    public static final DeferredItem<PureCrystalItem> PURE_FLUIX_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_FLUIX_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 1500d, 1200));
    public static final DeferredItem<PureCrystalItem> PURE_NETHER_QUARTZ_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_NETHER_QUARTZ_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 800d, 900));
    public static final DeferredItem<PureCrystalItem> PURE_ENDER_QUARTZ =
            registerPureCrystalItem(AECSItemIds.PURE_ENDER_QUARTZ,
                    properties -> new PureCrystalItem(properties, 900d, 800));
    public static final DeferredItem<PureCrystalItem> PURE_METEOR_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_METEOR_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 1200d, 1500));
    public static final DeferredItem<PureCrystalItem> PURE_RESONATING_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_RESONATING_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 2500d, 2400));
    public static final DeferredItem<PureCrystalItem> PURE_ENTRO_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_ENTRO_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 2000d, 2400));
    public static final DeferredItem<PureCrystalItem> PURE_REDSTONE_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_REDSTONE_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 1800d, 2000));
    public static final DeferredItem<PureCrystalItem> PURE_QUANTUM_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_QUANTUM_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 2320d, 3000));
    public static final DeferredItem<PureCrystalItem> PURE_ROSE_QUARTZ =
            registerPureCrystalItem(AECSItemIds.PURE_ROSE_QUARTZ,
                    properties -> new PureCrystalItem(properties, 2000d, 1800));
    public static final DeferredItem<PureCrystalItem> PURE_IRRADIATED_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_IRRADIATED_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 2900d, 2400));
    public static final DeferredItem<PureCrystalItem> PURE_EMBER_CRYSTAL =
            registerPureCrystalItem(AECSItemIds.PURE_EMBER_CRYSTAL,
                    properties -> new PureCrystalItem(properties, 2000d, 3600));

    public static final DeferredItem<CrystalSeedItem> CERTUS_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.CERTUS_QUARTZ_SEED, properties -> new CrystalSeedItem(properties, PURE_CERTUS_QUARTZ_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> FLUIX_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.FLUIX_CRYSTAL_SEED, properties -> new CrystalSeedItem(properties, PURE_FLUIX_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> NETHER_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.NETHER_QUARTZ_SEED, properties -> new CrystalSeedItem(properties, PURE_NETHER_QUARTZ_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ENDER_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.ENDER_QUARTZ_SEED, properties -> new CrystalSeedItem(properties, PURE_ENDER_QUARTZ));
    public static final DeferredItem<CrystalSeedItem> METEOR_SEED = registerCrystalSeedItem(AECSItemIds.METEOR_SEED, properties -> new CrystalSeedItem(properties, PURE_METEOR_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> RESONATING_SEED = registerCrystalSeedItem(AECSItemIds.RESONATING_SEED, properties -> new CrystalSeedItem(properties, PURE_RESONATING_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ENTRO_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.ENTRO_CRYSTAL_SEED, properties -> new CrystalSeedItem(properties, PURE_ENTRO_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> REDSTONE_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.REDSTONE_CRYSTAL_SEED, properties -> new CrystalSeedItem(properties, PURE_REDSTONE_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> QUANTUM_CRYSTAL_SEED = registerCrystalSeedItem(AECSItemIds.QUANTUM_CRYSTAL_SEED, properties -> new CrystalSeedItem(properties, PURE_QUANTUM_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> ROSE_QUARTZ_SEED = registerCrystalSeedItem(AECSItemIds.ROSE_QUARTZ_SEED, properties -> new CrystalSeedItem(properties, PURE_ROSE_QUARTZ));
    public static final DeferredItem<CrystalSeedItem> IRRADIATED_SEED = registerCrystalSeedItem(AECSItemIds.IRRADIATED_SEED, properties -> new CrystalSeedItem(properties, PURE_IRRADIATED_CRYSTAL));
    public static final DeferredItem<CrystalSeedItem> EMBER_SEED = registerCrystalSeedItem(AECSItemIds.EMBER_SEED, properties -> new CrystalSeedItem(properties, PURE_EMBER_CRYSTAL));

    public static final DeferredItem<Item> NETHER_QUARTZ_DUST = registerOtherItem(AECSItemIds.NETHER_QUARTZ_DUST, Item::new);
    public static final DeferredItem<Item> RESONATING_DUST = registerOtherItem(AECSItemIds.RESONATING_DUST, Item::new);
    public static final DeferredItem<Item> REDSTONE_CRYSTAL_DUST = registerOtherItem(AECSItemIds.REDSTONE_CRYSTAL_DUST, Item::new);
    public static final DeferredItem<Item> QUANTUM_CRYSTAL_DUST = registerOtherItem(AECSItemIds.QUANTUM_CRYSTAL_DUST, Item::new);
    public static final DeferredItem<Item> IRRADIATED_CRYSTAL_DUST = registerOtherItem(AECSItemIds.IRRADIATED_CRYSTAL_DUST, Item::new);

    public static final DeferredItem<Item> RESONATING_PRINT_PRESS = registerOtherItem(AECSItemIds.RESONATING_PRINT_PRESS, Item::new);
    public static final DeferredItem<Item> RESONATING_CIRCUIT_PRINT = registerOtherItem(AECSItemIds.RESONATING_CIRCUIT_PRINT, Item::new);
    public static final DeferredItem<Item> RESONATING_PROCESSOR = registerOtherItem(AECSItemIds.RESONATING_PROCESSOR, Item::new);
    public static final DeferredItem<Item> SIMPLE_CIRCUIT_PRINT = registerOtherItem(AECSItemIds.SIMPLE_CIRCUIT_PRINT, Item::new);
    public static final DeferredItem<Item> SIMPLE_PROCESSOR = registerOtherItem(AECSItemIds.SIMPLE_PROCESSOR, Item::new);

    public static final DeferredItem<Item> BLANK_PRINT_PRESS = registerOtherItem(AECSItemIds.BLANK_PRINT_PRESS, Item::new);
    public static final DeferredItem<Item> ENDER_BLANK_PRINT_PRESS = registerOtherItem(AECSItemIds.ENDER_BLANK_PRINT_PRESS, Item::new);

    public static final DeferredItem<ExtendedEnderInterfaceUpgradeItem> EX_ENDER_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.EX_ENDER_INTERFACE_UPGRADE, ExtendedEnderInterfaceUpgradeItem::new);
    public static final DeferredItem<ExtendedIntegratedInterfaceUpgradeItem> EX_INTEGRATED_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.EX_INTEGRATED_INTERFACE_UPGRADE, ExtendedIntegratedInterfaceUpgradeItem::new);
    public static final DeferredItem<ExtendedResonatingPatternProviderUpgradeItem> EX_RESONATING_PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.EX_RESONATING_PATTERN_PROVIDER_UPGRADE, ExtendedResonatingPatternProviderUpgradeItem::new);

    public static final DeferredItem<EnderInterfaceUpgradeItem> ENDER_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.ENDER_INTERFACE_UPGRADE, EnderInterfaceUpgradeItem::new);
    public static final DeferredItem<IntegratedInterfaceUpgradeItem> INTEGRATED_INTERFACE_UPGRADE = registerOtherItem(AECSItemIds.INTEGRATED_INTERFACE_UPGRADE, IntegratedInterfaceUpgradeItem::new);
    public static final DeferredItem<ResonatingPatternProviderUpgradeItem> RESONATING_PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.RESONATING_PATTERN_PROVIDER_UPGRADE, ResonatingPatternProviderUpgradeItem::new);
    public static final DeferredItem<PatternProviderUpgradeItem> PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.PATTERN_PROVIDER_UPGRADE, PatternProviderUpgradeItem::new);
    public static final DeferredItem<MeteorPatternProviderUpgradeItem> METEOR_PATTERN_PROVIDER_UPGRADE = registerOtherItem(AECSItemIds.METEOR_PATTERN_PROVIDER_UPGRADE, MeteorPatternProviderUpgradeItem::new);

    public static final DeferredItem<Item> crystalGrowthCard = registerOtherItem(AECSItemIds.CRYSTAL_GROWTH_CARD, Upgrades::createUpgradeCardItem);
    public static final DeferredItem<EnderLinkerItem> enderLink = registerOtherItem(AECSItemIds.ENDER_LINKER, EnderLinkerItem::new);
    public static final DeferredItem<ResonatingPatternItem> RESONATING_PATTERN = registerOtherItem(AECSItemIds.RESONATING_PATTERN, ResonatingPatternItem::new);
    public static final DeferredItem<ResonatingPatternConverterItem> RESONATING_PATTERN_CONVERTER = registerOtherItem(AECSItemIds.RESONATING_PATTERN_CONVERTER, properties -> new ResonatingPatternConverterItem(properties.stacksTo(1)));

    public static final DeferredItem<EnderSwordItem> ENDER_CRYSTAL_SWORD = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_SWORD, properties -> new EnderSwordItem(properties.stacksTo(1)));
    public static final DeferredItem<EnderAxeItem> ENDER_CRYSTAL_AXE = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_AXE, properties -> new EnderAxeItem(properties.stacksTo(1)));
    public static final DeferredItem<EnderPickaxeItem> ENDER_CRYSTAL_PICKAXE = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_PICKAXE, properties -> new EnderPickaxeItem(properties.stacksTo(1)));
    public static final DeferredItem<EnderShovelItem> ENDER_CRYSTAL_SHOVEL = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_SHOVEL, properties -> new EnderShovelItem(properties.stacksTo(1)));
    public static final DeferredItem<EnderHoeItem> ENDER_CRYSTAL_HOE = registerToolsItem(AECSItemIds.ENDER_CRYSTAL_HOE, properties -> new EnderHoeItem(properties.stacksTo(1)));

    public static final DeferredItem<MeteorSwordItem> METEOR_CRYSTAL_SWORD = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_SWORD, properties -> new MeteorSwordItem(properties.stacksTo(1)));
    public static final DeferredItem<MeteorAxeItem> METEOR_CRYSTAL_AXE = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_AXE, properties -> new MeteorAxeItem(properties.stacksTo(1)));
    public static final DeferredItem<MeteorPickaxeItem> METEOR_CRYSTAL_PICKAXE = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_PICKAXE, properties -> new MeteorPickaxeItem(properties.stacksTo(1)));
    public static final DeferredItem<MeteorShovelItem> METEOR_CRYSTAL_SHOVEL = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_SHOVEL, properties -> new MeteorShovelItem(properties.stacksTo(1)));
    public static final DeferredItem<MeteorHoeItem> METEOR_CRYSTAL_HOE = registerToolsItem(AECSItemIds.METEOR_CRYSTAL_HOE, properties -> new MeteorHoeItem(properties.stacksTo(1)));

    public static final DeferredItem<ResonatingSwordItem> RESONATING_CRYSTAL_SWORD = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_SWORD, properties -> new ResonatingSwordItem(properties.stacksTo(1)));
    public static final DeferredItem<ResonatingAxeItem> RESONATING_CRYSTAL_AXE = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_AXE, properties -> new ResonatingAxeItem(properties.stacksTo(1)));
    public static final DeferredItem<ResonatingPickaxeItem> RESONATING_CRYSTAL_PICKAXE = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_PICKAXE, properties -> new ResonatingPickaxeItem(properties.stacksTo(1)));
    public static final DeferredItem<ResonatingShovelItem> RESONATING_CRYSTAL_SHOVEL = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_SHOVEL, properties -> new ResonatingShovelItem(properties.stacksTo(1)));
    public static final DeferredItem<ResonatingHoeItem> RESONATING_CRYSTAL_HOE = registerToolsItem(AECSItemIds.RESONATING_CRYSTAL_HOE, properties -> new ResonatingHoeItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> FLOUR = registerOtherItem(AECSItemIds.FLOUR, Item::new);
    public static final DeferredItem<Item> WOODEN_GEAR = registerOtherItem(AECSItemIds.WOODEN_GEAR, Item::new);

    // 快速物品状态
    public static Item.Properties defaultBuilder()
    {
        return new Item.Properties();
    }

    // getter
    public static List<DeferredItem<? extends Item>> getALL()
    {
        return Collections.unmodifiableList(ALL);
    }

    public static List<DeferredItem<CrystalSeedItem>> getCrystalSeeds()
    {
        return Collections.unmodifiableList(CRYSTAL_SEEDS);
    }

    public static List<DeferredItem<PureCrystalItem>> getPureCrystal()
    {
        return Collections.unmodifiableList(PURE_CRYSTAL);
    }

    public static List<DeferredItem<? extends Item>> getTools()
    {
        return Collections.unmodifiableList(TOOLS);
    }

    public static List<DeferredItem<? extends Item>> getOthers()
    {
        return Collections.unmodifiableList(OTHERS);
    }

    // 工具方法
    private static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> factory)
    {
        DeferredItem<T> obj = ITEMS.registerItem(name, factory, AECSItems::defaultBuilder);
        ALL.add(obj);
        return obj;
    }

    private static DeferredItem<CrystalSeedItem> registerCrystalSeedItem(String name, Function<Item.Properties, CrystalSeedItem> factory)
    {
        DeferredItem<CrystalSeedItem> obj = registerItem(name, factory);
        CRYSTAL_SEEDS.add(obj);
        return obj;
    }

    private static DeferredItem<PureCrystalItem> registerPureCrystalItem(String name, Function<Item.Properties, PureCrystalItem> factory)
    {
        DeferredItem<PureCrystalItem> obj = registerItem(name, factory);
        PURE_CRYSTAL.add(obj);
        return obj;
    }

    private static <T extends Item> DeferredItem<T> registerToolsItem(String name, Function<Item.Properties, T> factory)
    {
        DeferredItem<T> obj = registerItem(name, factory);
        TOOLS.add(obj);
        return obj;
    }

    private static <T extends Item> DeferredItem<T> registerOtherItem(String name, Function<Item.Properties, T> factory)
    {
        DeferredItem<T> obj = registerItem(name, factory);
        OTHERS.add(obj);
        return obj;
    }

    // 注册监听
    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
