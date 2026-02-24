package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEItems;
import com.glodblock.github.appflux.util.AFTags;
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
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> originalOut)
    {
        var compatOut = withConditions(originalOut, modLoaded(AECSConstants.MEK_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_IRRADIATED_CRYSTAL, AECSBlocks.IRRADIATED_CRYSTAL_BLOCK);

        PressurizedReactionRecipeBuilder.reaction(
                        ItemStackIngredientCreator.INSTANCE.from(AECSItems.NETHER_QUARTZ_SEED, 1),
                        FluidStackIngredientCreator.INSTANCE.from(MekanismTags.Fluids.SULFURIC_ACID, 1000),
                        GasStackIngredientCreator.INSTANCE.from(MekanismGases.FISSILE_FUEL, 1000),
                        400, AECSItems.IRRADIATED_SEED.toStack())
                .build(compatOut, getPressurizedReactionPath("irradiated_seed_first"));

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.IRRADIATED_SEED.toStack(32), 51200)
                .require(MekanismTags.Items.DUSTS_SULFUR, 16)
                .require(AECSTags.Items.DUSTS_URANIUM, 8)
                .require(AECSItems.IRRADIATED_CRYSTAL_DUST, 8)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(
                        enchantedItem(MekanismItems.ATOMIC_DISASSEMBLER, 1, AECSEnchantments.ENDER_LINK.get(), 1), 64000)
                .require(MekanismItems.ATOMIC_DISASSEMBLER, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ, 1)
                .require(AEItems.SINGULARITY, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(
                        enchantedItem(MekanismItems.MEKA_TOOL, 1, AECSEnchantments.ENDER_LINK.get(), 1), 64000)
                .require(MekanismItems.MEKA_TOOL, 1)
                .require(AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ, 1)
                .require(AEItems.SINGULARITY, 1)
                .save(compatOut);

        ItemStackToChemicalRecipeBuilder.oxidizing(
                        ItemStackIngredientCreator.INSTANCE.from(AECSTags.Items.PURE_IRRADIATED_CRYSTAL, 1),
                        MekanismGases.NUCLEAR_WASTE.getStack(500))
                .build(compatOut, getOxidizingPath("nuclear_waste"));

        CrystalPulverizerRecipeBuilder.pulverizing(AECSItems.IRRADIATED_CRYSTAL_DUST, 1, 8000)
                .require(AECSTags.Items.PURE_IRRADIATED_CRYSTAL, 1)
                .save(compatOut);

        ItemStackToItemStackRecipeBuilder.crushing(
                        ItemStackIngredientCreator.INSTANCE.from(Stream.of(ItemStackIngredientCreator.INSTANCE.from(AECSTags.Items.PURE_QUANTUM_CRYSTAL), ItemStackIngredientCreator.INSTANCE.from(AAEItems.QUANTUM_ALLOY))),
                        AECSItems.QUANTUM_CRYSTAL_DUST.toStack())
                .build(compatOut, getCrushingPath("quantum_crystal_dust"));

        ItemStackToItemStackRecipeBuilder.crushing(
                        ItemStackIngredientCreator.INSTANCE.from(Stream.of(ItemStackIngredientCreator.INSTANCE.from(AECSTags.Items.PURE_REDSTONE_CRYSTAL), ItemStackIngredientCreator.INSTANCE.from(AFTags.REDSTONE_GEM))),
                        AECSItems.REDSTONE_CRYSTAL_DUST.toStack())
                .build(compatOut, getCrushingPath("redstone_crystal_dust"));

        ItemStackToItemStackRecipeBuilder.crushing(
                        ItemStackIngredientCreator.INSTANCE.from(AECSTags.Items.PURE_RESONATING_CRYSTAL, 1),
                        AECSItems.RESONATING_DUST.toStack())
                .build(compatOut, getCrushingPath("resonating_crystal_dust"));

        ItemStackToItemStackRecipeBuilder.crushing(
                        ItemStackIngredientCreator.INSTANCE.from(AECSTags.Items.PURE_METEOR_CRYSTAL, 1),
                        AEItems.SKY_DUST.stack())
                .build(compatOut, getCrushingPath("sky_dust"));

        ItemStackToItemStackRecipeBuilder.crushing(
                        ItemStackIngredientCreator.INSTANCE.from(AECSTags.Items.PURE_IRRADIATED_CRYSTAL, 1),
                        AECSItems.IRRADIATED_CRYSTAL_DUST.toStack())
                .build(compatOut, getCrushingPath("irradiated_crystal_dust"));
    }

    protected ResourceLocation getPressurizedReactionPath(String outName)
    {
        return AE2CrystalScience.makeId("pressurized_reaction/" + outName);
    }

    protected ResourceLocation getOxidizingPath(String outName)
    {
        return AE2CrystalScience.makeId("oxidizing/" + outName);
    }

    protected ResourceLocation getCrushingPath(String outName)
    {
        return AE2CrystalScience.makeId("mek_crushing/" + outName);
    }
}