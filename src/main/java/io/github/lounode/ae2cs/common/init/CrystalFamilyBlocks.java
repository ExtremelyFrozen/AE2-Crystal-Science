package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.common.block.CrystalMotherRockBlock;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Groups every locally registered block in one crystal family with the item produced by its final stage.
 */
public record CrystalFamilyBlocks(
                                  String materialId,
                                  DeferredBlock<CrystalMotherRockBlock> motherRock,
                                  List<DeferredBlock<? extends Block>> stages,
                                  Supplier<? extends ItemLike> crystalDrop) {

    public CrystalFamilyBlocks {
        Objects.requireNonNull(materialId, "materialId");
        Objects.requireNonNull(motherRock, "motherRock");
        Objects.requireNonNull(stages, "stages");
        Objects.requireNonNull(crystalDrop, "crystalDrop");
        if (stages.size() < 4 || stages.size() > 5) {
            throw new IllegalArgumentException("A local crystal family must contain four or five stages: " + materialId);
        }
        stages = List.copyOf(stages);
    }

    public DeferredBlock<? extends Block> finalStage() {
        return stages.getLast();
    }
}
