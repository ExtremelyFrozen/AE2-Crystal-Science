package io.github.lounode.ae2cs.core;

import appeng.api.ids.AECreativeTabIds;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.core.definitions.ItemDefinition;
import appeng.items.AEBaseItem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.lounode.ae2cs.api.ids.AE2CSCreativeTabIds;
import io.github.lounode.ae2cs.common.item.AE2CSItems;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.ArrayList;
import java.util.List;

public final class AE2CSMainCreativeTab {

    private static final Multimap<ResourceKey<CreativeModeTab>, ItemDefinition<?>> externalItemDefs = HashMultimap
            .create();
    private static final List<ItemDefinition<?>> itemDefs = new ArrayList<>();

    public static void init(Registry<CreativeModeTab> registry) {
        var tab = CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.ae2_crystal_seeds"))
                .icon(() -> AE2CSItems.PURE_CERTUS_QUARTZ_CRYSTAL.stack(1))
                .displayItems(AE2CSMainCreativeTab::buildDisplayItems)
                .build();
        Registry.register(registry, AE2CSCreativeTabIds.MAIN, tab);
    }

    public static void initExternal(BuildCreativeModeTabContentsEvent contents) {
        for (var itemDefinition : externalItemDefs.get(contents.getTabKey())) {
            contents.accept(itemDefinition);
        }
    }

    public static void add(ItemDefinition<?> itemDef) {
        itemDefs.add(itemDef);
    }

    public static void addExternal(ResourceKey<CreativeModeTab> tab, ItemDefinition<?> itemDef) {
        externalItemDefs.put(tab, itemDef);
    }

    private static void buildDisplayItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                                          CreativeModeTab.Output output) {
        for (var itemDef : itemDefs) {
            var item = itemDef.asItem();

            // For block items, the block controls the creative tab
            if (item instanceof AEBaseBlockItem baseItem
                    && baseItem.getBlock() instanceof AEBaseBlock baseBlock) {
                baseBlock.addToMainCreativeTab(itemDisplayParameters, output);
            } else if (item instanceof AEBaseItem baseItem) {
                baseItem.addToMainCreativeTab(itemDisplayParameters, output);
            } else {
                output.accept(itemDef);
            }
        }
    }
}
