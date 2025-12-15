package io.github.lounode.ae2cs.common.recipe.circuit_etcher;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public class CircuitEtcherRecipeInput implements RecipeInput
{
    private final ItemStack a;
    private final ItemStack b;
    private final ItemStack c;

    private CircuitEtcherRecipeInput(ItemStack a, ItemStack b, ItemStack c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static CircuitEtcherRecipeInput of(ItemStack a, ItemStack b, ItemStack c)
    {
        return new CircuitEtcherRecipeInput(a, b, c);
    }

    public ItemStack getInputA()
    {
        return a;
    }

    public ItemStack getInputB()
    {
        return b;
    }

    public ItemStack getInputC()
    {
        return c;
    }

    @Override
    public @NotNull ItemStack getItem(int i)
    {
        return switch (i)
        {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            default -> throw new IllegalArgumentException("Unexpected value: " + i);
        };
    }

    @Override
    public int size()
    {
        return 3;
    }
}
