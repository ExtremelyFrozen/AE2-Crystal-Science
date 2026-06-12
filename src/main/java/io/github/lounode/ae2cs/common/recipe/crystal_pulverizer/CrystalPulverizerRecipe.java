package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import org.jetbrains.annotations.NotNull;

public class CrystalPulverizerRecipe implements Recipe<SingleRecipeInput> {

    private final SizedIngredient input;
    private final ItemStack result;
    private final int energyCost;

    public CrystalPulverizerRecipe(SizedIngredient input, ItemStack result, int energyCost) {
        if (input.ingredient().isEmpty() || input.count() <= 0) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        if (energyCost <= 0) {
            throw new IllegalArgumentException("Time must be positive");
        }

        this.input = input;
        this.result = result;
        this.energyCost = energyCost;
    }

    public SizedIngredient input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    public int energyCost() {
        return energyCost;
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput singleRecipeInput, @NotNull Level level) {
        return input.test(singleRecipeInput.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput singleRecipeInput, HolderLookup.@NotNull Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AECSRecipeSerializers.CRYSTAL_PULVERIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return AECSRecipeTypes.CRYSTAL_PULVERIZER.get();
    }
}
