package io.github.lounode.ae2cs.datagen.builder.recipe;

import com.google.gson.JsonObject;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.SizedIngredient;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;
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

import java.util.*;
import java.util.function.Consumer;

public class CrystalPulverizerRecipeBuilder implements RecipeBuilder
{
    private final ItemStack result;
    private final int energyCost;

    private @Nullable SizedIngredient input = null;

    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();

    private final List<ItemPredicate.Builder> autoUnlockPredicates = new java.util.ArrayList<>(1);

    private CrystalPulverizerRecipeBuilder(ItemStack result, int energyCost)
    {
        this.result = result;
        this.energyCost = energyCost;
    }

    public static CrystalPulverizerRecipeBuilder pulverizing(ItemStack result, int energyCost)
    {
        return new CrystalPulverizerRecipeBuilder(result, energyCost);
    }

    public static CrystalPulverizerRecipeBuilder pulverizing(ItemLike result, int count, int energyCost)
    {
        return new CrystalPulverizerRecipeBuilder(new ItemStack(result, count), energyCost);
    }

    public CrystalPulverizerRecipeBuilder require(Ingredient ing, int count)
    {
        if (ing == null || ing.isEmpty() || count <= 0)
        {
            return this;
        }

        this.input = new SizedIngredient(ing, count);
        this.autoUnlockPredicates.clear();
        tryAddAutoUnlockFromIngredient(ing);
        return this;
    }

    public CrystalPulverizerRecipeBuilder require(ItemLike item, int count)
    {
        return require(Ingredient.of(item), count);
    }

    public CrystalPulverizerRecipeBuilder require(TagKey<Item> tag, int count)
    {
        return require(Ingredient.of(tag), count);
    }

    @Override
    public @NotNull CrystalPulverizerRecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance criterion)
    {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull CrystalPulverizerRecipeBuilder group(@Nullable String group)
    {
        return this;
    }

    @Override
    public @NotNull Item getResult()
    {
        return result.getItem();
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> output, @NotNull ResourceLocation id)
    {
        if (this.input == null || this.input.ingredient().isEmpty() || this.input.count() <= 0)
        {
            throw new IllegalStateException("CrystalPulverizerRecipe requires exactly 1 valid input: " + id);
        }
        if (this.energyCost <= 0)
        {
            throw new IllegalStateException("CrystalPulverizerRecipe time must be positive: " + id);
        }
        if (this.result.isEmpty())
        {
            throw new IllegalStateException("CrystalPulverizerRecipe result cannot be empty: " + id);
        }

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
            Set<String> usedNames = new HashSet<>();
            if (!this.autoUnlockPredicates.isEmpty())
            {
                int idx = 0;
                for (ItemPredicate.Builder p : this.autoUnlockPredicates)
                {
                    String name = uniqueCriterionName("has_input_" + idx++, usedNames);
                    adv.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(p.build()));
                }
            }
            else
            {
                adv.addCriterion("has_result", InventoryChangeTrigger.TriggerInstance.hasItems(this.result.getItem()));
            }
        }

        var recipe = new CrystalPulverizerRecipe(id, this.input, this.result, this.energyCost);
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
        ResourceLocation path = AE2CrystalScience.makeId("pulverizer/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath());
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
            addAutoUnlockPredicateForItem(items.get(0));
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
        private final CrystalPulverizerRecipe recipe;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        private Result(ResourceLocation id, CrystalPulverizerRecipe recipe,
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
            json.add("input", recipe.input().toJson());

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
