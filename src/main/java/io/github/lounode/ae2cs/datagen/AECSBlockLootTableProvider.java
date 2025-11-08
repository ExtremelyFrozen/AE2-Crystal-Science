package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AECSBlockLootTableProvider extends BlockLootSubProvider
{
    protected AECSBlockLootTableProvider(HolderLookup.Provider registries)
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate()
    {
        for (DeferredBlock<? extends Block> block : AECSBlocks.getALL())
        {
            dropSelf(block.get());
        }
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks()
    {
        return AECSBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
