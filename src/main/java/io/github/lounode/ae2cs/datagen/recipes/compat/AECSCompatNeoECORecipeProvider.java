package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

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

        packAndUnpack2x2(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                externalItem(AECSConstants.NEOECOAE_ID, "crystal_matrix"), AECSBlocks.PURE_CRYSTAL_GRID_BLOCK);

        addCrystalGrowthRecipes(compatOut, AECSItems.ENERGIZED_FLUIX_CRYSTAL_SEED,
                AECSItems.PURE_ENERGIZED_FLUIX_CRYSTAL, externalItem(AECSConstants.NEOECOAE_ID, "energized_fluix_crystal"));
        addCrystalGrowthRecipes(compatOut, AECSItems.ENERGIZED_CERTUS_QUARTZ_SEED,
                AECSItems.PURE_ENERGIZED_CERTUS_QUARTZ_CRYSTAL, externalItem(AECSConstants.NEOECOAE_ID, "energized_crystal"));
    }

    private static void addCrystalGrowthRecipes(RecipeOutput output, ItemLike seed, ItemLike pureCrystal, Item crystal) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, seed)
                .requires(crystal)
                .requires(Blocks.SAND, 2)
                .unlockedBy(getHasName(crystal), has(crystal))
                .save(output, getCrafterPath(seed, false));

        CrystalAggregatorRecipeBuilder.aggregating(seed, 32, 51200)
                .require(crystal, 8)
                .require(Blocks.SAND, 32)
                .save(output, "aggregator/" + getItemName(seed));

        CrystalPulverizerRecipeBuilder.pulverizing(crystal, 1, 8000)
                .require(pureCrystal, 1)
                .save(output, "pulverizer/" + getItemName(crystal) + "_from_" + getItemName(pureCrystal));
    }

    private static Item externalItem(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
