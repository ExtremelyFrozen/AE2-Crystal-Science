package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.ConventionTags;
import appeng.core.definitions.AEItems;
import com.wintercogs.ae2omnicells.common.init.OCBlocks;
import com.wintercogs.ae2omnicells.common.init.OCItems;
import com.wintercogs.ae2omnicells.common.init.OCTags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.recipes.AECSCraftRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.neoforged.neoforge.common.conditions.NeoForgeConditions.modLoaded;

public class AECSCompatOCRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatOCRecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
    {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        var compatOut = this.output.withConditions(modLoaded(AECSConstants.OMNI_CELL_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.OMNI_LINK_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.COMPLEX_LINK_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.MULTIDIMENSIONAL_EXPANSION_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);

        shaped(RecipeCategory.MISC, AECSItems.ENDER_BLANK_PRINT_PRESS)
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', OCBlocks.ENDER_INGOT_BLOCK)
                .define('b', ConventionTags.INSCRIBER_PRESSES)
                .unlockedBy(getHasName(OCBlocks.ENDER_INGOT_BLOCK), has(OCBlocks.ENDER_INGOT_BLOCK))
                .save(compatOut, getCrafterPath(AECSItems.ENDER_BLANK_PRINT_PRESS, true));

        CircuitEtcherRecipeBuilder.etching(OCItems.OMNI_LINK_PROCESSOR, 9, 14400)
                .require(OCBlocks.ENDER_INGOT_BLOCK, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(OCItems.COMPLEX_LINK_PROCESSOR, 9, 14400)
                .require(OCBlocks.NETHERITE_SCRAP_BLOCK, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(OCItems.MULTIDIMENSIONAL_EXPANSION_PROCESSOR, 9, 14400)
                .require(OCBlocks.SINGULARITY_BLOCK, 1)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(OCItems.OMNI_LINK_PROCESSOR, 32, 51200)
                .require(OCItems.OMNI_LINK_CIRCUIT_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
        CrystalAggregatorRecipeBuilder.aggregating(OCItems.COMPLEX_LINK_PROCESSOR, 32, 51200)
                .require(OCItems.COMPLEX_LINK_CIRCUIT_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
        CrystalAggregatorRecipeBuilder.aggregating(OCItems.MULTIDIMENSIONAL_EXPANSION_PROCESSOR, 32, 51200)
                .require(OCItems.MULTIDIMENSIONAL_EXPANSION_CIRCUIT_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
        CrystalAggregatorRecipeBuilder.aggregating(OCItems.ENDER_INGOT, 64, 102400)
                .require(OCTags.ENDER_PEARL_DUST, 32)
                .require(Tags.Items.INGOTS_IRON, 32)
                .require(ConventionTags.CERTUS_QUARTZ_DUST, 32)
                .save(compatOut);
    }

    public static class Runner extends RecipeProvider.Runner
    {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
        {
            super(output, lookupProvider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider, @NotNull RecipeOutput output)
        {
            return new AECSCompatOCRecipeProvider(provider, output);
        }

        @Override
        public @NotNull String getName()
        {
            return "AECS OMNI CELLS Compat Recipes";
        }
    }
}