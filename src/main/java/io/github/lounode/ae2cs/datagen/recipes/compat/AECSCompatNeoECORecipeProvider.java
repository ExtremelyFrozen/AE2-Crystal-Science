package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

import cn.dancingsnow.neoecoae.recipe.IntegratedWorkingStationRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatNeoECORecipeProvider extends AECSRecipeProvider {

    public AECSCompatNeoECORecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS NeoECO Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries) {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.NEOECOAE_ID));
        super.buildRecipes(compatOut, registries);

        Item energizedCrystalDust = externalItem("energized_crystal_dust");
        Item energizedFluixDust = externalItem("energized_fluix_crystal_dust");
        Item superconductingProcessor = externalItem("superconducting_processor");

        CircuitEtcherRecipeBuilder.etching(superconductingProcessor, 36, 57600)
                .require(externalItem("energized_superconductive_block"), 4)
                .require(AECSBlocks.PURE_CRYSTAL_GRID_BLOCK, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(superconductingProcessor, 32, 51200)
                .require(externalItem("superconducting_processor_print"), 32)
                .require(externalItem("crystal_matrix"), 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                externalItem("crystal_matrix"), AECSBlocks.PURE_CRYSTAL_GRID_BLOCK);

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC,
                externalItem("superconducting_processor_press"), AECSItems.BLANK_PRINT_PRESS);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.ENERGIZED_CERTUS_QUARTZ_SEED, 32, 51200)
                .require(energizedCrystalDust, 8)
                .require(AEItems.CERTUS_QUARTZ_DUST, 8)
                .require(Tags.Items.DUSTS_GLOWSTONE, 16)
                .save(compatOut, "aggregator/energized_certus_quartz_seed");

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.ENERGIZED_FLUIX_CRYSTAL_SEED, 32, 51200)
                .require(energizedFluixDust, 8)
                .require(ConventionTags.FLUIX_DUST, 4)
                .require(Tags.Items.DUSTS_REDSTONE, 16)
                .save(compatOut, "aggregator/energized_fluix_crystal_seed");

        IntegratedWorkingStationRecipe.builder()
                .require(energizedCrystalDust, 8)
                .require(AEItems.CERTUS_QUARTZ_DUST, 8)
                .require(Tags.Items.DUSTS_GLOWSTONE, 8)
                .itemOutput(AECSItems.ENERGIZED_CERTUS_QUARTZ_SEED, 32)
                .energy(62000)
                .save(compatOut, AE2CrystalScience.makeId("integrated_working_station/energized_certus_quartz_seed"));

        IntegratedWorkingStationRecipe.builder()
                .require(energizedFluixDust, 8)
                .require(AEItems.FLUIX_DUST, 4)
                .require(Tags.Items.DUSTS_REDSTONE, 8)
                .itemOutput(AECSItems.ENERGIZED_FLUIX_CRYSTAL_SEED, 32)
                .energy(62000)
                .save(compatOut, AE2CrystalScience.makeId("integrated_working_station/energized_fluix_crystal_seed"));

        CrystalPulverizerRecipeBuilder.pulverizing(energizedCrystalDust, 1, 8000)
                .require(externalItem("energized_crystal"), 1)
                .save(compatOut, "pulverizer/energized_crystal_dust_from_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(energizedFluixDust, 1, 8000)
                .require(externalItem("energized_fluix_crystal"), 1)
                .save(compatOut, "pulverizer/energized_fluix_crystal_dust_from_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(energizedCrystalDust, 1, 8000)
                .require(AECSItems.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL, 1)
                .save(compatOut, "pulverizer/energized_crystal_dust_from_pure_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(energizedFluixDust, 1, 8000)
                .require(AECSItems.PURE_ENERGIZED_FLUIX_CRYSTAL, 1)
                .save(compatOut, "pulverizer/energized_fluix_crystal_dust_from_pure_crystal");
    }

    private static Item externalItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(AECSConstants.NEOECOAE_ID, path));
    }
}
