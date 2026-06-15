package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lounode.ae2cs.AE2CrystalScience.makeId;

@Mod(value = AECSConstants.MODID, dist = Dist.CLIENT)
public class AE2CrystalScienceClient {

    public AE2CrystalScienceClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.register(this);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Set<CrystalSeedItem> seeds = BuiltInRegistries.ITEM.stream()
                    .filter(i -> AECSConstants.MODID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
                    .filter(i -> i instanceof CrystalSeedItem)
                    .map(item -> (CrystalSeedItem) item)
                    .collect(Collectors.toSet());

            for (var seed : seeds) {
                ItemProperties.register(seed, makeId("age"), (stack, level, player, s) -> seed.getGrowProcess(stack));
            }
        });
    }
}
