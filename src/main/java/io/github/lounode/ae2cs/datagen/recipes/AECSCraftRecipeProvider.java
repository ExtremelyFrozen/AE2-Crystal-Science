package io.github.lounode.ae2cs.datagen.recipes;

import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCraftRecipeProvider extends AECSRecipeProvider
{
    public AECSCraftRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Crafting Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
    }
}