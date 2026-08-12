package io.github.lounode.ae2cs.common.recipe.pulse_centrifuge;

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

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PulseCentrifugeRecipe implements Recipe<SingleRecipeInput> {

    private final SizedIngredient input;
    private final List<ItemStack> results;
    private final FluidStack fluidOutput;
    private final int energyCost;

    public PulseCentrifugeRecipe(SizedIngredient input, List<ItemStack> results, int energyCost) {
        this(input, results, FluidStack.EMPTY, energyCost);
    }

    public PulseCentrifugeRecipe(SizedIngredient input, List<ItemStack> results, FluidStack fluidOutput, int energyCost) {
        if (input.ingredient().isEmpty() || input.count() <= 0) {
            throw new IllegalArgumentException("Pulse centrifuge input cannot be empty");
        }
        if (results.isEmpty() || results.size() > 4 || results.stream().anyMatch(ItemStack::isEmpty)) {
            throw new IllegalArgumentException("Pulse centrifuge recipes require 1-4 non-empty results");
        }
        if (energyCost <= 0) {
            throw new IllegalArgumentException("Energy cost must be positive");
        }

        this.input = input;
        this.results = results.stream().map(ItemStack::copy).toList();
        this.fluidOutput = fluidOutput == null ? FluidStack.EMPTY : fluidOutput.copy();
        this.energyCost = energyCost;
    }

    public SizedIngredient input() {
        return input;
    }

    public List<ItemStack> results() {
        return results.stream().map(ItemStack::copy).toList();
    }

    public FluidStack fluidOutput() {
        return fluidOutput.copy();
    }

    public int energyCost() {
        return energyCost;
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return this.input.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input,
                                       HolderLookup.@NotNull Provider registries) {
        return results.getFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return results.getFirst().copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AECSRecipeSerializers.PULSE_CENTRIFUGE.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return AECSRecipeTypes.PULSE_CENTRIFUGE.get();
    }
}
