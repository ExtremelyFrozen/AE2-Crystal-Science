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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.NotNull;

public class CrystalPulverizerRecipe implements Recipe<SingleRecipeInput> {

    private final SizedIngredient input;
    private final ItemStack result;
    private final SizedFluidIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final int energyCost;

    public CrystalPulverizerRecipe(SizedIngredient input, ItemStack result, int energyCost) {
        this(input, result, null, FluidStack.EMPTY, energyCost);
    }

    public CrystalPulverizerRecipe(SizedIngredient input, ItemStack result, SizedFluidIngredient fluidInput,
                                   FluidStack fluidOutput, int energyCost) {
        if (input.ingredient().isEmpty() || input.count() <= 0) {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        if (energyCost <= 0) {
            throw new IllegalArgumentException("Time must be positive");
        }

        this.input = input;
        this.result = result;
        this.fluidInput = fluidInput;
        this.fluidOutput = fluidOutput == null ? FluidStack.EMPTY : fluidOutput.copy();
        this.energyCost = energyCost;
    }

    public SizedIngredient input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    public SizedFluidIngredient fluidInput() {
        return fluidInput;
    }

    public FluidStack fluidOutput() {
        return fluidOutput.copy();
    }

    public boolean matchesFluid(FluidStack fluid) {
        return fluidInput == null || fluidInput.test(fluid);
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
