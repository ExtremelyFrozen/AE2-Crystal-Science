package io.github.lounode.ae2cs.common.recipe.input;

import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SingleItemStackRecipeInput implements Container, Clearable
{
    private ItemStack item;

    private SingleItemStackRecipeInput(ItemStack item)
    {
        this.item = item == null ? ItemStack.EMPTY : item;
    }

    public static SingleItemStackRecipeInput of(ItemStack item)
    {
        return new SingleItemStackRecipeInput(item);
    }

    public ItemStack item()
    {
        return item;
    }

    @Override
    public int getContainerSize()
    {
        return 1;
    }

    @Override
    public boolean isEmpty()
    {
        return item.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int index)
    {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count)
    {
        if (index != 0 || count <= 0 || item.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        int removed = Math.min(count, item.getCount());
        ItemStack result = item.split(removed);
        if (item.isEmpty())
        {
            item = ItemStack.EMPTY;
        }
        setChanged();
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index)
    {
        if (index != 0)
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = item;
        item = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack)
    {
        if (index != 0)
        {
            return;
        }

        item = stack;
        setChanged();
    }

    @Override
    public void setChanged()
    {
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return true;
    }

    @Override
    public void clearContent()
    {
        item = ItemStack.EMPTY;
        setChanged();
    }
}
