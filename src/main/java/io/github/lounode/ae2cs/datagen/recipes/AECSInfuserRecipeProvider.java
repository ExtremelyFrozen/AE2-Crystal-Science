package io.github.lounode.ae2cs.datagen.recipes;

import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalInfuserRecipeBuilder;

import appeng.core.definitions.AEItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSInfuserRecipeProvider extends AECSRecipeProvider {

    public AECSInfuserRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS Crystal Infuser Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output, HolderLookup.@NotNull Provider registries) {
        super.buildRecipes(output, registries);

        CrystalInfuserRecipeBuilder.infusing(AECSItems.ENERGIZED_CERTUS_QUARTZ_SEED, 1, 64000)
                .require(AECSItems.CERTUS_QUARTZ_SEED, 1)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 1)
                .require(AECSItems.PURE_RESONATING_CRYSTAL, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .save(output, "infuser/energized_certus_quartz_seed");

        CrystalInfuserRecipeBuilder.infusing(AECSItems.ENERGIZED_FLUIX_CRYSTAL_SEED, 1, 64000)
                .require(AECSItems.FLUIX_CRYSTAL_SEED, 1)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 1)
                .require(Tags.Items.DUSTS_REDSTONE, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .save(output, "infuser/energized_fluix_crystal_seed");

        CrystalInfuserRecipeBuilder.infusing(AECSItems.ENERGIZED_CERTUS_QUARTZ_SEED, 2, 128000)
                .require(AECSItems.PURE_CERTUS_QUARTZ_CRYSTAL, 1)
                .require(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 2)
                .require(Items.REDSTONE, 8)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .save(output, "infuser/energized_certus_quartz_seed_from_crystal");
    }
}
