package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput, HolderLookup.@NotNull Provider registries)
    {
        super.buildRecipes(recipeOutput, registries);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.FLOUR, 2, 8000)
                .require(Tags.Items.CROPS_WHEAT, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.RESONATING_DUST, 1, 8000)
                .require(AECSTags.Items.GEM_RESONATING, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.ENDER_DUST, 1, 8000)
                .require(AECSTags.Items.GEM_ENDER_QUARTZ, 1)
                .save(recipeOutput, "pulverizer/" + getItemName(AEItems.ENDER_DUST) + "_from_quartz");

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.ENDER_DUST, 1, 8000)
                .require(Items.ENDER_PEARL, 1)
                .save(recipeOutput, "pulverizer/" + getItemName(AEItems.ENDER_DUST) + "_from_pearl");

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.SKY_DUST, 1, 8000)
                .require(AECSTags.Items.GEM_SKY_STONE_CRYSTAL, 1)
                .save(recipeOutput, "pulverizer/" + getItemName(AEItems.SKY_DUST) + "_from_quartz");

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.SKY_DUST, 1, 8000)
                .require(AEBlocks.SKY_STONE_BLOCK, 1)
                .save(recipeOutput, "pulverizer/" + getItemName(AEItems.SKY_DUST) + "_from_sky_stone_block");

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.NETHER_QUARTZ_DUST, 1, 8000)
                .require(Tags.Items.GEMS_QUARTZ, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.QUANTUM_CRYSTAL_DUST, 1, 8000)
                .require(AECSTags.Items.PURE_QUANTUM_CRYSTAL, 1)
                .save(recipeOutput, "quantum_crystal_dust_from_crystal");

        CrystalPulverizerRecipeBuilder.pulverizing(Blocks.GRAVEL, 1, 8000)
                .require(Blocks.STONE, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(Blocks.SAND, 1, 8000)
                .require(Blocks.GRAVEL, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(Blocks.RED_SAND, 1, 8000)
                .require(Blocks.GRANITE, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(Blocks.SOUL_SAND, 1, 8000)
                .require(Blocks.BLACKSTONE, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(Items.GUNPOWDER, 1, 8000)
                .require(Items.FLINT, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.CERTUS_QUARTZ_DUST, 1, 8000)
                .require(ConventionTags.ALL_CERTUS_QUARTZ, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.FLUIX_DUST, 1, 8000)
                .require(ConventionTags.ALL_FLUIX, 1)
                .save(recipeOutput);

        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.CERTUS_QUARTZ_CRYSTAL, 6, 8000)
                .require(AECSTags.Items.ORES_CERTUS_QUARTZ, 1)
                .save(recipeOutput);
    }
}