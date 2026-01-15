package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEBlocks;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSAggregatorRecipeProvider extends AECSRecipeProvider
{
    public AECSAggregatorRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Aggregator Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.toStack(), 16000)
                .require(AEBlocks.PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AEBlocks.INTERFACE, 1)
                .save(recipeOutput);

    }
}