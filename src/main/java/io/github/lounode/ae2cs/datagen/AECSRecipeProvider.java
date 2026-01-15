package io.github.lounode.ae2cs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
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
    public @NotNull String getName()
    {
        throw new IllegalStateException("Must be overridden");
    }

    // 熔炉样板辅助工具---------------------------------------------------------------
    protected static void smeltFood(RecipeCategory category, ItemLike input, ItemLike result, float experience, int cookingTime, RecipeOutput output)
    {
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(input), category, result, experience, cookingTime)
                .unlockedBy(getHasName(input), has(input))
                .save(output, "smelt/food/" + getItemName(result) + "_from_" + getItemName(input));
    }

    // 切石样板辅助工具---------------------------------------------------------------
    protected static void stonecutterResultFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike input,
            int resultCount
    )
    {
        Ingredient ingredient = Ingredient.of(input);
        String recipeId = "stonecutting/" + getItemName(result) + "_from_" + getItemName(input) + "_stonecutting";

        SingleItemRecipeBuilder.stonecutting(ingredient, category, result, resultCount)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, recipeId);
    }

    protected static void stonecutterResultFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike input
    )
    {
        stonecutterResultFromItem(recipeOutput, category, result, input, 1);
    }

    protected static void stonecutterResultFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> input,
            int resultCount
    )
    {
        Ingredient ingredient = Ingredient.of(input);

        // minecraft:logs -> "minecraft_logs"
        String tagId = sanitize(input.location());
        String unlockName = "has_" + tagId;
        String recipeId = "stonecutting/" + getItemName(result) + "_from_" + tagId + "_stonecutting";

        SingleItemRecipeBuilder.stonecutting(ingredient, category, result, resultCount)
                .unlockedBy(unlockName, has(input))
                .save(recipeOutput, recipeId);
    }

    protected static void stonecutterResultFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> input
    )
    {
        stonecutterResultFromTag(recipeOutput, category, result, input, 1);
    }

    // id工具------------------------------------------------
    protected static String getPrefixedItemName(String prefix, ItemLike item)
    {
        return prefix + "/" + getItemName(item);
    }

    protected static String sanitize(ResourceLocation id)
    {
        // namespace:path/xxx -> namespace_path_xxx
        return id.getNamespace() + "_" + id.getPath().replace('/', '_');
    }
}
