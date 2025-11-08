package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.item.AE2CrystalSeedsItems;
import io.github.lounode.ae2cs.common.util.resourcelocation.BlockNames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiConsumer;

import static io.github.lounode.ae2cs.common.util.resourcelocation.ResourceLocationUtil.prefix;

public final class AE2CrystalSeedsBlocks {
    public static final Block crystalGrowthChamber = new CrystalGrowthChamberBlock(copy(Blocks.IRON_BLOCK));
    public static final Block circuitEtcher = new Block(copy(Blocks.IRON_BLOCK));
    public static final Block quartzGrindstone = new Block(copy(Blocks.STONE));
    public static final Block crystalVibrationChamber = new Block(copy(Blocks.IRON_BLOCK));
    public static final Block crusher = new Block(copy(Blocks.IRON_BLOCK));


    public static BlockBehaviour.Properties copy(BlockBehaviour behaviour) {
        return BlockBehaviour.Properties.ofFullCopy(behaviour);
    }

    public static void registerBlocks(BiConsumer<Block, ResourceLocation> r) {
        r.accept(crystalGrowthChamber, prefix(BlockNames.CRYSTAL_GROWTH_CHAMBER));
        r.accept(circuitEtcher, prefix(BlockNames.CIRCUIT_ETCHER));
        r.accept(quartzGrindstone, prefix(BlockNames.QUARTZ_GRINDSTONE));
        r.accept(crystalVibrationChamber, prefix(BlockNames.CRYSTAL_VIBRATION_CHAMBER));
        r.accept(crusher, prefix(BlockNames.CRUSHER));
    }

    public static void registerItemBlocks(BiConsumer<Item, ResourceLocation> r) {
        r.accept(new BlockItem(crystalGrowthChamber, AE2CrystalSeedsItems.defaultBuilder()), BuiltInRegistries.BLOCK.getKey(crystalGrowthChamber));
        r.accept(new BlockItem(circuitEtcher, AE2CrystalSeedsItems.defaultBuilder()), BuiltInRegistries.BLOCK.getKey(circuitEtcher));
        r.accept(new BlockItem(quartzGrindstone, AE2CrystalSeedsItems.defaultBuilder()), BuiltInRegistries.BLOCK.getKey(quartzGrindstone));
        r.accept(new BlockItem(crystalVibrationChamber, AE2CrystalSeedsItems.defaultBuilder()), BuiltInRegistries.BLOCK.getKey(crystalVibrationChamber));
        r.accept(new BlockItem(crusher, AE2CrystalSeedsItems.defaultBuilder()), BuiltInRegistries.BLOCK.getKey(crusher));
    }
}
