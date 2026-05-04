package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import com.wintercogs.ae2omnicells.common.init.OCBlocks;
import com.wintercogs.ae2omnicells.common.init.OCItems;
import com.wintercogs.ae2omnicells.common.init.OCTags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> originalOut)
    {
        var compatOut = withConditions(originalOut, modLoaded(AECSConstants.OMNI_CELL_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.OMNI_LINK_PRINT_PRESS.get(), AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.COMPLEX_LINK_PRINT_PRESS.get(), AECSItems.ENDER_BLANK_PRINT_PRESS);
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, OCItems.MULTIDIMENSIONAL_EXPANSION_PRINT_PRESS.get(), AECSItems.ENDER_BLANK_PRINT_PRESS);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSItems.ENDER_BLANK_PRINT_PRESS)
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', OCBlocks.ENDER_INGOT_BLOCK.get())
                .define('b', ConventionTags.INSCRIBER_PRESSES)
                .unlockedBy(getHasName(OCBlocks.ENDER_INGOT_BLOCK.get()), has(OCBlocks.ENDER_INGOT_BLOCK.get()))
                .save(compatOut, getCrafterPath(AECSItems.ENDER_BLANK_PRINT_PRESS, true));

        CircuitEtcherRecipeBuilder.etching(OCItems.OMNI_LINK_PROCESSOR.get(), 36, 14400)
                .require(OCBlocks.ENDER_INGOT_BLOCK.get(), 4)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(OCItems.COMPLEX_LINK_PROCESSOR.get(), 36, 14400)
                .require(OCBlocks.NETHERITE_SCRAP_BLOCK.get(), 4)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(OCItems.MULTIDIMENSIONAL_EXPANSION_PROCESSOR.get(), 36, 14400)
                .require(OCBlocks.SINGULARITY_BLOCK.get(), 4)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(OCItems.OMNI_LINK_PROCESSOR.get(), 32, 51200)
                .require(OCItems.OMNI_LINK_CIRCUIT_PRINT.get(), 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
        CrystalAggregatorRecipeBuilder.aggregating(OCItems.COMPLEX_LINK_PROCESSOR.get(), 32, 51200)
                .require(OCItems.COMPLEX_LINK_CIRCUIT_PRINT.get(), 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
        CrystalAggregatorRecipeBuilder.aggregating(OCItems.MULTIDIMENSIONAL_EXPANSION_PROCESSOR.get(), 32, 51200)
                .require(OCItems.MULTIDIMENSIONAL_EXPANSION_CIRCUIT_PRINT.get(), 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);
        CrystalAggregatorRecipeBuilder.aggregating(OCItems.ENDER_INGOT.get(), 64, 102400)
                .require(OCTags.ENDER_PEARL_DUST, 32)
                .require(Tags.Items.INGOTS_IRON, 32)
                .require(ConventionTags.CERTUS_QUARTZ_DUST, 32)
                .save(compatOut);
    }
}
