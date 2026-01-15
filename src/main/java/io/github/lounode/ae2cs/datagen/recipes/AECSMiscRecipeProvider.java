package io.github.lounode.ae2cs.datagen.recipes;

import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.block.Blocks;
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
        TransformRecipeBuilder.transform(recipeOutput, AE2CrystalScience.makeId(getPrefixedItemName("transform", AECSItems.REDSTONE_CRYSTAL_DUST)),
                AECSItems.REDSTONE_CRYSTAL_DUST, 1,
                TransformCircumstance.EXPLOSION,
                AECSItems.pureMeteorCrystal,
                Blocks.REDSTONE_BLOCK,
                AECSItems.pureEnderQuartz);
    }
}
