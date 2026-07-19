package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatAE2LTRecipeProvider extends AECSRecipeProvider {

    public AECSCompatAE2LTRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS AE2LT Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries) {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.AE2LT_ID));
        super.buildRecipes(compatOut, registries);

        var overloadCrystalId = ResourceLocation.fromNamespaceAndPath(AECSConstants.AE2LT_ID, "charged_overload_crystal");
        var overloadCrystal = BuiltInRegistries.ITEM.getOptional(overloadCrystalId);
        if (overloadCrystal.isEmpty()) {
            AE2CrystalScience.LOGGER.warn("Skipping AE2 Lightning Tech compatibility recipes because required item {} is not registered", overloadCrystalId);
            return;
        }

        packAndUnpack2x2(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                overloadCrystal.get(), AECSBlocks.CHARGED_OVERLOAD_CRYSTAL_BLOCK);
    }
}
