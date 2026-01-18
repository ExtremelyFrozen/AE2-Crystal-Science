package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCraftRecipeProvider extends AECSRecipeProvider
{
    public AECSCraftRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Crafting Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        // 块配方
        pack2x2(recipeOutput, RecipeCategory.MISC, AECSItems.PURE_CERTUS_QUARTZ_CRYSTAL, AEBlocks.QUARTZ_BLOCK);
        pack2x2(recipeOutput, RecipeCategory.MISC, AECSItems.PURE_FLUIX_CRYSTAL, AEBlocks.FLUIX_BLOCK);
        pack2x2(recipeOutput, RecipeCategory.MISC, AECSItems.PURE_NETHER_QUARTZ_CRYSTAL, Blocks.QUARTZ_BLOCK);

        packAndUnpack3x3(recipeOutput, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_RESONATING_CRYSTAL, AECSBlocks.PURE_RESONATING_CRYSTAL_BLOCK);
        packAndUnpack3x3(recipeOutput, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_METEOR_CRYSTAL, AECSBlocks.PURE_METEOR_CRYSTAL_BLOCK);
        packAndUnpack3x3(recipeOutput, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_ENDER_QUARTZ, AECSBlocks.PURE_ENDER_QUARTZ_BLOCK);
        packAndUnpack3x3(recipeOutput, RecipeCategory.MISC, RecipeCategory.MISC,
                AEItems.SILICON, AECSBlocks.SILICON_BLOCK);

        // 工具配方
        toolPickaxeFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.METEOR_CRYSTAL_PICKAXE, AECSItems.PURE_METEOR_CRYSTAL);
        toolAxeFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.METEOR_CRYSTAL_AXE, AECSItems.PURE_METEOR_CRYSTAL);
        toolSwordFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.METEOR_CRYSTAL_SWORD, AECSItems.PURE_METEOR_CRYSTAL);
        toolHoeFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.METEOR_CRYSTAL_HOE, AECSItems.PURE_METEOR_CRYSTAL);
        toolShovelFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.METEOR_CRYSTAL_SHOVEL, AECSItems.PURE_METEOR_CRYSTAL);
        toolPickaxeFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.ENDER_CRYSTAL_PICKAXE, AECSItems.PURE_ENDER_QUARTZ);
        toolAxeFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.ENDER_CRYSTAL_AXE, AECSItems.PURE_ENDER_QUARTZ);
        toolSwordFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.ENDER_CRYSTAL_SWORD, AECSItems.PURE_ENDER_QUARTZ);
        toolHoeFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.ENDER_CRYSTAL_HOE, AECSItems.PURE_ENDER_QUARTZ);
        toolShovelFromItem(recipeOutput, RecipeCategory.MISC, AECSItems.ENDER_CRYSTAL_SHOVEL, AECSItems.PURE_ENDER_QUARTZ);

        // part<->方块互换配方
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.INTEGRATED_INTERFACE_BLOCK, AECSParts.INTEGRATE_INTERFACE_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK, AECSParts.EX_INTEGRATE_INTERFACE_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK, AECSParts.SIMPLE_PATTERN_PROVIDER_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, AECSParts.METEORITE_PATTERN_PROVIDER_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.ENDER_INTERFACE_BLOCK, AECSParts.ENDER_INTERFACE_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.EX_ENDER_INTERFACE_BLOCK, AECSParts.EX_ENDER_INTERFACE_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, AECSParts.RESONATING_PATTERN_PROVIDER_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK, AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART);
        swap1x1(recipeOutput, RecipeCategory.MISC, AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK, AECSParts.QUARTZ_OSCILLATOR_CLOCK_PART);

        // 种子配方
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.CERTUS_QUARTZ_SEED, 2)
                .requires(AEItems.CERTUS_QUARTZ_DUST)
                .requires(Blocks.SAND)
                .unlockedBy(getHasName(Blocks.SAND), has(Blocks.SAND))
                .save(recipeOutput, getCrafterPath(AECSItems.CERTUS_QUARTZ_SEED, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.FLUIX_CRYSTAL_SEED, 2)
                .requires(AEItems.FLUIX_DUST)
                .requires(Blocks.SAND)
                .unlockedBy(getHasName(Blocks.SAND), has(Blocks.SAND))
                .save(recipeOutput, getCrafterPath(AECSItems.FLUIX_CRYSTAL_SEED, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.NETHER_QUARTZ_SEED, 2)
                .requires(AECSTags.Items.DUST_QUARTZ)
                .requires(Blocks.SAND)
                .unlockedBy(getHasName(Blocks.SAND), has(Blocks.SAND))
                .save(recipeOutput, getCrafterPath(AECSItems.NETHER_QUARTZ_SEED, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.RESONATING_SEED)
                .requires(AECSTags.Items.DUST_RESONATING)
                .requires(ConventionTags.FLUIX_DUST)
                .requires(ConventionTags.SKY_STONE_DUST)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(AECSItems.RESONATING_DUST), has(AECSTags.Items.DUST_RESONATING))
                .save(recipeOutput, getCrafterPath(AECSItems.RESONATING_SEED, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.ENDER_QUARTZ_SEED, 1)
                .requires(ConventionTags.ENDER_PEARL_DUST)
                .requires(AECSTags.Items.DUST_QUARTZ)
                .requires(Blocks.SAND)
                .requires(Blocks.SAND)
                .unlockedBy(getHasName(Blocks.SAND), has(Blocks.SAND))
                .save(recipeOutput, getCrafterPath(AECSItems.ENDER_QUARTZ_SEED, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.METEOR_SEED)
                .requires(ConventionTags.SKY_STONE_DUST)
                .requires(ConventionTags.CERTUS_QUARTZ_DUST)
                .requires(Blocks.GRAVEL)
                .requires(Blocks.GRAVEL)
                .unlockedBy(getHasName(Blocks.GRAVEL), has(Blocks.GRAVEL))
                .save(recipeOutput, getCrafterPath(AECSItems.METEOR_SEED, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.crystalGrowthCard)
                .requires(AEItems.ADVANCED_CARD)
                .requires(AEBlocks.GROWTH_ACCELERATOR)
                .unlockedBy(getHasName(AEBlocks.GROWTH_ACCELERATOR), has(AEBlocks.GROWTH_ACCELERATOR))
                .save(recipeOutput, getCrafterPath(AECSItems.crystalGrowthCard, false));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSItems.BLANK_PRINT_PRESS)
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', AECSItems.SIMPLE_CIRCUIT_PRINT)
                .define('b', ConventionTags.INSCRIBER_PRESSES)
                .unlockedBy(getHasName(AECSItems.SIMPLE_CIRCUIT_PRINT), has(AECSItems.SIMPLE_CIRCUIT_PRINT))
                .save(recipeOutput, getCrafterPath(AECSItems.BLANK_PRINT_PRESS, true));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSItems.RESONATING_PRINT_PRESS)
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('b', AECSTags.Items.DUST_RESONATING)
                .define('c', AEItems.SILICON_PRESS)
                .unlockedBy(getHasName(AEItems.SILICON_PRESS), has(AEItems.SILICON_PRESS))
                .save(recipeOutput, getCrafterPath(AECSItems.RESONATING_PRINT_PRESS, true));

        // 各类升级
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.ENDER_INTERFACE_UPGRADE)
                .requires(AECSBlocks.ENDER_INTERFACE_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.ENDER_INTERFACE_BLOCK), has(AECSBlocks.ENDER_INTERFACE_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.ENDER_INTERFACE_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.EX_ENDER_INTERFACE_UPGRADE)
                .requires(AECSBlocks.EX_ENDER_INTERFACE_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.EX_ENDER_INTERFACE_BLOCK), has(AECSBlocks.EX_ENDER_INTERFACE_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.EX_ENDER_INTERFACE_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.INTEGRATED_INTERFACE_UPGRADE)
                .requires(AECSBlocks.INTEGRATED_INTERFACE_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.INTEGRATED_INTERFACE_BLOCK), has(AECSBlocks.INTEGRATED_INTERFACE_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.INTEGRATED_INTERFACE_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.EX_INTEGRATED_INTERFACE_UPGRADE)
                .requires(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK), has(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.EX_INTEGRATED_INTERFACE_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.RESONATING_PATTERN_PROVIDER_UPGRADE)
                .requires(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK), has(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.RESONATING_PATTERN_PROVIDER_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.EX_RESONATING_PATTERN_PROVIDER_UPGRADE)
                .requires(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK), has(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.EX_RESONATING_PATTERN_PROVIDER_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.PATTERN_PROVIDER_UPGRADE)
                .requires(AEBlocks.PATTERN_PROVIDER)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AEBlocks.PATTERN_PROVIDER), has(AEBlocks.PATTERN_PROVIDER))
                .save(recipeOutput, getCrafterPath(AECSItems.PATTERN_PROVIDER_UPGRADE, false));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AECSItems.METEOR_PATTERN_PROVIDER_UPGRADE)
                .requires(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK)
                .requires(Tags.Items.INGOTS)
                .unlockedBy(getHasName(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK), has(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK))
                .save(recipeOutput, getCrafterPath(AECSItems.METEOR_PATTERN_PROVIDER_UPGRADE, false));

        // 晶能聚合器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AEItems.FLUIX_PEARL)
                .define('b', AEBlocks.MOLECULAR_ASSEMBLER)
                .define('c', Tags.Items.DUSTS_REDSTONE)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', AEItems.LOGIC_PROCESSOR)
                .define('f', AEBlocks.CONDENSER)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_AGGREGATOR_BLOCK, true));

        // 电路切片机
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CIRCUIT_ETCHER_BLOCK)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ded")
                .define('a', AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .define('b', AEBlocks.INSCRIBER)
                .define('c', AECSItems.RESONATING_PROCESSOR)
                .define('d', AEItems.ENGINEERING_PROCESSOR)
                .define('e', AECSItems.BLANK_PRINT_PRESS)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CIRCUIT_ETCHER_BLOCK, true));

        // 催生仓
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .define('a', AEBlocks.GROWTH_ACCELERATOR)
                .define('b', AEBlocks.QUARTZ_CLUSTER)
                .define('c', AEBlocks.QUARTZ_GLASS)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', ConventionTags.GLASS_CABLE)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK, true));

        // 谐振仓
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AECSTags.Items.GEM_RESONATING)
                .define('b', AEBlocks.ENERGY_ACCEPTOR)
                .define('c', Tags.Items.INGOTS_IRON)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .define('f', AEBlocks.VIBRATION_CHAMBER)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK, true));

        // 粉碎机
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.CRYSTAL_PULVERIZER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .define('b', AEBlocks.INSCRIBER)
                .define('c', Tags.Items.INGOTS_IRON)
                .define('d', AECSBlocks.QUARTZ_GRINDSTONE_BLOCK)
                .define('e', AEItems.LOGIC_PROCESSOR)
                .define('f', AECSItems.RESONATING_PROCESSOR)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.CRYSTAL_PULVERIZER_BLOCK, true));

        // 石英磨具
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.QUARTZ_GRINDSTONE_BLOCK)
                .pattern("aba")
                .pattern("cac")
                .pattern("aca")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('b', AECSTags.Items.GEARS_WOOD)
                .define('c', ConventionTags.ALL_CERTUS_QUARTZ)
                .unlockedBy(getHasName(AECSItems.WOODEN_GEAR), has(AECSItems.WOODEN_GEAR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.QUARTZ_GRINDSTONE_BLOCK, true));

        // 熵变反应器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AECSTags.Items.GEM_SKY_STONE_CRYSTAL)
                .define('b', AEItems.ENTROPY_MANIPULATOR)
                .define('c', AEItems.ENGINEERING_PROCESSOR)
                .define('d', AEBlocks.CONDENSER)
                .define('e', Tags.Items.INGOTS_IRON)
                .define('f', AECSItems.RESONATING_PROCESSOR)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.ENTROPY_VARIATION_REACTION_CHAMBER_BLOCK, true));

        // 自装配式供应器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_METEOR_CRYSTAL)
                .define('b', AEBlocks.MOLECULAR_ASSEMBLER)
                .define('c', AEBlocks.PATTERN_PROVIDER)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .define('e', AEItems.SINGULARITY)
                .define('f', AEItems.CELL_COMPONENT_256K)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, true));

        // 初级样板供应器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('b', AECSTags.Items.GEARS_WOOD)
                .define('c', AECSItems.SIMPLE_PROCESSOR)
                .define('d', Items.CRAFTING_TABLE)
                .unlockedBy(getHasName(AECSItems.SIMPLE_PROCESSOR), has(AECSItems.SIMPLE_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK, true));

        // 末影广播装置
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.ENDER_BROADCASTER_BLOCK)
                .pattern("aba")
                .pattern("bcb")
                .pattern("ada")
                .define('a', AEItems.MATTER_BALL)
                .define('b', AEParts.ME_P2P_TUNNEL)
                .define('c', AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_RESONATING_CRYSTAL)
                .define('d', AECSItems.RESONATING_PROCESSOR)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.ENDER_BROADCASTER_BLOCK, true));

        // 末影发信器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.ENDER_EMITTER_BLOCK)
                .pattern("aba")
                .pattern(" c ")
                .pattern("ded")
                .define('a', Tags.Items.INGOTS_IRON)
                .define('b', AEBlocks.WIRELESS_ACCESS_POINT)
                .define('c', AECSItems.RESONATING_PROCESSOR)
                .define('d', ConventionTags.SMART_CABLE)
                .define('e', AECSTags.Items.STORAGE_BLOCK_PURE_CRYSTAL_ENDER_QUARTZ)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.ENDER_EMITTER_BLOCK, true));

        // 石英振荡器
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK)
                .pattern("aba")
                .pattern("cdc")
                .pattern("efe")
                .define('a', Blocks.COMPARATOR)
                .define('b', AEParts.LEVEL_EMITTER)
                .define('c', AEParts.TOGGLE_BUS)
                .define('d', AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK)
                .define('e', AECSItems.RESONATING_PROCESSOR)
                .define('f', Items.CLOCK)
                .unlockedBy(getHasName(AECSItems.RESONATING_PROCESSOR), has(AECSItems.RESONATING_PROCESSOR))
                .save(recipeOutput, getCrafterPath(AECSBlocks.QUARTZ_OSCILLATOR_CLOCK_BLOCK, true));
    }
}