package io.github.lounode.ae2cs.datagen.builder.recipe;

import com.google.gson.JsonObject;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.SizedIngredient;
import io.github.lounode.ae2cs.common.recipe.crystal_aggregator.CrystalAggregatorRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CrystalAggregatorRecipeBuilder implements RecipeBuilder
{

    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

    private final ItemStack result;
    private final int energyCost;
    private final List<SizedIngredient> inputs = new ArrayList<>(3);

    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();

    private final List<ItemPredicate.Builder> autoUnlockPredicates = new ArrayList<>(3);

    private CrystalAggregatorRecipeBuilder(ItemStack result, int energyCost)
    {
        this.result = result;
        this.energyCost = energyCost;
    }

    public static CrystalAggregatorRecipeBuilder aggregating(ItemStack result, int energyCost)
    {
        return new CrystalAggregatorRecipeBuilder(result, energyCost);
    }

    public static CrystalAggregatorRecipeBuilder aggregating(ItemLike result, int count, int energyCost)
    {
        return new CrystalAggregatorRecipeBuilder(new ItemStack(result, count), energyCost);
    }

    public CrystalAggregatorRecipeBuilder require(Ingredient ing, int count)
    {
        if (ing == null || ing.isEmpty() || count <= 0)
        {
            return this;
        }

        if (inputs.size() >= 3)
        {
            throw new IllegalStateException("CrystalAggregatorRecipe supports at most 3 inputs");
        }

        inputs.add(new SizedIngredient(ing, count));
        tryAddAutoUnlockFromIngredient(ing);
        return this;
    }

    public CrystalAggregatorRecipeBuilder require(ItemLike item, int count)
    {
        require(Ingredient.of(item), count);
        return this;
    }

    public CrystalAggregatorRecipeBuilder require(TagKey<Item> tag, int count)
    {
        require(Ingredient.of(tag), count);
        return this;
    }

    @Override
    public @NotNull CrystalAggregatorRecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance criterion)
    {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull CrystalAggregatorRecipeBuilder group(@Nullable String group)
    {
        return this;
    }

    @Override
    public @NotNull Item getResult()
    {
        return result.getItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> output, @NotNull ResourceLocation id)
    {
        Advancement.Builder adv = Advancement.Builder.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(RequirementsStrategy.OR);

        if (!this.criteria.isEmpty())
        {
            this.criteria.forEach(adv::addCriterion);
        }
        else
        {
            int idx = 0;
            Set<String> usedNames = new HashSet<>();

            for (ItemPredicate.Builder p : this.autoUnlockPredicates)
            {
                String name = uniqueCriterionName("has_input_" + idx++, usedNames);
                adv.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(p.build()));
            }

            if (this.autoUnlockPredicates.isEmpty())
            {
                adv.addCriterion("has_result", InventoryChangeTrigger.TriggerInstance.hasItems(this.result.getItem()));
            }
        }

        SizedIngredient a = !inputs.isEmpty() ? inputs.get(0) : EMPTY;
        SizedIngredient b = inputs.size() > 1 ? inputs.get(1) : EMPTY;
        SizedIngredient c = inputs.size() > 2 ? inputs.get(2) : EMPTY;

        var recipe = new CrystalAggregatorRecipe(id, a, b, c, result, energyCost);
        output.accept(new Result(id, recipe, adv, id.withPrefix("recipes/")));
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> recipeOutput, @NotNull String id)
    {
        save(recipeOutput, AE2CrystalScience.parseOrMakeId(id));
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> recipeOutput)
    {
        ResourceLocation path = AE2CrystalScience.makeId("aggregator/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath());
        save(recipeOutput, path);
    }

    private void addAutoUnlockPredicateForItem(Item item)
    {
        MinMaxBounds.Ints countBound = MinMaxBounds.Ints.atLeast(1);

        autoUnlockPredicates.add(
                ItemPredicate.Builder.item()
                        .of(item)
                        .withCount(countBound)
        );
    }

    private void tryAddAutoUnlockFromIngredient(Ingredient ing)
    {
        ItemStack[] stacks = ing.getItems();
        if (stacks.length == 0) return;

        List<Item> items = Arrays.stream(stacks)
                .filter(s -> s != null && !s.isEmpty())
                .map(ItemStack::getItem)
                .distinct()
                .toList();

        if (!items.isEmpty())
        {
            addAutoUnlockPredicateForItem(items.getFirst());
        }
    }

    private static String uniqueCriterionName(String base, Set<String> used)
    {
        String name = base;
        int i = 1;
        while (!used.add(name))
        {
            name = base + "_" + i++;
        }
        return name;
    }

    private static class Result implements FinishedRecipe
    {
        private final ResourceLocation id;
        private final CrystalAggregatorRecipe recipe;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        private Result(ResourceLocation id, CrystalAggregatorRecipe recipe,
                       Advancement.Builder advancement, ResourceLocation advancementId)
        {
            this.id = id;
            this.recipe = recipe;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject json)
        {
            json.addProperty("type", ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipe.getSerializer()).toString());
            json.add("input_a", recipe.inputA().toJson());
            json.add("input_b", recipe.inputB().toJson());
            json.add("input_c", recipe.inputC().toJson());

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", ForgeRegistries.ITEMS.getKey(recipe.result().getItem()).toString());
            if (recipe.result().getCount() != 1)
            {
                resultJson.addProperty("count", recipe.result().getCount());
            }
            json.add("result", resultJson);

            json.addProperty("energy_cost", recipe.energyCost());
        }

        @Override
        public @NotNull ResourceLocation getId()
        {
            return id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType()
        {
            return recipe.getSerializer();
        }

        @Override
        public @Nullable JsonObject serializeAdvancement()
        {
            return advancement.serializeToJson();
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId()
        {
            return advancementId;
        }
    }
}
