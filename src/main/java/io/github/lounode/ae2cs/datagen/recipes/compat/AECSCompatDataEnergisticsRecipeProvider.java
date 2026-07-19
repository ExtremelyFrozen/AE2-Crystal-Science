package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;

import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * 为 DataEnergistics 的数据水晶补充谐振晶体生长链配方。
 */
public class AECSCompatDataEnergisticsRecipeProvider extends AECSRecipeProvider {

    public AECSCompatDataEnergisticsRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS DataEnergistics Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries) {
        RecipeOutput compatOut = originalOut.withConditions(modLoaded(AECSConstants.DATA_ENERGISTICS_ID));
        Item dataDust = externalItem("data_dust");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.DATA_CRYSTAL_SEED)
                .requires(dataDust)
                .requires(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .requires(Blocks.SAND, 2)
                .unlockedBy(getHasName(dataDust), has(dataDust))
                .save(compatOut, getCrafterPath(AECSItems.DATA_CRYSTAL_SEED, false));

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.DATA_CRYSTAL_SEED, 32, 51200)
                .require(dataDust, 8)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 8)
                .require(Blocks.SAND, 32)
                .save(compatOut, "aggregator/data_crystal_seed");

        CrystalPulverizerRecipeBuilder.pulverizing(dataDust, 1, 8000)
                .require(AECSItems.PURE_DATA_CRYSTAL, 1)
                .save(compatOut, "pulverizer/data_dust_from_pure_data_crystal");
    }

    private static Item externalItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(AECSConstants.DATA_ENERGISTICS_ID, path));
    }
}
