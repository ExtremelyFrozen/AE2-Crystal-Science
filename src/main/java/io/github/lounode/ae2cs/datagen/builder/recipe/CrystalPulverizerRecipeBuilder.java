package io.github.lounode.ae2cs.datagen.builder.recipe;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.crystal_pulverizer.CrystalPulverizerRecipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CrystalPulverizerRecipeBuilder implements RecipeBuilder {

    private final ItemStack result;
    private final int energyCost;

    private @Nullable SizedIngredient input = null;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    // 自动成就解锁：如果没有手写 unlockedBy，则自动从输入推导
    private final List<ItemPredicate.Builder> autoUnlockPredicates = new ArrayList<>(1);

    private CrystalPulverizerRecipeBuilder(ItemStack result, int energyCost) {
        this.result = result;
        this.energyCost = energyCost;
    }

    public static CrystalPulverizerRecipeBuilder pulverizing(ItemStack result, int energyCost) {
        return new CrystalPulverizerRecipeBuilder(result, energyCost);
    }

    public static CrystalPulverizerRecipeBuilder pulverizing(ItemLike result, int count, int energyCost) {
        return new CrystalPulverizerRecipeBuilder(new ItemStack(result, count), energyCost);
    }

    /**
     * 设置输入（带数量）
     */
    public CrystalPulverizerRecipeBuilder require(Ingredient ing, int count) {
        if (ing == null || ing.isEmpty() || count <= 0) {
            return this;
        }

        this.input = new SizedIngredient(ing, count);
        this.autoUnlockPredicates.clear();
        tryAddAutoUnlockFromIngredient(ing);
        return this;
    }

    // 方便方法：item / tag
    public CrystalPulverizerRecipeBuilder require(ItemLike item, int count) {
        return require(Ingredient.of(item), count);
    }

    public CrystalPulverizerRecipeBuilder require(TagKey<Item> tag, int count) {
        return require(Ingredient.of(tag), count);
    }

    @Override
    public @NotNull CrystalPulverizerRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull CrystalPulverizerRecipeBuilder group(@Nullable String group) {
        // 配方书分组；此处不写入，保留接口
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(@NotNull RecipeOutput output, @NotNull ResourceLocation id) {
        if (this.input == null || this.input.ingredient().isEmpty() || this.input.count() <= 0) {
            throw new IllegalStateException("CrystalPulverizerRecipe requires exactly 1 valid input: " + id);
        }
        if (this.energyCost <= 0) {
            throw new IllegalStateException("CrystalPulverizerRecipe time must be positive: " + id);
        }
        if (this.result.isEmpty()) {
            throw new IllegalStateException("CrystalPulverizerRecipe result cannot be empty: " + id);
        }

        Advancement.Builder adv = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        // 如果手写了 unlockedBy，就按手写的来
        if (!this.criteria.isEmpty()) {
            this.criteria.forEach(adv::addCriterion);
        } else {
            // 否则自动生成：拥有输入即可解锁；推不出来就用结果兜底
            Set<String> usedNames = new HashSet<>();
            if (!this.autoUnlockPredicates.isEmpty()) {
                int idx = 0;
                for (ItemPredicate.Builder p : this.autoUnlockPredicates) {
                    String name = uniqueCriterionName("has_input_" + idx++, usedNames);
                    adv.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(p));
                }
            } else {
                adv.addCriterion("has_result",
                        InventoryChangeTrigger.TriggerInstance.hasItems(this.result.getItem()));
            }
        }

        var recipe = new CrystalPulverizerRecipe(this.input, this.result, this.energyCost);
        output.accept(id, recipe, adv.build(id.withPrefix("recipes/")));
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
        save(recipeOutput, AE2CrystalScience.parseOrMakeId(id));
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput) {
        ResourceLocation path = AE2CrystalScience.makeId("pulverizer/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath());
        save(recipeOutput, path);
    }

    // 用于自动生成成就解锁条件
    private void addAutoUnlockPredicateForItem(Item item) {
        MinMaxBounds.Ints countBound = MinMaxBounds.Ints.atLeast(1);

        autoUnlockPredicates.add(
                ItemPredicate.Builder.item()
                        .of(item)
                        .withCount(countBound));
    }

    /**
     * 尽力推导自动解锁：Ingredient 取第一个非空物品作为解锁条件
     */
    private void tryAddAutoUnlockFromIngredient(Ingredient ing) {
        ItemStack[] stacks = ing.getItems();
        if (stacks.length == 0) return;

        List<Item> items = Arrays.stream(stacks)
                .filter(s -> s != null && !s.isEmpty())
                .map(ItemStack::getItem)
                .distinct()
                .toList();

        if (!items.isEmpty()) {
            addAutoUnlockPredicateForItem(items.getFirst());
        }
    }

    private static String uniqueCriterionName(String base, Set<String> used) {
        String name = base;
        int i = 1;
        while (!used.add(name)) {
            name = base + "_" + i++;
        }
        return name;
    }
}
