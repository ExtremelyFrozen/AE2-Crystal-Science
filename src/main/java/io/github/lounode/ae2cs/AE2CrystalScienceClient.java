package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lounode.ae2cs.AE2CrystalScience.makeId;

public class AE2CrystalScienceClient
{
    public static void clientInit()
    {
    }

    public static void clientCommonSetup()
    {
        Set<CrystalSeedItem> seeds = ForgeRegistries.ITEMS.getValues().stream()
                .filter(i -> AECSConstants.MODID.equals(ForgeRegistries.ITEMS.getKey(i).getNamespace()))
                .filter(i -> i instanceof CrystalSeedItem)
                .map(item -> (CrystalSeedItem) item)
                .collect(Collectors.toSet());

        for (var seed : seeds)
        {
            ItemProperties.register(seed, makeId("age"), (stack, level, player, s) -> {
                return seed.getGrowProcess(stack);
            });
        }
    }

    public static void clientRegister(IEventBus modBus, IEventBus gameBus)
    {

    }
}
