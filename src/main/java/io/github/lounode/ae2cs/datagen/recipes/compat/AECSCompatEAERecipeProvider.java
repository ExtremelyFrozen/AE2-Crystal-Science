package io.github.lounode.ae2cs.datagen.recipes.compat;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.extendedae.util.EAETags;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
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

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.MOSTLY_ENTROIZED_FLUIX_BUDDING, 1, 16000)
                .require(AECSItems.ENTRO_CRYSTAL_SEED, 1)
                .require(AECSTags.Items.PURE_RESONATING_CRYSTAL, 1)
                .require(AEBlocks.FLUIX_BLOCK, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.ENTRO_CRYSTAL_SEED, 4, 16000)
                .require(EAETags.ENTRO_DUST, 1)
                .require(AECSTags.Items.DUST_QUARTZ, 3)
                .require(ConventionTags.SKY_STONE_DUST, 1)
                .save(compatOut, "aggregator/" + getItemName(AECSItems.ENTRO_CRYSTAL_SEED) + "_from_dust");

        CrystalAggregatorRecipeBuilder.aggregating(AECSItems.ENTRO_CRYSTAL_SEED, 6, 16000)
                .require(EAESingletons.ENTRO_SEED, 2)
                .require(AECSTags.Items.DUST_QUARTZ, 4)
                .save(compatOut, "aggregator/" + getItemName(AECSItems.ENTRO_CRYSTAL_SEED) + "_from_original_seed");

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.EX_ASSEMBLER, 1, 32000)
                .require(AEBlocks.MOLECULAR_ASSEMBLER, 4)
                .require(AECSItems.RESONATING_PROCESSOR, 4)
                .require(AEItems.SPEED_CARD, 2)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.MACHINE_FRAME, 1, 32000)
                .require(EAETags.ENTRO_INGOT, 4)
                .require(AEBlocks.QUARTZ_GLASS, 1)
                .require(Tags.Items.INGOTS_IRON, 4)
                .save(compatOut, "aggregator/" + getItemName(EAESingletons.MACHINE_FRAME) + "_from_entro_ingot");

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.MACHINE_FRAME, 1, 32000)
                .require(AECSTags.Items.PURE_ENTRO_CRYSTAL, 4)
                .require(AEBlocks.QUARTZ_GLASS, 1)
                .require(Tags.Items.INGOTS_IRON, 4)
                .save(compatOut, "aggregator/" + getItemName(EAESingletons.MACHINE_FRAME) + "_from_pure_entro_crystal");

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.WIRELESS_HUB, 1, 32000)
                .require(AECSItems.RESONATING_PROCESSOR, 4)
                .require(AEBlocks.QUANTUM_LINK, 1)
                .require(EAESingletons.WIRELESS_CONNECTOR, 1)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(EAESingletons.CONCURRENT_PROCESSOR, 64, 144000)
                .require(EAESingletons.CONCURRENT_PROCESSOR_PRINT, 64)
                .require(Tags.Items.DUSTS_REDSTONE, 64)
                .require(AEItems.SILICON_PRINT, 64)
                .save(compatOut);

        CrystalAggregatorRecipeBuilder.aggregating(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK.toStack(), 16000)
                .require(EAESingletons.EX_PATTERN_PROVIDER, 1)
                .require(AECSItems.RESONATING_PROCESSOR, 1)
                .require(EAESingletons.EX_INTERFACE, 1)
                .save(compatOut, "aggregator/" + getItemName(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK) + "_from_extended_pattern_and_interface");

        // 扩展接口联动配方
        CrystalAssemblerRecipeBuilder.assemble(AECSBlocks.EX_ENDER_INTERFACE_BLOCK)
                .input(AECSBlocks.ENDER_INTERFACE_BLOCK)
                .input(AEItems.CAPACITY_CARD, 3)
                .input(Tags.Items.GLASS_BLOCKS, 3)
                .input(EAESingletons.CONCURRENT_PROCESSOR)
                .input(ConventionTags.GLASS_CABLE, 6)
                .save(compatOut, getCrystalAssemblerPath(AECSBlocks.EX_ENDER_INTERFACE_BLOCK));

        CrystalAssemblerRecipeBuilder.assemble(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK)
                .input(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK)
                .input(AEItems.CAPACITY_CARD, 3)
                .input(Blocks.CRAFTING_TABLE, 3)
                .input(EAESingletons.CONCURRENT_PROCESSOR)
                .input(ConventionTags.GLASS_CABLE, 6)
                .save(compatOut, getCrystalAssemblerPath(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK));

        CrystalAssemblerRecipeBuilder.assemble(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK)
                .input(AECSBlocks.INTEGRATED_INTERFACE_BLOCK)
                .input(AEItems.CAPACITY_CARD, 6)
                .input(Blocks.CRAFTING_TABLE, 6)
                .input(EAESingletons.CONCURRENT_PROCESSOR, 2)
                .input(ConventionTags.GLASS_CABLE, 12)
                .input(Tags.Items.GLASS_BLOCKS, 6)
                .save(compatOut, getCrystalAssemblerPath(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK));
    }

    protected static ResourceLocation getCrystalAssemblerPath(ItemLike output)
    {
        return AE2CrystalScience.makeId(getPrefixedItemName("crystal_assembler", output));
    }
}