package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSEtcherRecipeProvider extends AECSRecipeProvider
{
    public AECSEtcherRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Etcher Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        CircuitEtcherRecipeBuilder.etching(AECSItems.RESONATING_PROCESSOR, 9, 14400)
                .require(AECSTags.Items.STORAGE_BLOCK_RESONATING, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SKY_STONE_CRYSTAL, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(recipeOutput);

        CircuitEtcherRecipeBuilder.etching(AEItems.ENGINEERING_PROCESSOR, 9, 14400)
                .require(Tags.Items.STORAGE_BLOCKS_DIAMOND, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(recipeOutput);

        CircuitEtcherRecipeBuilder.etching(AEItems.LOGIC_PROCESSOR, 9, 14400)
                .require(Tags.Items.STORAGE_BLOCKS_GOLD, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(recipeOutput);

        CircuitEtcherRecipeBuilder.etching(AEItems.CALCULATION_PROCESSOR, 36, 57600)
                .require(AECSTags.Items.STORAGE_BLOCK_CERTUS_QUARTZ, 9)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(recipeOutput);

        CircuitEtcherRecipeBuilder.etching(AECSItems.SIMPLE_PROCESSOR, 36, 57600)
                .require(Blocks.QUARTZ_BLOCK, 9)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(recipeOutput);
    }
}