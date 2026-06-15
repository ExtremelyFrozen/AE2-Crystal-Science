package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.me.logic.*;
import io.github.lounode.ae2cs.common.me.menuhost.ResonatingPatternConverterMenuHost;
import io.github.lounode.ae2cs.common.menu.*;
import io.github.lounode.ae2cs.common.menu.ResonantTemplateCodingTermMenu;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.*;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;

import appeng.helpers.IPatternTerminalMenuHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.PatternProviderMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AECSMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, AECSConstants.MODID);

    public static final Supplier<MenuType<CrystalGrowthChamberMenu>> CRYSTAL_GROWTH_CHAMBER_MENU = MENU_TYPES.register("crystal_growth_chamber_menu",
            () -> MenuTypeBuilder.create(CrystalGrowthChamberMenu::new, CrystalGrowthChamberBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_growth_chamber_menu")));

    public static final Supplier<MenuType<IntegratedInterfaceMenu>> INTEGRATED_INTERFACE_MENU = MENU_TYPES.register("integrated_interface_menu",
            () -> MenuTypeBuilder.create(IntegratedInterfaceMenu::new, IntegratedInterfaceHost.class)
                    .build(AE2CrystalScience.makeId("integrated_interface_menu")));

    public static final Supplier<MenuType<IntegratedInterfaceSetStockAmountMenu>> INTEGRATED_INTERFACE_SET_STOCK_AMOUNT_MENU = MENU_TYPES.register("integrated_interface_set_stock_amount_menu",
            () -> MenuTypeBuilder.create(IntegratedInterfaceSetStockAmountMenu::new, IntegratedInterfaceHost.class)
                    .build(AE2CrystalScience.makeId("integrated_interface_set_stock_amount_menu")));

    public static final Supplier<MenuType<CrystalVibrationChamberMenu>> CRYSTAL_VIBRATION_CHAMBER_MENU = MENU_TYPES.register("crystal_vibration_chamber_menu",
            () -> MenuTypeBuilder.create(CrystalVibrationChamberMenu::new, CrystalVibrationChamberBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_vibration_chamber_menu")));

    public static final Supplier<MenuType<CircuitEtcherMenu>> CIRCUIT_ETCHER_MENU = MENU_TYPES.register("circuit_etcher_menu",
            () -> MenuTypeBuilder.create(CircuitEtcherMenu::new, CircuitEtcherBlockEntity.class)
                    .build(AE2CrystalScience.makeId("circuit_etcher_menu")));

    public static final Supplier<MenuType<CrystalPulverizerMenu>> CRYSTAL_PULVERIZER_MENU = MENU_TYPES.register("crystal_pulverizer_menu",
            () -> MenuTypeBuilder.create(CrystalPulverizerMenu::new, CrystalPulverizerBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_pulverizer_menu")));

    public static final Supplier<MenuType<QuartzGrindstoneMenu>> QUARTZ_GRINDSTONE_MENU = MENU_TYPES.register("quartz_grindstone_menu",
            () -> MenuTypeBuilder.create(QuartzGrindstoneMenu::new, QuartzGrindstoneBlockEntity.class)
                    .build(AE2CrystalScience.makeId("quartz_grindstone_menu")));

    public static final Supplier<MenuType<MeteoritePatternProviderMenu>> METEORITE_PATTERN_PROVIDER_MENU = MENU_TYPES.register("meteorite_pattern_provider_menu",
            () -> MenuTypeBuilder.create(MeteoritePatternProviderMenu::new, MeteoritePatternProviderHost.class)
                    .build(AE2CrystalScience.makeId("meteorite_pattern_provider_menu")));

    public static final Supplier<MenuType<PatternProviderMenu>> SIMPLE_PATTERN_PROVIDER_MENU = MENU_TYPES.register("simple_pattern_provider_menu",
            () -> MenuTypeBuilder.create(PatternProviderMenu::new, PatternProviderLogicHost.class)
                    .build(AE2CrystalScience.makeId("simple_pattern_provider_menu")));

    public static final Supplier<MenuType<CrystalAggregatorMenu>> CRYSTAL_AGGREGATOR_MENU = MENU_TYPES.register("crystal_aggregator_menu",
            () -> MenuTypeBuilder.create(CrystalAggregatorMenu::new, CrystalAggregatorBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_aggregator_menu")));

    public static final Supplier<MenuType<EnderBroadcasterMenu>> ENDER_BROADCASTER_MENU = MENU_TYPES.register("ender_broadcaster_menu",
            () -> MenuTypeBuilder.create(EnderBroadcasterMenu::new, EnderBroadcasterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("ender_broadcaster_menu")));

    public static final Supplier<MenuType<FrequencyBandMenu>> FREQUENCY_BAND_MENU = MENU_TYPES.register("frequency_band_menu",
            () -> MenuTypeBuilder.create(FrequencyBandMenu::new, EnderBroadcasterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("frequency_band_menu")));

    public static final Supplier<MenuType<FrequencyBandLinkMenu>> FREQUENCY_BAND_LINK_MENU = MENU_TYPES.register("frequency_band_link_menu",
            () -> MenuTypeBuilder.create(FrequencyBandLinkMenu::new, EnderBroadcasterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("frequency_band_link_menu")));

    public static final Supplier<MenuType<FrequencyBandCreateMenu>> FREQUENCY_BAND_CREATE_MENU = MENU_TYPES.register("frequency_band_create_menu",
            () -> MenuTypeBuilder.create(FrequencyBandCreateMenu::new, EnderBroadcasterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("frequency_band_create_menu")));

    public static final Supplier<MenuType<FrequencyBandManagerMenu>> FREQUENCY_BAND_MANAGER_MENU = MENU_TYPES.register("frequency_band_manager_menu",
            () -> MenuTypeBuilder.create(FrequencyBandManagerMenu::new, EnderBroadcasterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("frequency_band_manager_menu")));

    public static final Supplier<MenuType<BandWhiteListManagerMenu>> BAND_WHITE_LIST_MANAGER_MENU = MENU_TYPES.register("band_white_list_manager_menu",
            () -> MenuTypeBuilder.create(BandWhiteListManagerMenu::new, EnderBroadcasterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("band_white_list_manager_menu")));

    public static final Supplier<MenuType<EnderEmitterMenu>> ENDER_EMITTER_MENU = MENU_TYPES.register("ender_emitter_menu",
            () -> MenuTypeBuilder.create(EnderEmitterMenu::new, EnderEmitterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("ender_emitter_menu")));

    public static final Supplier<MenuType<EnderEmitterFrequencyBandMenu>> ENDER_EMITTER_FREQUENCY_BAND_MENU = MENU_TYPES.register("ender_emitter_frequency_band_menu",
            () -> MenuTypeBuilder.create(EnderEmitterFrequencyBandMenu::new, EnderEmitterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("ender_emitter_frequency_band_menu")));

    public static final Supplier<MenuType<EnderEmitterFrequencyBandLinkMenu>> ENDER_EMITTER_FREQUENCY_BAND_LINK_MENU = MENU_TYPES.register("ender_emitter_frequency_band_link_menu",
            () -> MenuTypeBuilder.create(EnderEmitterFrequencyBandLinkMenu::new, EnderEmitterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("ender_emitter_frequency_band_link_menu")));

    public static final Supplier<MenuType<EnderInterfaceMenu>> ENDER_INTERFACE_MENU = MENU_TYPES.register("ender_interface_menu",
            () -> MenuTypeBuilder.create(EnderInterfaceMenu::new, EnderInterfaceHost.class)
                    .build(AE2CrystalScience.makeId("ender_interface_menu")));

    public static final Supplier<MenuType<ResonatingPatternProviderMenu>> RESONATING_PATTERN_PROVIDER_MENU = MENU_TYPES.register("resonating_pattern_provider_menu",
            () -> MenuTypeBuilder.create(ResonatingPatternProviderMenu::new, ResonatingPatternProviderHost.class)
                    .build(AE2CrystalScience.makeId("resonating_pattern_provider_menu")));

    public static final Supplier<MenuType<EntropyVariationReactionChamberMenu>> ENTROPY_VARIATION_REACTION_CHAMBER_MENU = MENU_TYPES.register("entropy_variation_reaction_chamber_menu",
            () -> MenuTypeBuilder.create(EntropyVariationReactionChamberMenu::new, EntropyVariationReactionChamberBlockEntity.class)
                    .build(AE2CrystalScience.makeId("entropy_variation_reaction_chamber_menu")));

    public static final Supplier<MenuType<QuartzOscillatorClockMenu>> QUARTZ_OSCILLATOR_CLOCK_MENU = MENU_TYPES.register("quartz_oscillator_clock_menu",
            () -> MenuTypeBuilder.create(QuartzOscillatorClockMenu::new, QuartzOscillatorClockHost.class)
                    .build(AE2CrystalScience.makeId("quartz_oscillator_clock_menu")));

    public static final Supplier<MenuType<SideConfigMenu>> SIDE_CONFIG_MENU = MENU_TYPES.register("side_config_menu",
            () -> MenuTypeBuilder.create(SideConfigMenu::new, CustomReturnableSubMenuHost.class)
                    .build(AE2CrystalScience.makeId("side_config_menu")));

    public static final Supplier<MenuType<ResonatingPatternConverterMenu>> RESONATING_PATTERN_CONVERTER_MENU = MENU_TYPES.register("resonating_pattern_converter_menu",
            () -> MenuTypeBuilder.create(ResonatingPatternConverterMenu::new, ResonatingPatternConverterMenuHost.class)
                    .build(AE2CrystalScience.makeId("resonating_pattern_converter_menu")));

    public static final Supplier<MenuType<ResonantTemplateCodingTermMenu>> RESONANT_TEMPLATE_CODING_TERM_MENU = MENU_TYPES.register("resonant_template_coding_term_menu",
            () -> MenuTypeBuilder.create(ResonantTemplateCodingTermMenu::new, IPatternTerminalMenuHost.class)
                    .build(AE2CrystalScience.makeId("resonant_template_coding_term_menu")));

    public static void registerMenus(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
