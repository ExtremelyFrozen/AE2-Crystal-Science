package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.util.AGTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatAGRecipeProvider extends AECSRecipeProvider {

    public AECSCompatAGRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS AG Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries) {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.AG_ID));
        super.buildRecipes(compatOut, registries);

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AGSingletons.ORIGINATION_PRESS, AECSItems.BLANK_PRINT_PRESS);

        CrystalAggregatorRecipeBuilder.aggregating(AGSingletons.EMBER_CRYSTAL, 32, 51200)
                .require(AECSTags.Items.PURE_EMBER_CRYSTAL, 16)
                .require(AEBlocks.TINY_TNT, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.EMBER_SEED.toStack(32), 51200)
                .require(AGTags.EMBER_DUST, 8)
                .require(Items.BLAZE_POWDER, 8)
                .require(Tags.Items.GUNPOWDERS, 16)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AGSingletons.ORIGINATION_PROCESSOR, 32, 51200)
                .require(AGSingletons.ORIGINATION_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);

        CrystalPulverizerRecipeBuilder.pulverizing(AGSingletons.EMBER_DUST, 1, 8000)
                .require(AECSTags.Items.PURE_EMBER_CRYSTAL, 1)
                .save(compatOut, "pulverizer/" + getItemName(AGSingletons.EMBER_DUST) + "_from_pure_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(AGSingletons.EMBER_DUST, 1, 8000)
                .require(AGSingletons.EMBER_CRYSTAL, 1)
                .save(compatOut, "pulverizer/" + getItemName(AGSingletons.EMBER_DUST) + "_from_original_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(AGSingletons.COPPER_DUST, 1, 8000)
                .require(Tags.Items.INGOTS_COPPER, 1)
                .save(compatOut);

        CrystalPulverizerRecipeBuilder.pulverizing(AGSingletons.GOLD_DUST, 1, 8000)
                .require(Tags.Items.INGOTS_GOLD, 1)
                .save(compatOut);

        CrystalPulverizerRecipeBuilder.pulverizing(AGSingletons.NETHERITE_DUST, 1, 8000)
                .require(Tags.Items.INGOTS_NETHERITE, 1)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(AGSingletons.ORIGINATION_PROCESSOR, 36, 57600)
                .require(AGSingletons.EMBER_BLOCK, 9)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);
    }
}
