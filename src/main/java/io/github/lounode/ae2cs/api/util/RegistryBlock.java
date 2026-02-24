package io.github.lounode.ae2cs.api.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RegistryBlock<T extends Block> implements Supplier<T>, ItemLike
{
    private final RegistryObject<T> block;

    public RegistryBlock(RegistryObject<T> block)
    {
        this.block = block;
    }

    @Override
    public T get()
    {
        return this.block.get();
    }

    @Override
    public @NotNull Item asItem()
    {
        return this.block.get().asItem();
    }

    public ItemStack toStack(int count)
    {
        ItemStack stack = this.asItem().getDefaultInstance();
        if (stack.isEmpty())
        {
            throw new IllegalStateException("Block does not have a corresponding item: " + this.block.getId());
        }
        else
        {
            stack.setCount(count);
            return stack;
        }
    }

    public ItemStack toStack()
    {
        return this.toStack(1);
    }
}
