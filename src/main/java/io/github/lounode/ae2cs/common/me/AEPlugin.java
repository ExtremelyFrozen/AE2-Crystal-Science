package io.github.lounode.ae2cs.common.me;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.neoforged.bus.api.IEventBus;

public class AEPlugin
{
    private static String INTEGRATED_INTERFACE_GROUP_NAME = "block.ae2cs.integrated_interface";
    private static String INTERFACE_GROUP_NAME = "block.ae2.interface";
    private static String METEORITE_PATTERN_PROVIDER_GROUP_NAME = "block.ae2cs.meteorite_pattern_provider";

    /**
     * 在mod入口点调用
     */
    public static void onInit()
    {

    }

    /**
     * init后立刻运行此段代码，在这里进行注册相关内容
     */
    public static void onRegister(IEventBus modEventBus, IEventBus gameEventBus)
    {

    }

    /**
     * 在FMLCommonSetupEvent阶段调用
     */
    public static void onCommonSetup()
    {
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK, 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK, 3);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CIRCUIT_ETCHER_BLOCK, 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_PULVERIZER_BLOCK, 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK, 4);

        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.INTEGRATED_INTERFACE_BLOCK, 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.INTEGRATE_INTERFACE_PART, 1, INTEGRATED_INTERFACE_GROUP_NAME);

        addGrowthCardSupport();
    }

    private static void addGrowthCardSupport()
    {
        // AE原版机器
        Upgrades.add(AECSItems.crystalGrowthCard, AEParts.STORAGE_BUS, 1);
        Upgrades.add(AECSItems.crystalGrowthCard, AEParts.IMPORT_BUS, 1);
        Upgrades.add(AECSItems.crystalGrowthCard, AEParts.EXPORT_BUS, 1);
        Upgrades.add(AECSItems.crystalGrowthCard, AEParts.INTERFACE, 1, INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AEBlocks.INTERFACE, 1, INTERFACE_GROUP_NAME);

        // AECS机器
        Upgrades.add(AECSItems.crystalGrowthCard, AECSParts.INTEGRATE_INTERFACE_PART, 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSBlocks.INTEGRATED_INTERFACE_BLOCK, 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSParts.METEORITE_PATTERN_PROVIDER_PART, 1, METEORITE_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, 1, METEORITE_PATTERN_PROVIDER_GROUP_NAME);

    }
}
