package io.github.lounode.ae2cs.common.recipe.input;

import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ThreeItemStackRecipeInput implements Container, Clearable {

    private final ItemStack[] items = new ItemStack[3];

    private ThreeItemStackRecipeInput(ItemStack a, ItemStack b, ItemStack c) {
        this.items[0] = a == null ? ItemStack.EMPTY : a;
        this.items[1] = b == null ? ItemStack.EMPTY : b;
        this.items[2] = c == null ? ItemStack.EMPTY : c;
    }

    public static ThreeItemStackRecipeInput of(ItemStack a, ItemStack b, ItemStack c) {
        return new ThreeItemStackRecipeInput(a, b, c);
    }

    public ItemStack getInputA() {
        return items[0];
    }

    public ItemStack getInputB() {
        return items[1];
    }

    public ItemStack getInputC() {
        return items[2];
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return items[0].isEmpty() && items[1].isEmpty() && items[2].isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        if (index < 0 || index >= 3) {
            return ItemStack.EMPTY;
        }
        return items[index];
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        if (count <= 0) return ItemStack.EMPTY;

        ItemStack stack = getItem(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        int removed = Math.min(count, stack.getCount());
        ItemStack result = stack.split(removed);

        if (stack.isEmpty()) {
            items[index] = ItemStack.EMPTY;
        }

        setChanged();
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        if (index < 0 || index >= 3) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = items[index];
        items[index] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        if (index < 0 || index >= 3) {
            return;
        }

        items[index] = stack;
        setChanged();
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items[0] = ItemStack.EMPTY;
        items[1] = ItemStack.EMPTY;
        items[2] = ItemStack.EMPTY;
        setChanged();
    }
}
