package io.github.lounode.ae2cs.common.init.client;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.implementations.PatternProviderMenu;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.client.gui.*;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.MeteoritePatternProviderMenu;
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
        event.<MeteoritePatternProviderMenu, MeteoritePatternProviderGUI>register(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(),
                (menu, id, inv) -> new MeteoritePatternProviderGUI(menu, id, inv, StyleManager.loadStyleDoc("/screens/meteorite_pattern_provider_menu.json")));
        event.<PatternProviderMenu, PatternProviderScreen<PatternProviderMenu>>register(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(),
                (menu, id, inv) -> new PatternProviderScreen<>(menu, id, inv, StyleManager.loadStyleDoc("/screens/simple_pattern_provider_menu.json")));
    }
}
