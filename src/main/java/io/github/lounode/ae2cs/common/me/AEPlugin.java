package io.github.lounode.ae2cs.common.me;

import appeng.api.features.GridLinkables;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.item.tools.ToolLinkableHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID)
public class AEPlugin
{
    private static String INTEGRATED_INTERFACE_GROUP_NAME = "block.ae2cs.integrated_interface";
    private static String INTERFACE_GROUP_NAME = "block.ae2.interface";
    private static String METEORITE_PATTERN_PROVIDER_GROUP_NAME = "block.ae2cs.meteorite_pattern_provider";
    private static String ENDER_INTERFACE_GROUP_NAME = "block.ae2cs.ender_interface";
    private static String EX_ENDER_INTERFACE_GROUP_NAME = "block.ae2cs.extended_ender_interface";
    private static String QUARTZ_OSCILLATOR_CLOCK_GROUP_NAME = "block.ae2cs.quartz_oscillator_clock";
    private static String RESONATING_PATTERN_PROVIDER_GROUP_NAME = "block.ae2cs.resonating_pattern_provider";

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
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK.get(), 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK.get(), 3);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CIRCUIT_ETCHER_BLOCK.get(), 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_PULVERIZER_BLOCK.get(), 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.get(), 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK.get(), 4);
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK.get(), 4);

        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get(), 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.INTEGRATE_INTERFACE_PART.get(), 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.ENDER_INTERFACE_PART.get(), 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.ENDER_INTERFACE_BLOCK.get(), 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.EX_ENDER_INTERFACE_PART.get(), 1, EX_ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get(), 1, EX_ENDER_INTERFACE_GROUP_NAME);

        Upgrades.add(AEItems.FUZZY_CARD, AECSParts.ENDER_INTERFACE_PART.get(), 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.FUZZY_CARD, AECSBlocks.ENDER_INTERFACE_BLOCK.get(), 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.FUZZY_CARD, AECSParts.EX_ENDER_INTERFACE_PART.get(), 1, EX_ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.FUZZY_CARD, AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get(), 1, EX_ENDER_INTERFACE_GROUP_NAME);

        Upgrades.add(AEItems.REDSTONE_CARD, AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.get(), 1, QUARTZ_OSCILLATOR_CLOCK_GROUP_NAME);
        Upgrades.add(AEItems.REDSTONE_CARD, AECSParts.QUARTZ_OSCILLATOR_CLOCK_PART.get(), 1, QUARTZ_OSCILLATOR_CLOCK_GROUP_NAME);

        addGrowthCardSupport();
    }

    private static void addGrowthCardSupport()
    {
        // AE原版机器
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AEParts.STORAGE_BUS, 1);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AEParts.IMPORT_BUS, 1);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AEParts.EXPORT_BUS, 1);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AEParts.INTERFACE, 1, INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AEBlocks.INTERFACE, 1, INTERFACE_GROUP_NAME);

        // AECS机器
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSParts.INTEGRATE_INTERFACE_PART.get(), 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get(), 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSParts.METEORITE_PATTERN_PROVIDER_PART.get(), 1, METEORITE_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.get(), 1, METEORITE_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.get(), 1, RESONATING_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSParts.RESONATING_PATTERN_PROVIDER_PART.get(), 1, RESONATING_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSParts.ENDER_INTERFACE_PART.get(), 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSBlocks.ENDER_INTERFACE_BLOCK.get(), 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSParts.EX_ENDER_INTERFACE_PART.get(), 1, EX_ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard.get(), AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get(), 1, EX_ENDER_INTERFACE_GROUP_NAME);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagsUpdated(TagsUpdatedEvent event)
    {
        // 为全部工具添加可链接能力
        var tagOpt = ForgeRegistries.ITEMS.tags().getTag(Tags.Items.TOOLS);
        if (tagOpt.isEmpty()) return;

        tagOpt.stream().forEach(item -> {
            if(GridLinkables.get(item) == null)
                GridLinkables.register(item, ToolLinkableHandler.INSTANCE);
        });
    }
}
