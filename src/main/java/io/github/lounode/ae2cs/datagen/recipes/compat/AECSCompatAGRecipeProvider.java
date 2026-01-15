package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatAGRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatAGRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS AG Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        var compatOut = recipeOutput.withConditions(modLoaded(AECSConstants.AG_ID));
    }
}