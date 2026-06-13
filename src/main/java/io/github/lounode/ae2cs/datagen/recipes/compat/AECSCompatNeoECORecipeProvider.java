package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

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
    }

    private static Item externalItem(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
