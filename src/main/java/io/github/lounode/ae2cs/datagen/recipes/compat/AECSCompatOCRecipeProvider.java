package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.datagen.providers.tags.ConventionTags;
import com.wintercogs.ae2omnicells.common.init.OCBlocks;
import com.wintercogs.ae2omnicells.common.init.OCItems;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatOCRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatOCRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS OMNI CELLS Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.OMNI_CELL_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.OMNI_LINK_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.COMPLEX_LINK_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.MULTIDIMENSIONAL_EXPANSION_PRINT_PRESS, AECSItems.ENDER_BLANK_PRINT_PRESS);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSItems.ENDER_BLANK_PRINT_PRESS)
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
    }
}