package io.github.lounode.ae2cs.common.init.client;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.client.gui.CrystalGrowthChamberGUI;
import io.github.lounode.ae2cs.client.gui.IntegratedInterfaceGUI;
import io.github.lounode.ae2cs.common.init.AECSMenus;
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
    }
}
