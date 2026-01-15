package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import com.glodblock.github.extendedae.common.EAESingletons;
import gripe._90.megacells.MEGACells;
import gripe._90.megacells.definition.MEGAItems;
import gripe._90.megacells.definition.MEGATags;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatMegaCellRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatMegaCellRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Mega Cell Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.MEGA_CELL_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, MEGAItems.ACCUMULATION_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        CircuitEtcherRecipeBuilder.etching(MEGAItems.ACCUMULATION_PROCESSOR, 9, 14400)
                .require(AECSTags.Items.STORAGE_BLOCK_SKY_STEEL, 1)
                .require(AEBlocks.FLUIX_BLOCK, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 1)
                .save(compatOut);
    }
}