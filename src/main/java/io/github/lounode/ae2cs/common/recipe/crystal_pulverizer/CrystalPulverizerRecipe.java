package io.github.lounode.ae2cs.common.recipe.crystal_pulverizer;

import io.github.lounode.ae2cs.common.init.AECSRecipeSerializers;
import io.github.lounode.ae2cs.common.init.AECSRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public class CrystalPulverizerRecipe implements Recipe<SingleRecipeInput>
{
    private final SizedIngredient input;
    private final ItemStack result;
    private final int energyCost;
    private final PlacementInfo placementInfo;

    public CrystalPulverizerRecipe(SizedIngredient input, ItemStack result, int energyCost)
    {
        if (isDefinitelyEmpty(input.ingredient()) || input.count() <= 0)
        {
            throw new IllegalArgumentException("Input cannot be empty");
        }
        if (energyCost <= 0)
        {
            throw new IllegalArgumentException("Time must be positive");
        }

        this.input = input;
        this.result = result;
        this.energyCost = energyCost;
        this.placementInfo = createPlacementInfo(input.ingredient());
    }

    private static boolean isDefinitelyEmpty(Ingredient ingredient)
    {
        try
        {
            return ingredient.isEmpty();
        }
        catch (UnsupportedOperationException ignored)
        {
            return false;
        }
    }

    private static PlacementInfo createPlacementInfo(Ingredient ingredient)
    {
        try
        {
            return PlacementInfo.create(ingredient);
        }
        catch (UnsupportedOperationException ignored)
        {
            return PlacementInfo.NOT_PLACEABLE;
        }
    }

    public SizedIngredient input()
    {
        return input;
    }

    public ItemStack result()
    {
        return result;
    }

    public int energyCost()
    {
        return energyCost;
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput singleRecipeInput, @NotNull Level level)
    {
        return input.test(singleRecipeInput.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput singleRecipeInput)
    {
        return result.copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public @NotNull RecipeSerializer<CrystalPulverizerRecipe> getSerializer()
    {
        return AECSRecipeSerializers.CRYSTAL_PULVERIZER.get();
    }

    @Override
    public @NotNull RecipeType<CrystalPulverizerRecipe> getType()
    {
        return AECSRecipeTypes.CRYSTAL_PULVERIZER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
