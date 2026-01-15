package io.github.lounode.ae2cs.datagen.recipes;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSMiscRecipeProvider extends AECSRecipeProvider
{
    public AECSMiscRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Misc Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput)
    {
        // 爆炸以获得谐振水晶粉
        TransformRecipeBuilder.transform(recipeOutput, getTransformPath(AECSItems.RESONATING_DUST),
                AECSItems.REDSTONE_CRYSTAL_DUST, 1,
                TransformCircumstance.EXPLOSION,
                AECSItems.pureMeteorCrystal,
                Blocks.REDSTONE_BLOCK,
                AECSItems.pureEnderQuartz);

        // 压印系列
        InscriberRecipeBuilder.inscribe(AECSTags.Items.GEM_RESONATING, AECSItems.RESONATING_CIRCUIT_PRINT, 1)
                .setTop(Ingredient.of(AECSItems.RESONATING_PRINT_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, getInscriberPath(AECSItems.RESONATING_CIRCUIT_PRINT));

        InscriberRecipeBuilder.inscribe(ConventionTags.SKY_STONE_DUST, AECSItems.RESONATING_PROCESSOR, 1)
                .setTop(Ingredient.of(AECSTags.Items.GEM_RESONATING))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.RESONATING_PROCESSOR));

        InscriberRecipeBuilder.inscribe(Tags.Items.GEMS_QUARTZ, AECSItems.SIMPLE_CIRCUIT_PRINT, 1)
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, getInscriberPath(AECSItems.SIMPLE_CIRCUIT_PRINT));
    }
}
