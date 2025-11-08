package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.core.MainCreativeTab;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import com.google.common.base.Preconditions;
import io.github.lounode.ae2cs.api.AE2CrystalSeedsAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AE2CSBlocks {
    public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks(AE2CrystalSeedsAPI.MOD_ID);
    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();
    //public static final BlockDefinition<CrystalVibrationChamberBlock> CRYSTAL_VIBRATION_CHAMBER = block("Crystal Vibration Chamber", prefix(BlockNames.CRYSTAL_VIBRATION_CHAMBER), VibrationChamberBlock::new);

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    private static <T extends Block> BlockDefinition<T> block(String englishName, ResourceLocation id,
                                                              Supplier<T> blockSupplier) {
        return block(englishName, id, blockSupplier, null);
    }

    private static <T extends Block> BlockDefinition<T> block(
            String englishName,
            ResourceLocation id,
            Supplier<T> blockSupplier,
            @Nullable BiFunction<Block, Item.Properties, BlockItem> itemFactory) {
        Preconditions.checkArgument(id.getNamespace().equals(AE2CrystalSeedsAPI.MOD_ID));

        // Create block and matching item
        var deferredBlock = DR.register(id.getPath(), blockSupplier);
        var deferredItem = AEItems.DR.register(id.getPath(), () -> {
            var block = deferredBlock.get();
            var itemProperties = new Item.Properties();
            if (itemFactory != null) {
                var item = itemFactory.apply(block, itemProperties);
                if (item == null) {
                    throw new IllegalArgumentException("BlockItem factory for " + id + " returned null");
                }
                return item;
            } else if (block instanceof AEBaseBlock) {
                return new AEBaseBlockItem(block, itemProperties);
            } else {
                return new BlockItem(block, itemProperties);
            }
        });

        var itemDef = new ItemDefinition<>(englishName, deferredItem);
        MainCreativeTab.add(itemDef);
        BlockDefinition<T> definition = new BlockDefinition<>(englishName, deferredBlock, itemDef);

        BLOCKS.add(definition);

        return definition;

    }
}
