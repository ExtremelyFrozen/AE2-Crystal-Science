package io.github.lounode.ae2cs.datagen;

import appeng.core.definitions.AEItems;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.recipe.ResonatingPatternUpgradeRecipe;
import io.github.lounode.ae2cs.datagen.builder.recipe.CircuitEtcherRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalPulverizerRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSRecipeProvider extends RecipeProvider implements IConditionBuilder
{

    public AECSRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        // 这里是一个测试，用来测试电路蚀刻器的配方生成工作
        CircuitEtcherRecipeBuilder.etching(AEItems.CALCULATION_PROCESSOR, 64, 6400)
                .require(Items.REDSTONE, 64)
                .require(AEItems.SILICON, 64)
                .require(AECSItems.pureCertusQuartzCrystal, 64)
                .save(recipeOutput, AE2CrystalScience.makeId("circuit/calculation_processor"));

        // 晶能装配器测试配方
        CrystalAggregatorRecipeBuilder.aggregating(AEItems.SINGULARITY, 64, 6400)
                .require(AEItems.MATTER_BALL, 64)
                .require(AECSItems.pureCertusQuartzCrystal, 64)
                .save(recipeOutput, AE2CrystalScience.makeId("aggregating/singularity"));

        // 用于测试晶能粉碎机的配方生成
        CrystalPulverizerRecipeBuilder.pulverizing(AEItems.SILICON, 9, 6400)
                .require(Items.SAND, 1)
                .save(recipeOutput, AE2CrystalScience.makeId("pulverizer/silicon"));

        // 添加谐振样板配方
        SpecialRecipeBuilder.special(ResonatingPatternUpgradeRecipe::new)
                .save(recipeOutput, "resonating_pattern_upgrade");

        // 添加谐振样板拆解
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AEItems.BLANK_PATTERN)
                .requires(AECSItems.RESONATING_PATTERN)
                .unlockedBy("just_has_resonating_pattern", has(AECSItems.RESONATING_PATTERN))
                .save(recipeOutput);

    }

    // ---------- 统一ID工具 ----------

}
