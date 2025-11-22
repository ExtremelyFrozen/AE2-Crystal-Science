package io.github.lounode.ae2cs.common.init.client;

import appeng.client.gui.me.crafting.SetStockAmountScreen;
import appeng.menu.implementations.SetStockAmountMenu;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.client.gui.CrystalGrowthChamberGUI;
import io.github.lounode.ae2cs.client.gui.IntegratedInterfaceGUI;
import io.github.lounode.ae2cs.client.gui.IntegratedInterfaceSetStockAmountGUI;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.IntegratedInterfaceSetStockAmountMenu;
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
    }
}
