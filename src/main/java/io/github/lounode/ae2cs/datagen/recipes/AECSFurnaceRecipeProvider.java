package io.github.lounode.ae2cs.datagen.recipes;

import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSFurnaceRecipeProvider extends AECSRecipeProvider
{
    public AECSFurnaceRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Furnace Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        smeltFood(RecipeCategory.FOOD, AECSItems.FLOUR, Items.BREAD, 0.1f, 200, recipeOutput);
    }
}