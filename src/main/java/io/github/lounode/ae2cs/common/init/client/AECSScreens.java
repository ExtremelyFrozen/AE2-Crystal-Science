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
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class AECSScreens
{
    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            MenuScreens.register(AECSMenus.CRYSTAL_GROWTH_CHAMBER_MENU.get(), CrystalGrowthChamberGUI::new);

            MenuScreens.<IntegratedInterfaceMenu, IntegratedInterfaceGUI>register(AECSMenus.INTEGRATED_INTERFACE_MENU.get(), (menu, inv, title) -> new IntegratedInterfaceGUI(menu, inv, title,
                    menu.extended ?
                            StyleManager.loadStyleDoc("/screens/extended_integrated_interface_menu.json") :
                            StyleManager.loadStyleDoc("/screens/integrated_interface_menu.json")));

            MenuScreens.register(AECSMenus.INTEGRATED_INTERFACE_SET_STOCK_AMOUNT_MENU.get(), IntegratedInterfaceSetStockAmountGUI::new);
            MenuScreens.register(AECSMenus.CRYSTAL_VIBRATION_CHAMBER_MENU.get(), CrystalVibrationChamberGUI::new);
            MenuScreens.register(AECSMenus.CIRCUIT_ETCHER_MENU.get(), CircuitEtcherGUI::new);
            MenuScreens.register(AECSMenus.CRYSTAL_PULVERIZER_MENU.get(), CrystalPulverizerGUI::new);
            MenuScreens.register(AECSMenus.QUARTZ_GRINDSTONE_MENU.get(), QuartzGrindstoneGUI::new);
            MenuScreens.register(AECSMenus.CRYSTAL_AGGREGATOR_MENU.get(), CrystalAggregatorGUI::new);
            MenuScreens.<MeteoritePatternProviderMenu, MeteoritePatternProviderGUI>register(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(),
                    (menu, id, inv) -> new MeteoritePatternProviderGUI(menu, id, inv, StyleManager.loadStyleDoc("/screens/meteorite_pattern_provider_menu.json")));
            MenuScreens.<PatternProviderMenu, PatternProviderScreen<PatternProviderMenu>>register(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(),
                    (menu, id, inv) -> new PatternProviderScreen<>(menu, id, inv, StyleManager.loadStyleDoc("/screens/simple_pattern_provider_menu.json")));
            MenuScreens.register(AECSMenus.ENDER_BROADCASTER_MENU.get(), EnderBroadcasterGUI::new);
            MenuScreens.register(AECSMenus.FREQUENCY_BAND_MENU.get(), FrequencyBandGUI::new);
            MenuScreens.register(AECSMenus.FREQUENCY_BAND_LINK_MENU.get(), FrequencyBandLinkGUI::new);
            MenuScreens.register(AECSMenus.FREQUENCY_BAND_CREATE_MENU.get(), FrequencyBandCreateGUI::new);
            MenuScreens.register(AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get(), io.github.lounode.ae2cs.client.gui.linker.broadcast.FrequencyBandManagerGUI::new);
            MenuScreens.register(AECSMenus.BAND_WHITE_LIST_MANAGER_MENU.get(), BandWhiteListManagerGUI::new);

            MenuScreens.<EnderEmitterMenu, EnderEmitterGUI>register(AECSMenus.ENDER_EMITTER_MENU.get(),
                    (menu, inv, title) -> new EnderEmitterGUI(menu, inv, title,
                            StyleManager.loadStyleDoc("/screens/ender_emitter_menu.json")));

            MenuScreens.<EnderInterfaceMenu, EnderInterfaceGUI>register(AECSMenus.ENDER_INTERFACE_MENU.get(),
                    (menu, inv, title) -> new EnderInterfaceGUI(menu, inv, title,
                            menu.extended ?
                                    StyleManager.loadStyleDoc("/screens/extended_ender_interface_menu.json") :
                                    StyleManager.loadStyleDoc("/screens/ender_interface_menu.json")));

            MenuScreens.<ResonatingPatternProviderMenu, ResonatingPatternProviderGUI>register(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(),
                    (menu, inv, title) -> new ResonatingPatternProviderGUI(menu, inv, title,
                            menu.extended ?
                                    StyleManager.loadStyleDoc("/screens/extended_resonating_pattern_provider_menu.json") :
                                    StyleManager.loadStyleDoc("/screens/resonating_pattern_provider_menu.json")));

            MenuScreens.<EntropyVariationReactionChamberMenu, EntropyVariationReactionChamberGUI>register(AECSMenus.ENTROPY_VARIATION_REACTION_CHAMBER_MENU.get(),
                    (menu, inv, title) -> new EntropyVariationReactionChamberGUI(menu, inv, title,
                            StyleManager.loadStyleDoc("/screens/entropy_variation_reaction_chamber_menu.json")));

            MenuScreens.<QuartzOscillatorClockMenu, QuartzOscillatorClockGUI>register(AECSMenus.QUARTZ_OSCILLATOR_CLOCK_MENU.get(),
                    (menu, inv, title) -> new QuartzOscillatorClockGUI(menu, inv, title,
                            StyleManager.loadStyleDoc("/screens/quartz_oscillator_clock_menu.json")));

            MenuScreens.<SideConfigMenu, SideConfigGUI>register(AECSMenus.SIDE_CONFIG_MENU.get(),
                    (menu, inv, title) -> new SideConfigGUI(menu, inv, title,
                            StyleManager.loadStyleDoc("/screens/side_config_menu.json")));

            MenuScreens.<ResonatingPatternConverterMenu, ResonatingPatternConverterGUI>register(AECSMenus.RESONATING_PATTERN_CONVERTER_MENU.get(),
                    (menu, inv, title) -> new ResonatingPatternConverterGUI(menu, inv, title,
                            StyleManager.loadStyleDoc("/screens/resonating_pattern_converter_menu.json")));
        });
    }
}
