package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import com.glodblock.github.appflux.common.AFItemAndBlock;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AECSCompatAFRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatAFRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS AF Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> originalOut)
    {
        var compatOut = withConditions(originalOut, modLoaded(AECSConstants.AF_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_REDSTONE_CRYSTAL, AECSBlocks.PURE_REDSTONE_CRYSTAL_BLOCK);

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AFItemAndBlock.ENERGY_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        chargedRecipeWithAggregator(compatOut, AECSItems.PURE_REDSTONE_CRYSTAL, AFItemAndBlock.REDSTONE_CRYSTAL);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.REDSTONE_CRYSTAL_DUST.toStack(), 8000)
                .require(AECSTags.Items.PURE_REDSTONE_CRYSTAL, 1)
                .save(compatOut, "pulverizer/" + getItemName(AECSItems.REDSTONE_CRYSTAL_DUST) + "_from_pure_redstone_crystal");
        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.REDSTONE_CRYSTAL_DUST.toStack(), 8000)
                .require(AFItemAndBlock.REDSTONE_CRYSTAL, 1)
                .save(compatOut, "pulverizer/" + getItemName(AECSItems.REDSTONE_CRYSTAL_DUST) + "_from_redstone_crystal");
        CrystalPulverizerRecipeBuilder.pulverizing(Items.REDSTONE.getDefaultInstance(), 8000)
                .require(AECSTags.Items.DUST_REDSTONE_CRYSTAL, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.REDSTONE_CRYSTAL_SEED.toStack(32), 51200)
                .require(Tags.Items.DUSTS_GLOWSTONE, 16)
                .require(Tags.Items.DUSTS_REDSTONE, 8)
                .require(AECSTags.Items.DUST_REDSTONE_CRYSTAL, 8)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AFItemAndBlock.INSULATING_RESIN.getDefaultInstance(), 16000)
                .require(Blocks.CACTUS, 1)
                .require(ConventionTags.SILICON, 1)
                .require(Items.SLIME_BALL, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AFItemAndBlock.ENERGY_PROCESSOR, 32, 51200)
                .require(AFItemAndBlock.ENERGY_PROCESSOR_PRINT, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 32)
                .require(AEItems.SILICON_PRINT, 32)
                .save(compatOut);

        CircuitEtcherRecipeBuilder.etching(AFItemAndBlock.ENERGY_PROCESSOR, 36, 14400)
                .require(AFItemAndBlock.CHARGED_REDSTONE, 36)
                .require(Tags.Items.STORAGE_BLOCKS_REDSTONE, 4)
                .require(AECSTags.Items.STORAGE_BLOCK_SILICON, 4)
                .save(compatOut);
    }
}
