package io.github.lounode.ae2cs.common.me;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.neoforged.bus.api.IEventBus;

public class AEPlugin
{
    /**
     * 在mod入口点调用
     */
    public static void onInit()
    {

    }

    /**
     * init后立刻运行此段代码，在这里进行注册相关内容
     */
    public static void onRegister(IEventBus modEventBus, IEventBus gameEventBus)
    {

    }

    /**
     * 在FMLCommonSetupEvent阶段调用
     */
    public static void onCommonSetup()
    {
        Upgrades.add(AEItems.SPEED_CARD, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK, 4);
    }
}
