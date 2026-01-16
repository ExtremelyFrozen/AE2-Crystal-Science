package io.github.lounode.ae2cs.datagen.recipes.compat;

import com.simibubi.create.AllItems;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AECSCompatCreateRecipeProvider extends AECSRecipeProvider
{
    public AECSCompatCreateRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Create Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput originalOut)
    {
        var compatOut = originalOut.withConditions(modLoaded(AECSConstants.CREATE_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.pureRoseQuartz, AECSBlocks.PURE_ROSE_QUARTZ_BLOCK);

        // 高纯玫瑰水晶的唯一用处
        stonecutterResultFromItem(compatOut, RecipeCategory.MISC, AllItems.POLISHED_ROSE_QUARTZ, AECSItems.pureRoseQuartz, 2);
    }
}