package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
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
        // 集成接口
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.toStack(), 16000)
                .require(AEBlocks.PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AEBlocks.INTERFACE, 1)
                .save(recipeOutput);

        // 末影接口
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.ENDER_INTERFACE_BLOCK.toStack(), 16000)
                .require(AECSTags.Items.GEM_ENDER_QUARTZ, 4)
                .require(AEBlocks.INTERFACE, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .save(recipeOutput);

        // 谐振样板供应器
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.toStack(), 16000)
                .require(AEBlocks.PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AECSBlocks.ENDER_INTERFACE_BLOCK, 1)
                .save(recipeOutput);

        // 石英震荡钟
        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK.toStack(), 8000)
                .require(AEParts.LEVEL_EMITTER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK, 1)
                .save(recipeOutput);

    }
}