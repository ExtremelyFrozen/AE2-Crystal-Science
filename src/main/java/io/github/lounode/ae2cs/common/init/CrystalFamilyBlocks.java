package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.common.block.CrystalMotherRockBlock;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.Objects;

/**
 * Groups locally registered crystal stages and their bud drops. Final-stage loot tables are handwritten resources.
 */
public record CrystalFamilyBlocks(
                                  String materialId,
                                  DeferredBlock<CrystalMotherRockBlock> motherRock,
                                  List<DeferredBlock<? extends Block>> stages,
                                  TagKey<Item> budDrop) {

    public CrystalFamilyBlocks {
        Objects.requireNonNull(materialId, "materialId");
        Objects.requireNonNull(motherRock, "motherRock");
        Objects.requireNonNull(stages, "stages");
        Objects.requireNonNull(budDrop, "budDrop");
        if (stages.size() < 4 || stages.size() > 5) {
            throw new IllegalArgumentException("A local crystal family must contain four or five stages: " + materialId);
        }
        stages = List.copyOf(stages);
    }

    public DeferredBlock<? extends Block> finalStage() {
        return stages.getLast();
    }
}
