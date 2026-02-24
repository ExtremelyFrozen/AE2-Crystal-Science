package io.github.lounode.ae2cs.datagen.builder.recipe;

import com.google.gson.JsonObject;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.SizedIngredient;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
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

public class CircuitEtcherRecipeBuilder implements RecipeBuilder
{

    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

    private final ItemStack result;
    private final int energyCost;
    private final List<SizedIngredient> inputs = new ArrayList<>(3);

    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();

    // 用来自动写配方成就，在没有手写unlockBy条件时，会自动启用
    private final List<ItemPredicate.Builder> autoUnlockPredicates = new ArrayList<>(3);

    private CircuitEtcherRecipeBuilder(ItemStack result, int energyCost)
    {
        this.result = result;
        this.energyCost = energyCost;
    }

    public static CircuitEtcherRecipeBuilder etching(ItemStack result, int energyCost)
    {
        return new CircuitEtcherRecipeBuilder(result, energyCost);
    }

    public static CircuitEtcherRecipeBuilder etching(ItemLike result, int count, int energyCost)
    {
        return new CircuitEtcherRecipeBuilder(new ItemStack(result, count), energyCost);
    }

    /**
     * 添加一个输入（带数量），最多 3 个
     */
    public CircuitEtcherRecipeBuilder require(Ingredient ing, int count)
    {
        // 空/无效：不添加，不占位
        if (ing == null || ing.isEmpty() || count <= 0)
        {
            return this;
        }

        if (inputs.size() >= 3)
        {
            throw new IllegalStateException("CircuitEtcherRecipe supports at most 3 inputs");
        }

        inputs.add(new SizedIngredient(ing, count));
        tryAddAutoUnlockFromIngredient(ing);
        return this;
    }

    // 方便方法：item / tag
    public CircuitEtcherRecipeBuilder require(ItemLike item, int count)
    {
        require(Ingredient.of(item), count);
        return this;
    }

    public CircuitEtcherRecipeBuilder require(TagKey<Item> tag, int count)
    {
        require(Ingredient.of(tag), count);
        return this;
    }

    @Override
    public @NotNull CircuitEtcherRecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance criterion)
    {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull CircuitEtcherRecipeBuilder group(@Nullable String group)
    {
        // 这个 group 用于配方书分组。对这里没用，保留接口但不写入
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

        // 如果手写了 unlockedBy，就按手写的来
        if (!this.criteria.isEmpty())
        {
            this.criteria.forEach(adv::addCriterion);
        }
        else
        {
            // 否则自动生成：拥有任意一个输入即可解锁
            // 如果输入也推不出来，用结果物品兜底，避免 advancement 只有 has_the_recipe 导致循环解锁
            int idx = 0;
            Set<String> usedNames = new HashSet<>();

            for (ItemPredicate.Builder p : this.autoUnlockPredicates)
            {
                String name = uniqueCriterionName("has_input_" + idx++, usedNames);
                adv.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(p.build()));
            }

            if (this.autoUnlockPredicates.isEmpty())
            {
                adv.addCriterion("has_result",
                        InventoryChangeTrigger.TriggerInstance.hasItems(this.result.getItem()));
            }
        }

        // 填满到 3 个输入（缺省用 EMPTY）
        SizedIngredient a = inputs.size() > 0 ? inputs.get(0) : EMPTY;
        SizedIngredient b = inputs.size() > 1 ? inputs.get(1) : EMPTY;
        SizedIngredient c = inputs.size() > 2 ? inputs.get(2) : EMPTY;

        var recipe = new CircuitEtcherRecipe(id, a, b, c, result, energyCost);
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
        ResourceLocation path = AE2CrystalScience.makeId("circuit_etcher/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath());
        save(recipeOutput, path);
    }

    // 用于自动生成成就解锁条件
    private void addAutoUnlockPredicateForItem(Item item)
    {
        MinMaxBounds.Ints countBound = MinMaxBounds.Ints.atLeast(1);

        autoUnlockPredicates.add(
                ItemPredicate.Builder.item()
                        .of(item)
                        .withCount(countBound)
        );
    }

    /**
     * 对 require(Ingredient, count) 做一个尽力推导的自动解锁：Ingredient 始终选用第一个物品作为其解锁条件
     */
    private void tryAddAutoUnlockFromIngredient(Ingredient ing)
    {
        ItemStack[] stacks = ing.getItems();
        if (stacks.length == 0) return;

        // 去空
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
        private final CircuitEtcherRecipe recipe;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        private Result(ResourceLocation id, CircuitEtcherRecipe recipe,
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
