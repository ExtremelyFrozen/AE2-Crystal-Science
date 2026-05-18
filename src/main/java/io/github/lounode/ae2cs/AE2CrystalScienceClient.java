package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AECSConstants.MODID, dist = Dist.CLIENT)
public class AE2CrystalScienceClient {
    public AE2CrystalScienceClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.register(this);
    }

}
