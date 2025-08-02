package io.github.lounode.ae2_crystal_seeds.common.block;

import io.github.lounode.ae2_crystal_seeds.common.item.AE2CrystalSeedsItems;
import io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.BlockNames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiConsumer;

import static io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.ResourceLocationUtil.prefix;

public final class AE2CrystalSeedsBlocks {
    public static final Block crystalGrowthChamber = new CrystalGrowthChamberBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    );

    public static void registerBlocks(BiConsumer<Block, ResourceLocation> r) {
        r.accept(crystalGrowthChamber, prefix(BlockNames.CRYSTAL_GROWTH_CHAMBER));
    }

    public static void registerItemBlocks(BiConsumer<Item, ResourceLocation> r) {
        r.accept(new BlockItem(crystalGrowthChamber, AE2CrystalSeedsItems.defaultBuilder()), BuiltInRegistries.BLOCK.getKey(crystalGrowthChamber));
    }
}
