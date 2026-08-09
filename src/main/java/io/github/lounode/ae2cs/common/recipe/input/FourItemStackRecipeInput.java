package io.github.lounode.ae2cs.common.recipe.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import org.jetbrains.annotations.NotNull;

/** Input view used by machines with four interchangeable ingredient slots. */
public class FourItemStackRecipeInput implements RecipeInput {

    private final ItemStack[] items;

    private FourItemStackRecipeInput(ItemStack a, ItemStack b, ItemStack c, ItemStack d) {
        this.items = new ItemStack[] { a, b, c, d };
    }

    public static FourItemStackRecipeInput of(ItemStack a, ItemStack b, ItemStack c, ItemStack d) {
        return new FourItemStackRecipeInput(a, b, c, d);
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        if (index < 0 || index >= items.length) {
            throw new IllegalArgumentException("Unexpected input slot: " + index);
        }
        return items[index];
    }

    @Override
    public int size() {
        return items.length;
    }
}
