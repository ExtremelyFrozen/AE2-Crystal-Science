package io.github.lounode.ae2_crystal_seeds.client;

import io.github.lounode.ae2_crystal_seeds.AE2CrystalSeeds;
import io.github.lounode.ae2_crystal_seeds.api.AE2CrystalSeedsAPI;
import io.github.lounode.ae2_crystal_seeds.common.item.CrystalSeedItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AE2CrystalSeedsAPI.MOD_ID, dist = Dist.CLIENT)
public class AE2CrystalSeedsClient {
    public AE2CrystalSeedsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        var modbus = container.getEventBus();

        modbus.register(CrystalSeedItem.ClientEventHandler.class);
    }
}