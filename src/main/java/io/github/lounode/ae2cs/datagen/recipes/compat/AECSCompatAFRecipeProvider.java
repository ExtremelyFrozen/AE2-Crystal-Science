package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import com.glodblock.github.appflux.common.AFSingletons;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.AF_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_REDSTONE_CRYSTAL, AECSBlocks.PURE_REDSTONE_CRYSTAL_BLOCK);

        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AFSingletons.ENERGY_PROCESSOR_PRESS, AECSItems.BLANK_PRINT_PRESS);

        chargedRecipeWithAggregator(compatOut, AECSItems.PURE_REDSTONE_CRYSTAL, AFSingletons.REDSTONE_CRYSTAL);

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.REDSTONE_CRYSTAL_DUST.toStack(), 8000)
                .require(AECSTags.Items.PURE_REDSTONE_CRYSTAL, 1)
                .save(compatOut, "pulverizer/" + getItemName(AECSItems.REDSTONE_CRYSTAL_DUST) + "_from_pure_redstone_crystal");
        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.REDSTONE_CRYSTAL_DUST.toStack(), 8000)
                .require(AFSingletons.REDSTONE_CRYSTAL, 1)
                .save(compatOut, "pulverizer/" + getItemName(AECSItems.REDSTONE_CRYSTAL_DUST) + "_from_redstone_crystal");
        CrystalPulverizerRecipeBuilder.pulverizing(Items.REDSTONE.getDefaultInstance(), 8000)
                .require(AECSTags.Items.DUST_REDSTONE_CRYSTAL, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.REDSTONE_CRYSTAL_SEED.toStack(64), 102400)
                .require(Tags.Items.DUSTS_GLOWSTONE, 32)
                .require(Tags.Items.DUSTS_REDSTONE, 16)
                .require(AECSTags.Items.DUST_REDSTONE_CRYSTAL, 16)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AFSingletons.INSULATING_RESIN.getDefaultInstance(), 16000)
                .require(Blocks.CACTUS, 1)
                .require(ConventionTags.SILICON, 1)
                .require(Items.SLIME_BALL, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AFSingletons.ENERGY_PROCESSOR, 64, 102400)
                .require(AFSingletons.ENERGY_PROCESSOR_PRINT, 64)
                .require(Tags.Items.DUSTS_REDSTONE, 64)
                .require(AEItems.SILICON_PRINT, 64)
                .save(compatOut);

        CrystalPulverizerRecipeBuilder.pulverizing(AFSingletons.EMERALD_DUST, 1, 8000)
                .require(Tags.Items.GEMS_EMERALD, 1)
                .save(compatOut);

        CrystalPulverizerRecipeBuilder.pulverizing(AFSingletons.DIAMOND_DUST, 1, 8000)
                .require(Tags.Items.GEMS_DIAMOND, 1)
                .save(compatOut);
    }
}