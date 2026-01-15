package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSPulverizerRecipeProvider extends AECSRecipeProvider
{
    public AECSPulverizerRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Pulverizer Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.FLOUR, 2, 1600)
                .require(Tags.Items.CROPS_WHEAT, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.RESONATING_DUST, 1, 1600)
                .require(AECSTags.Items.GEM_RESONATING, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.ENDER_DUST, 1, 1600)
                .require(AECSTags.Items.GEM_ENDER_QUARTZ, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.SKY_DUST, 1, 1600)
                .require(AECSTags.Items.GEM_SKY_STONE_CRYSTAL, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.NETHER_QUARTZ_DUST, 1, 1600)
                .require(Tags.Items.GEMS_QUARTZ, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.QUANTUM_CRYSTAL_DUST, 1, 1600)
                .require(AECSItems.pureQuantumCrystal, 1)
                .save(recipeOutput, "quantum_crystal_dust_from_crystal");


    }
}