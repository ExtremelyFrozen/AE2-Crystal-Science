package io.github.lounode.ae2cs.datagen.builder.recipe;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.crystal_infuser.CrystalInfuserRecipe;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrystalInfuserRecipeBuilder implements RecipeBuilder {

    private static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

    private final ItemStack result;
    private final int energyCost;
    private final List<SizedIngredient> inputs = new ArrayList<>(4);
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final List<ItemPredicate.Builder> autoUnlockPredicates = new ArrayList<>(4);

    private CrystalInfuserRecipeBuilder(ItemStack result, int energyCost) {
        this.result = result;
        this.energyCost = energyCost;
    }

    public static CrystalInfuserRecipeBuilder infusing(ItemStack result, int energyCost) {
        return new CrystalInfuserRecipeBuilder(result, energyCost);
    }

    public static CrystalInfuserRecipeBuilder infusing(ItemLike result, int count, int energyCost) {
        return infusing(new ItemStack(result, count), energyCost);
    }

    public CrystalInfuserRecipeBuilder require(Ingredient ingredient, int count) {
        if (ingredient == null || ingredient.isEmpty() || count <= 0) return this;
        if (inputs.size() >= 4) {
            throw new IllegalStateException("CrystalInfuserRecipe supports at most 4 inputs");
        }
        inputs.add(new SizedIngredient(ingredient, count));
        ItemStack[] items = ingredient.getItems();
        if (items.length > 0 && !items[0].isEmpty()) {
            autoUnlockPredicates.add(ItemPredicate.Builder.item()
                    .of(items[0].getItem())
                    .withCount(MinMaxBounds.Ints.atLeast(1)));
        }
        return this;
    }

    public CrystalInfuserRecipeBuilder require(ItemLike item, int count) {
        return require(Ingredient.of(item), count);
    }

    public CrystalInfuserRecipeBuilder require(TagKey<Item> tag, int count) {
        return require(Ingredient.of(tag), count);
    }

    @Override
    public @NotNull CrystalInfuserRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull CrystalInfuserRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(RecipeOutput output, @NotNull ResourceLocation id) {
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        if (!criteria.isEmpty()) {
            criteria.forEach(advancement::addCriterion);
        } else {
            int index = 0;
            Set<String> usedNames = new HashSet<>();
            for (ItemPredicate.Builder predicate : autoUnlockPredicates) {
                String name = "has_input_" + index++;
                while (!usedNames.add(name)) name = "has_input_" + index++;
                advancement.addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(predicate));
            }
            if (autoUnlockPredicates.isEmpty()) {
                advancement.addCriterion("has_result", InventoryChangeTrigger.TriggerInstance.hasItems(result.getItem()));
            }
        }

        SizedIngredient a = inputs.size() > 0 ? inputs.get(0) : EMPTY;
        SizedIngredient b = inputs.size() > 1 ? inputs.get(1) : EMPTY;
        SizedIngredient c = inputs.size() > 2 ? inputs.get(2) : EMPTY;
        SizedIngredient d = inputs.size() > 3 ? inputs.get(3) : EMPTY;
        output.accept(id, new CrystalInfuserRecipe(a, b, c, d, result, energyCost), advancement.build(id.withPrefix("recipes/")));
    }

    @Override
    public void save(@NotNull RecipeOutput output, @NotNull String id) {
        save(output, AE2CrystalScience.parseOrMakeId(id));
    }

    @Override
    public void save(@NotNull RecipeOutput output) {
        save(output, AE2CrystalScience.makeId("infuser/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath()));
    }
}
