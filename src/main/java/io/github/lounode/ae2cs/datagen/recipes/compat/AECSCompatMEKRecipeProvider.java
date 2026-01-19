package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSEnchantments;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
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
    protected void buildRecipes(@NotNull RecipeOutput originalOut, HolderLookup.@NotNull Provider registries)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.MEK_ID));
        super.buildRecipes(compatOut, registries);

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_IRRADIATED_CRYSTAL, AECSBlocks.IRRADIATED_CRYSTAL_BLOCK);

        PressurizedReactionRecipeBuilder.reaction(
                        ItemStackIngredient.of(SizedIngredient.of(AECSItems.NETHER_QUARTZ_SEED, 1)),
                        FluidStackIngredient.of(SizedFluidIngredient.of(MekanismTags.Fluids.SULFURIC_ACID, 1000)),
                        ChemicalStackIngredient.of(new SingleChemicalIngredient(MekanismChemicals.FISSILE_FUEL), 1000),
                        400, AECSItems.IRRADIATED_SEED.toStack())
                .build(compatOut, getPressurizedReactionPath("irradiated_seed_first"));

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.IRRADIATED_SEED.toStack(64), 102400)
                .require(MekanismTags.Items.DUSTS_SULFUR, 32)
                .require(AECSTags.Items.DUSTS_URANIUM, 16)
                .require(AECSItems.IRRADIATED_CRYSTAL_DUST, 16)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(
                        enchantedItem(registries, MekanismItems.ATOMIC_DISASSEMBLER, 1, AECSEnchantments.ENDER_LINK, 1), 64000)
                .require(MekanismItems.ATOMIC_DISASSEMBLER, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ, 1)
                .require(AEItems.SINGULARITY, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(
                        enchantedItem(registries, MekanismItems.MEKA_TOOL, 1, AECSEnchantments.ENDER_LINK, 1), 64000)
                .require(MekanismItems.MEKA_TOOL, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ, 1)
                .require(AEItems.SINGULARITY, 1)
                .save(compatOut);

        ItemStackToChemicalRecipeBuilder.oxidizing(
                        ItemStackIngredient.of(SizedIngredient.of(AECSTags.Items.PURE_IRRADIATED_CRYSTAL, 1)),
                        MekanismChemicals.NUCLEAR_WASTE.asStack(500))
                .build(compatOut, getOxidizingPath("nuclear_waste"));

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.IRRADIATED_CRYSTAL_DUST, 1, 8000)
                .require(AECSTags.Items.PURE_IRRADIATED_CRYSTAL, 1)
                .save(compatOut);
    }

    protected ResourceLocation getPressurizedReactionPath(String outName)
    {
        return AE2CrystalScience.makeId("pressurized_reaction/" + outName);
    }

    protected ResourceLocation getOxidizingPath(String outName)
    {
        return AE2CrystalScience.makeId("oxidizing/" + outName);
    }
}