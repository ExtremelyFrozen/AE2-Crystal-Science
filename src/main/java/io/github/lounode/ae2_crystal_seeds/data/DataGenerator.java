package io.github.lounode.ae2_crystal_seeds.data;

import io.github.lounode.ae2_crystal_seeds.api.AE2CrystalSeedsAPI;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = AE2CrystalSeedsAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerator {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent dataEvent) {
        var pack = dataEvent.getGenerator().getVanillaPack(true);
        var file = dataEvent.getExistingFileHelper();
        var lookup = dataEvent.getLookupProvider();

        pack.addProvider(output -> new AE2CrystalSeedsBlockStateProvider(output, file));
        pack.addProvider(output -> new AE2CrystalSeedsItemModelProvider(output, file));
    }
}
