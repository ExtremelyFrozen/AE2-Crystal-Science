package io.github.lounode.ae2cs.common.init.client;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.implementations.PatternProviderMenu;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.client.gui.*;
import io.github.lounode.ae2cs.client.gui.linker.broadcast.*;
import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.*;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class AECSScreens
{
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(AECSMenus.CRYSTAL_GROWTH_CHAMBER_MENU.get(), CrystalGrowthChamberGUI::new);
        event.register(AECSMenus.INTEGRATED_INTERFACE_MENU.get(), IntegratedInterfaceGUI::new);
        event.register(AECSMenus.INTEGRATED_INTERFACE_SET_STOCK_AMOUNT_MENU.get(), IntegratedInterfaceSetStockAmountGUI::new);
        event.register(AECSMenus.CRYSTAL_VIBRATION_CHAMBER_MENU.get(), CrystalVibrationChamberGUI::new);
        event.register(AECSMenus.CIRCUIT_ETCHER_MENU.get(), CircuitEtcherGUI::new);
        event.register(AECSMenus.CRYSTAL_PULVERIZER_MENU.get(), CrystalPulverizerGUI::new);
        event.register(AECSMenus.QUARTZ_GRINDSTONE_MENU.get(), QuartzGrindstoneGUI::new);
        event.register(AECSMenus.CRYSTAL_AGGREGATOR_MENU.get(), CrystalAggregatorGUI::new);
        event.<MeteoritePatternProviderMenu, MeteoritePatternProviderGUI>register(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(),
                (menu, id, inv) -> new MeteoritePatternProviderGUI(menu, id, inv, StyleManager.loadStyleDoc("/screens/meteorite_pattern_provider_menu.json")));
        event.<PatternProviderMenu, PatternProviderScreen<PatternProviderMenu>>register(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(),
                (menu, id, inv) -> new PatternProviderScreen<>(menu, id, inv, StyleManager.loadStyleDoc("/screens/simple_pattern_provider_menu.json")));
        event.register(AECSMenus.ENDER_BROADCASTER_MENU.get(), EnderBroadcasterGUI::new);
        event.register(AECSMenus.FREQUENCY_BAND_MENU.get(), FrequencyBandGUI::new);
        event.register(AECSMenus.FREQUENCY_BAND_LINK_MENU.get(), FrequencyBandLinkGUI::new);
        event.register(AECSMenus.FREQUENCY_BAND_CREATE_MENU.get(), FrequencyBandCreateGUI::new);
        event.register(AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get(), io.github.lounode.ae2cs.client.gui.linker.broadcast.FrequencyBandManagerGUI::new);
        event.register(AECSMenus.BAND_WHITE_LIST_MANAGER_MENU.get(), BandWhiteListManagerGUI::new);

        event.<EnderEmitterMenu, EnderEmitterGUI>register(AECSMenus.ENDER_EMITTER_MENU.get(),
                (menu, inv, title) -> new EnderEmitterGUI(menu, inv, title,
                        StyleManager.loadStyleDoc("/screens/ender_emitter_menu.json")));

        event.<EnderInterfaceMenu, EnderInterfaceGUI>register(AECSMenus.ENDER_INTERFACE_MENU.get(),
                (menu, inv, title) -> new EnderInterfaceGUI(menu, inv, title,
                        StyleManager.loadStyleDoc("/screens/ender_interface_menu.json")));

        event.<ResonatingPatternProviderMenu, ResonatingPatternProviderGUI>register(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(),
                (menu, inv, title) -> new ResonatingPatternProviderGUI(menu, inv, title,
                        StyleManager.loadStyleDoc("/screens/resonating_pattern_provider.json")));

        event.<EntropyVariationReactionChamberMenu, EntropyVariationReactionChamberGUI>register(AECSMenus.ENTROPY_VARIATION_REACTION_CHAMBER_MENU.get(),
                (menu, inv, title) -> new EntropyVariationReactionChamberGUI(menu, inv, title,
                        StyleManager.loadStyleDoc("/screens/entropy_variation_reaction_chamber_menu.json")));

        event.<QuartzOscillatorClockMenu, QuartzOscillatorClockGUI>register(AECSMenus.QUARTZ_OSCILLATOR_CLOCK_MENU.get(),
                (menu, inv, title) -> new QuartzOscillatorClockGUI(menu, inv, title,
                        StyleManager.loadStyleDoc("/screens/quartz_oscillator_clock_menu.json")));

        event.<SideConfigMenu, SideConfigGUI>register(AECSMenus.SIDE_CONFIG_MENU.get(),
                (menu, inv, title) -> new SideConfigGUI(menu, inv, title,
                        StyleManager.loadStyleDoc("/screens/side_config_menu.json")));
    }
}
