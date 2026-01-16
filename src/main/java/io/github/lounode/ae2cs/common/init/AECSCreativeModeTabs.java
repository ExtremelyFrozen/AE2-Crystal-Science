package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AECSCreativeModeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AECSConstants.MODID);

    public static final Supplier<CreativeModeTab> AE2CS_CREATIVE_TAB = CREATIVE_MODE_TAB.register(
            "ae2cs_creative_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(AECSItems.PURE_CERTUS_QUARTZ_CRYSTAL.get()))
                    .title(Component.translatable("creativetab.ae2cs.items"))
                    .displayItems((params, output) -> {
                        for (DeferredItem<? extends Item> ro : AECSItems.getALL())
                        {
                            output.accept(ro.get());
                        }
                        for (DeferredItem<? extends Item> ro : AECSParts.getAll())
                        {
                            output.accept(ro.get());
                        }
                        for (DeferredBlock<? extends Block> ro : AECSBlocks.getALL())
                        {
                            output.accept(ro.get());
                        }
                    })
                    .build()
    );

    public static void register(IEventBus eventBus)
    {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
