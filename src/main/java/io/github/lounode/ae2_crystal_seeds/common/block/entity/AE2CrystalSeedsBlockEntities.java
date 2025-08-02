package io.github.lounode.ae2_crystal_seeds.common.block.entity;

import com.mojang.datafixers.types.Type;
import io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.BlockNames;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static io.github.lounode.ae2_crystal_seeds.common.block.AE2CrystalSeedsBlocks.crystalGrowthChamber;
import static io.github.lounode.ae2_crystal_seeds.common.util.resourcelocation.ResourceLocationUtil.prefix;

public final class AE2CrystalSeedsBlockEntities {
    private static final Map<ResourceLocation, BlockEntityType<?>> ALL = new HashMap<>();

    public static final BlockEntityType<CrystalGrowthChamberBlockEntity> CRYSTAL_GROWTH_CHAMBER =
            type(prefix(BlockNames.CRYSTAL_GROWTH_CHAMBER), CrystalGrowthChamberBlockEntity::new, crystalGrowthChamber);

    private static <T extends BlockEntity> BlockEntityType<T> type(ResourceLocation id, BiFunction<BlockPos, BlockState, T> func, Block... blocks) {
        var ret = BlockEntityType.Builder.of(func::apply, blocks).build(null);
        var old = ALL.put(id, ret);
        if (old != null) {
            throw new IllegalArgumentException("Duplicate id " + id);
        }
        return ret;
    }

    public static void registerTiles(BiConsumer<BlockEntityType<?>, ResourceLocation> r) {
        for (var e : ALL.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }
}
