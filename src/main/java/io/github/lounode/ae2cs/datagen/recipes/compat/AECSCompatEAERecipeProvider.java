package io.github.lounode.ae2cs.datagen.recipes.compat;

import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.util.EAETags;
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

public class AECSCompatEAERecipeProvider extends AECSRecipeProvider
{
    public AECSCompatEAERecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS EAE Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.EAE_ID));

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, EAESingletons.CONCURRENT_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        CircuitEtcherRecipeBuilder.etching(EAESingletons.CONCURRENT_PROCESSOR, 36, 57600)
                .require(EAETags.ENTRO_BLOCK, 9)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);
    }
}