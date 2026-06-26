package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;

import gripe._90.megacells.definition.MEGAItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AECSCompatMegaCellRecipeProvider extends AECSRecipeProvider {

    public AECSCompatMegaCellRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS Mega Cell Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> originalOut) {
        var compatOut = withConditions(originalOut, modLoaded(AECSConstants.MEGA_CELL_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, MEGAItems.ACCUMULATION_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        CircuitEtcherRecipeBuilder.etching(MEGAItems.ACCUMULATION_PROCESSOR, 36, 14400)
                .require(AECSTags.Items.STORAGE_BLOCK_SKY_STEEL, 4)
                .require(AEBlocks.FLUIX_BLOCK, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(MEGAItems.ACCUMULATION_PROCESSOR, 32, 51200)
                .require(MEGAItems.ACCUMULATION_PROCESSOR_PRINT, 32)
                .require(ConventionTags.FLUIX_DUST, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
    }
}
