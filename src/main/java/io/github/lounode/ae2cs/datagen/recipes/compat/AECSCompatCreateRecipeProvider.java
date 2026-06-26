package io.github.lounode.ae2cs.datagen.recipes.compat;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.datagen.AECSRecipeProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AECSCompatCreateRecipeProvider extends AECSRecipeProvider {

    public AECSCompatCreateRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public @NotNull String getName() {
        return "AECS Create Compat Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> originalOut) {
        var compatOut = withConditions(originalOut, modLoaded(AECSConstants.CREATE_ID));

        packAndUnpack3x3(compatOut, RecipeCategory.MISC, RecipeCategory.MISC,
                AECSItems.PURE_ROSE_QUARTZ, AECSBlocks.PURE_ROSE_QUARTZ_BLOCK);

        // 高纯玫瑰水晶的唯一用处
        stonecutterResultFromTag(compatOut, RecipeCategory.MISC, AllItems.POLISHED_ROSE_QUARTZ, AECSTags.Items.PURE_ROSE_QUARTZ, 2);

        MechanicalCraftingRecipeBuilder.shapedRecipe(AECSItems.ROSE_QUARTZ_SEED)
                .patternLine("a")
                .key('a', AllItems.POLISHED_ROSE_QUARTZ)
                .build(compatOut, getMechanicalCraftingPath(AECSItems.ROSE_QUARTZ_SEED));
    }

    protected static ResourceLocation getMechanicalCraftingPath(ItemLike output) {
        return AE2CrystalScience.makeId(getPrefixedItemName("mechanical_crafting", output));
    }
}
