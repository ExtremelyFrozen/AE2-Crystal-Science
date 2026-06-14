package io.github.lounode.ae2cs.common.me;

import appeng.api.features.GridLinkables;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.WirelessTerminalItem;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.item.tools.ToolLinkableHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class AEPlugin
{
    private static String INTEGRATED_INTERFACE_GROUP_NAME = "block.ae2cs.integrated_interface";
    private static String INTERFACE_GROUP_NAME = "block.ae2.interface";
    private static String METEORITE_PATTERN_PROVIDER_GROUP_NAME = "block.ae2cs.meteorite_pattern_provider";
    private static String ENDER_INTERFACE_GROUP_NAME = "block.ae2cs.ender_interface";
    private static String EX_ENDER_INTERFACE_GROUP_NAME = "block.ae2cs.extended_ender_interface";
    private static String QUARTZ_OSCILLATOR_CLOCK_GROUP_NAME = "block.ae2cs.quartz_oscillator_clock";
    private static String RESONATING_PATTERN_PROVIDER_GROUP_NAME = "block.ae2cs.resonating_pattern_provider";
    private static String WIRELESS_TERMINAL_GROUP_NAME = GuiText.WirelessTerminals.getTranslationKey();

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
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK, 4);

        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.INTEGRATED_INTERFACE_BLOCK, 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.INTEGRATE_INTERFACE_PART, 1, INTEGRATED_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.ENDER_INTERFACE_PART, 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.ENDER_INTERFACE_BLOCK, 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSParts.EX_ENDER_INTERFACE_PART, 1, EX_ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.CRAFTING_CARD, AECSBlocks.EX_ENDER_INTERFACE_BLOCK, 1, EX_ENDER_INTERFACE_GROUP_NAME);

        Upgrades.add(AEItems.FUZZY_CARD, AECSParts.ENDER_INTERFACE_PART, 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.FUZZY_CARD, AECSBlocks.ENDER_INTERFACE_BLOCK, 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.FUZZY_CARD, AECSParts.EX_ENDER_INTERFACE_PART, 1, EX_ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AEItems.FUZZY_CARD, AECSBlocks.EX_ENDER_INTERFACE_BLOCK, 1, EX_ENDER_INTERFACE_GROUP_NAME);

        Upgrades.add(AEItems.REDSTONE_CARD, AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK, 1, QUARTZ_OSCILLATOR_CLOCK_GROUP_NAME);
        Upgrades.add(AEItems.REDSTONE_CARD, AECSParts.QUARTZ_OSCILLATOR_CLOCK_PART, 1, QUARTZ_OSCILLATOR_CLOCK_GROUP_NAME);

        GridLinkables.register(AECSItems.WIRELESS_RESONANT_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
        Upgrades.add(AEItems.ENERGY_CARD, AECSItems.WIRELESS_RESONANT_TERMINAL, 2, WIRELESS_TERMINAL_GROUP_NAME);
        BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath("megacells", "greater_energy_card"))
                .ifPresent(card -> Upgrades.add(card, AECSItems.WIRELESS_RESONANT_TERMINAL, 2,
                        WIRELESS_TERMINAL_GROUP_NAME));

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
        Upgrades.add(AECSItems.crystalGrowthCard, AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, 1, RESONATING_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSParts.RESONATING_PATTERN_PROVIDER_PART, 1, RESONATING_PATTERN_PROVIDER_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSParts.ENDER_INTERFACE_PART, 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSBlocks.ENDER_INTERFACE_BLOCK, 1, ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSParts.EX_ENDER_INTERFACE_PART, 1, EX_ENDER_INTERFACE_GROUP_NAME);
        Upgrades.add(AECSItems.crystalGrowthCard, AECSBlocks.EX_ENDER_INTERFACE_BLOCK, 1, EX_ENDER_INTERFACE_GROUP_NAME);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagsUpdated(TagsUpdatedEvent event)
    {
        // 为全部工具添加可链接能力
        var tagOpt = BuiltInRegistries.ITEM.getTag(Tags.Items.TOOLS);
        if (tagOpt.isEmpty()) return;

        for (var holder : tagOpt.get())
        {
            if (GridLinkables.get(holder.value()) == null)
                GridLinkables.register(holder.value(), ToolLinkableHandler.INSTANCE);
        }
    }
}
