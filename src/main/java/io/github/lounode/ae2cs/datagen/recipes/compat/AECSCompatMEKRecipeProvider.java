package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatMEKRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatMEKRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS MEK Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.MEK_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_IRRADIATED_CRYSTAL, AECSBlocks.IRRADIATED_CRYSTAL_BLOCK);

        PressurizedReactionRecipeBuilder.reaction(
                        ItemStackIngredient.of(SizedIngredient.of(AECSItems.netherQuartzSeed, 1)),
                        FluidStackIngredient.of(SizedFluidIngredient.of(MekanismTags.Fluids.SULFURIC_ACID, 1000)),
                        ChemicalStackIngredient.of(new SingleChemicalIngredient(MekanismChemicals.FISSILE_FUEL), 1000),
                        400, AECSItems.IRRADIATED_SEED.toStack())
                .build(compatOut, getPressurizedReactionPath("irradiated_seed_first"));

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.IRRADIATED_SEED.toStack(4), 16000)
                .require(MekanismTags.Items.DUSTS_SULFUR, 2)
                .require(AECSTags.Items.DUSTS_URANIUM, 1)
                .require(AECSItems.IRRADIATED_CRYSTAL_DUST, 1)
                .save(compatOut);
    }

    protected ResourceLocation getPressurizedReactionPath(String outName)
    {
        return AE2CrystalScience.makeId("pressurized_reaction/" + outName);
    }
}