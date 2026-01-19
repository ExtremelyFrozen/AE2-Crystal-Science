package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lounode.ae2cs.AE2CrystalScience.makeId;

public class AE2CrystalScienceClient
{
    public static void clientInit()
    {
    }

    // TODO 这里，我不太确定1.20.1的注册表结构是应该使用内建表还是Forge表
    // TODO 后续自行修改即可
    public static void clientCommonSetup()
    {
        Set<CrystalSeedItem> seeds = BuiltInRegistries.ITEM.stream()
                .filter(i -> AECSConstants.MODID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
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
