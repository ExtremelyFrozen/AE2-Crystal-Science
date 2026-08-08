package io.github.lounode.ae2cs.datagen.builder.recipe;

import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.recipe.pulse_centrifuge.PulseCentrifugeRecipe;

import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PulseCentrifugeRecipeBuilder implements RecipeBuilder {

    private final List<ItemStack> results = new ArrayList<>(4);
    private final int energyCost;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private @Nullable SizedIngredient input;

    private PulseCentrifugeRecipeBuilder(ItemStack result, int energyCost) {
        this.results.add(result);
        this.energyCost = energyCost;
    }

    public static PulseCentrifugeRecipeBuilder separating(ItemStack result, int energyCost) {
        return new PulseCentrifugeRecipeBuilder(result, energyCost);
    }

    public static PulseCentrifugeRecipeBuilder separating(ItemLike result, int count, int energyCost) {
        return separating(new ItemStack(result, count), energyCost);
    }

    public PulseCentrifugeRecipeBuilder require(Ingredient ingredient, int count) {
        if (ingredient != null && !ingredient.isEmpty() && count > 0) {
            input = new SizedIngredient(ingredient, count);
        }
        return this;
    }

    public PulseCentrifugeRecipeBuilder require(ItemLike item, int count) {
        return require(Ingredient.of(item), count);
    }

    public PulseCentrifugeRecipeBuilder require(TagKey<Item> tag, int count) {
        return require(Ingredient.of(tag), count);
    }

    public PulseCentrifugeRecipeBuilder addResult(ItemLike result, int count) {
        return addResult(new ItemStack(result, count));
    }

    public PulseCentrifugeRecipeBuilder addResult(ItemStack result) {
        if (results.size() >= 4) {
            throw new IllegalStateException("PulseCentrifugeRecipe supports at most 4 results");
        }
        results.add(result);
        return this;
    }

    @Override
    public @NotNull PulseCentrifugeRecipeBuilder unlockedBy(@NotNull String name,
                                                            @NotNull Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull PulseCentrifugeRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return results.getFirst().getItem();
    }

    @Override
    public void save(@NotNull RecipeOutput output, @NotNull ResourceLocation id) {
        if (input == null) {
            throw new IllegalStateException("PulseCentrifugeRecipe requires one input: " + id);
        }

        var advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        if (criteria.isEmpty()) {
            ItemStack[] matchingStacks = input.ingredient().getItems();
            Item unlockItem = matchingStacks.length > 0 ? matchingStacks[0].getItem() : getResult();
            advancement.addCriterion("has_input", InventoryChangeTrigger.TriggerInstance.hasItems(unlockItem));
        } else {
            criteria.forEach(advancement::addCriterion);
        }

        output.accept(id, new PulseCentrifugeRecipe(input, results, energyCost),
                advancement.build(id.withPrefix("recipes/")));
    }

    @Override
    public void save(@NotNull RecipeOutput output, @NotNull String id) {
        save(output, AE2CrystalScience.parseOrMakeId(id));
    }

    @Override
    public void save(@NotNull RecipeOutput output) {
        save(output, AE2CrystalScience.makeId(
                "pulse_centrifuge/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath()));
    }
}
