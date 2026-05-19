package io.github.lounode.ae2cs.datagen.builder.recipe;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.circuit_etcher.CircuitEtcherRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CircuitEtcherRecipeBuilder implements RecipeBuilder
{
    private final ItemStackTemplate result;
    private final int energyCost;
    private final List<SizedIngredient> inputs = new ArrayList<>(3);

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    // 用来自动写配方成就，在没有手写unlockBy条件时，会自动启用
    private final List<Item> autoUnlockItems = new ArrayList<>(3);

    private CircuitEtcherRecipeBuilder(ItemStackTemplate result, int energyCost)
    {
        this.result = result;
        this.energyCost = energyCost;
    }

    public static CircuitEtcherRecipeBuilder etching(ItemStackTemplate result, int energyCost)
    {
        return new CircuitEtcherRecipeBuilder(result, energyCost);
    }

    public static CircuitEtcherRecipeBuilder etching(ItemStack result, int energyCost)
    {
        return etching(ItemStackTemplate.fromNonEmptyStack(result), energyCost);
    }

    public static CircuitEtcherRecipeBuilder etching(ItemLike result, int count, int energyCost)
    {
        return new CircuitEtcherRecipeBuilder(new ItemStackTemplate(result.asItem(), count), energyCost);
    }

    /**
     * 添加一个输入（带数量），最多 3 个
     */
    public CircuitEtcherRecipeBuilder require(Ingredient ing, int count)
    {
        // 空/无效：不添加，不占位
        if (ing == null || count <= 0)
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
        require(ingredient(tag), count);
        return this;
    }

    @Override
    public @NotNull CircuitEtcherRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion)
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

    public @NotNull Item getResult()
    {
        return result.item().value();
    }

    @Override
    public @NotNull ResourceKey<Recipe<?>> defaultId()
    {
        return recipeKey(AE2CrystalScience.makeId("circuit_etcher/" + getItemName(getResult())));
    }

    @Override
    public void save(RecipeOutput output, @NotNull ResourceKey<Recipe<?>> id)
    {
        Identifier advancementId = id.identifier().withPrefix("recipes/");
        Advancement.Builder adv = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

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

            for (Item item : this.autoUnlockItems)
            {
                String name = uniqueCriterionName("has_input_" + idx++, usedNames);
                adv.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(item));
            }

            if (this.autoUnlockItems.isEmpty())
            {
                adv.addCriterion("has_result",
                        InventoryChangeTrigger.TriggerInstance.hasItems(this.result.item().value()));
            }
        }

        SizedIngredient a = inputs.size() > 0 ? inputs.get(0) : null;
        SizedIngredient b = inputs.size() > 1 ? inputs.get(1) : null;
        SizedIngredient c = inputs.size() > 2 ? inputs.get(2) : null;

        var recipe = new CircuitEtcherRecipe(a, b, c, result, energyCost);
        output.accept(id, recipe, adv.build(advancementId));
    }

    public void save(RecipeOutput output, @NotNull Identifier id)
    {
        save(output, recipeKey(id));
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id)
    {
        save(recipeOutput, AE2CrystalScience.parseOrMakeId(id));
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput)
    {
        save(recipeOutput, defaultId());
    }

    // 用于自动生成成就解锁条件
    private void addAutoUnlockItem(Item item)
    {
        autoUnlockItems.add(item);
    }

    /**
     * 对 require(Ingredient, count) 做一个尽力推导的自动解锁：Ingredient 始终选用第一个物品作为其解锁条件
     */
    private void tryAddAutoUnlockFromIngredient(Ingredient ing)
    {
        try
        {
            ing.items()
                    .map(holder -> holder.value())
                    .distinct()
                    .findFirst()
                    .ifPresent(this::addAutoUnlockItem);
        }
        catch (IllegalStateException | UnsupportedOperationException ignored)
        {
        }
    }

    private static Ingredient ingredient(TagKey<Item> tag)
    {
        return Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
    }

    private static ResourceKey<Recipe<?>> recipeKey(Identifier id)
    {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    private static String getItemName(Item item)
    {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
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
}
