package io.github.lounode.ae2cs.api.util;

import appeng.core.definitions.BlockDefinition;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BlockDefinitionSupplier implements Supplier<Block>
{
    private final BlockDefinition<? extends Block> blockDefinition;

    private BlockDefinitionSupplier(@NotNull BlockDefinition<? extends Block> blockDefinition)
    {
        this.blockDefinition = blockDefinition;
    }

    public static BlockDefinitionSupplier of(@NotNull BlockDefinition<? extends Block> blockDefinition)
    {
        return new BlockDefinitionSupplier(blockDefinition);
    }

    @Override
    public Block get()
    {
        return blockDefinition.block();
    }
}
