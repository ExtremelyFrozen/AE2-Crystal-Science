package io.github.lounode.ae2cs.common.recipe.crystal_infuser;

import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import io.github.lounode.ae2cs.common.recipe.input.FourItemStackRecipeInput;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** A four-slot, unordered recipe used by the crystal infuser. */
public class CrystalInfuserRecipe implements Recipe<FourItemStackRecipeInput> {

    private final SizedIngredient inputA;
    private final SizedIngredient inputB;
    private final SizedIngredient inputC;
    private final SizedIngredient inputD;
    private final ItemStack result;
    private final int energyCost;
    private final List<SizedIngredient> effective;

    public CrystalInfuserRecipe(SizedIngredient inputA, SizedIngredient inputB, SizedIngredient inputC,
                                SizedIngredient inputD, ItemStack result, int energyCost) {
        if (energyCost <= 0) {
            throw new IllegalArgumentException("Energy cost must be positive");
        }
        this.inputA = inputA;
        this.inputB = inputB;
        this.inputC = inputC;
        this.inputD = inputD;
        this.result = result;
        this.energyCost = energyCost;
        this.effective = new ArrayList<>(4);
        addIfRequired(inputA);
        addIfRequired(inputB);
        addIfRequired(inputC);
        addIfRequired(inputD);
        if (effective.isEmpty()) {
            throw new IllegalArgumentException("Crystal infuser recipe needs at least one input");
        }
    }

    private void addIfRequired(SizedIngredient ingredient) {
        if (ingredient != null && !ingredient.ingredient().isEmpty() && ingredient.count() > 0) {
            effective.add(ingredient);
        }
    }

    public SizedIngredient inputA() {
        return inputA;
    }

    public SizedIngredient inputB() {
        return inputB;
    }

    public SizedIngredient inputC() {
        return inputC;
    }

    public SizedIngredient inputD() {
        return inputD;
    }

    public List<SizedIngredient> required() {
        return effective;
    }

    public ItemStack result() {
        return result;
    }

    public int energyCost() {
        return energyCost;
    }

    /** Returns the machine slot for each required input, or null when no assignment exists. */
    public int[] findMatch(FourItemStackRecipeInput input) {
        if (effective.size() > input.size()) return null;

        int[] match = new int[effective.size()];
        boolean[] used = new boolean[input.size()];
        if (findMatch(input, 0, used, match)) {
            return match;
        }
        return null;
    }

    private boolean findMatch(FourItemStackRecipeInput input, int ingredientIndex, boolean[] used, int[] match) {
        if (ingredientIndex == effective.size()) return true;

        SizedIngredient required = effective.get(ingredientIndex);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (used[slot] || stack.getCount() < required.count() || !required.ingredient().test(stack)) continue;
            used[slot] = true;
            match[ingredientIndex] = slot;
            if (findMatch(input, ingredientIndex + 1, used, match)) return true;
            used[slot] = false;
        }
        return false;
    }

    @Override
    public boolean matches(@NotNull FourItemStackRecipeInput input, @NotNull Level level) {
        return findMatch(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull FourItemStackRecipeInput input,
                                       HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (SizedIngredient ingredient : effective) {
            ingredients.add(ingredient.ingredient());
        }
        return ingredients;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= effective.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AECSRecipeSerializers.CRYSTAL_INFUSER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return AECSRecipeTypes.CRYSTAL_INFUSER.get();
    }
}
