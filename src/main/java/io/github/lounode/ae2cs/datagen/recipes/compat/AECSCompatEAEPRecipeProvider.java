package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;

import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * 为 ExtendedAE Plus 的莱卓罗水晶补充高纯生长链。
 */
public class AECSCompatEAEPRecipeProvider extends AECSRecipeProvider {

    public AECSCompatEAEPRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS ExtendedAE Plus Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries) {
        RecipeOutput compatOut = originalOut.withConditions(modLoaded(AECSConstants.EAEP_ID));
        Item lattraCrystal = externalItem("lattra_crystal");
        Item lattraDust = externalItem("lattra_dust");
        Item lattraCrystalBlock = externalItem("lattra_crystal_block");

        pack2x2(compatOut, RecipeCategory.MISC, AECSItems.PURE_LATTRA_CRYSTAL, lattraCrystalBlock);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.LATTRA_CRYSTAL_SEED, 32, 51200)
                .require(lattraDust, 8)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 8)
                .require(AECSTags.Items.DUST_QUARTZ, 16)
                .save(compatOut, "aggregator/lattra_crystal_seed");

        CrystalPulverizerRecipeBuilder.pulverizing(lattraDust, 1, 8000)
                .require(AECSTags.Items.GEM_LATTRA_CRYSTAL, 1)
                .save(compatOut, "pulverizer/lattra_dust_from_crystal");
    }

    private static Item externalItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(AECSConstants.EAEP_ID, path));
    }
}
