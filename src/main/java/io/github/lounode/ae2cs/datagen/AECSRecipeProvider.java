package io.github.lounode.ae2cs.datagen;

import appeng.recipes.handlers.ChargerRecipeBuilder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.datagen.builder.recipe.CrystalAggregatorRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
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

    // 合成配方工具---------------------------------------------------------------

    /**
     * 1x1 双向互换
     */
    protected static void swap1x1(RecipeOutput out, RecipeCategory category, ItemLike a, ItemLike b)
    {
        swap1x1(out, category, category, a, b);
    }

    /**
     * 允许为两个方向分别指定RecipeCategory
     */
    protected static void swap1x1(RecipeOutput out,
                                  RecipeCategory aToBCategory,
                                  RecipeCategory bToACategory,
                                  ItemLike a,
                                  ItemLike b)
    {
        // A -> B
        ShapelessRecipeBuilder.shapeless(aToBCategory, b, 1)
                .requires(a)
                .unlockedBy(getHasName(a), has(a))
                .save(out, craftingConversionId(b, a));

        // B -> A
        ShapelessRecipeBuilder.shapeless(bToACategory, a, 1)
                .requires(b)
                .unlockedBy(getHasName(b), has(b))
                .save(out, craftingConversionId(a, b));
    }

    protected static void pack2x2(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike input,
            ItemLike output
    )
    {
        ShapedRecipeBuilder.shaped(category, output)
                .pattern("##")
                .pattern("##")
                .define('#', input)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, getCrafterPath(input, output, true));
    }

    protected static void unpackTo4(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike input,
            ItemLike output
    )
    {
        ShapelessRecipeBuilder.shapeless(category, output, 4)
                .requires(input)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, getCrafterPath(input, output, false));
    }

    protected static void packAndUnpack2x2(
            RecipeOutput recipeOutput,
            RecipeCategory packCategory,
            RecipeCategory unpackCategory,
            ItemLike loose,
            ItemLike packed
    )
    {
        pack2x2(recipeOutput, packCategory, loose, packed);
        unpackTo4(recipeOutput, unpackCategory, packed, loose);
    }


    protected static void pack3x3(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike input,
            ItemLike output
    )
    {
        ShapedRecipeBuilder.shaped(category, output)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', input)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, getCrafterPath(input, output, true));
    }

    protected static void unpackTo9(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike input,
            ItemLike output
    )
    {
        ShapelessRecipeBuilder.shapeless(category, output, 9)
                .requires(input)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, getCrafterPath(input, output, false));
    }

    protected static void packAndUnpack3x3(
            RecipeOutput recipeOutput,
            RecipeCategory packCategory,
            RecipeCategory unpackCategory,
            ItemLike loose,
            ItemLike packed
    )
    {
        pack3x3(recipeOutput, packCategory, loose, packed);
        unpackTo9(recipeOutput, unpackCategory, packed, loose);
    }

    // 五类工具辅助方法---------------------------------------------------------------
    // Pickaxe 镐：  ###
    //               |
    //               |
    protected static void toolPickaxeFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike material
    )
    {
        ShapedRecipeBuilder.shaped(category, result)
                .pattern("###")
                .pattern(" | ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    protected static void toolPickaxeFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> material
    )
    {
        String tagId = sanitize(material.location());
        String unlockName = "has_" + tagId;

        ShapedRecipeBuilder.shaped(category, result)
                .pattern("###")
                .pattern(" | ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(unlockName, has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    // Axe 斧：  ##
    //          #|
    //           |
    protected static void toolAxeFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike material
    )
    {
        ShapedRecipeBuilder.shaped(category, result)
                .pattern("## ")
                .pattern("#| ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    protected static void toolAxeFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> material
    )
    {
        String tagId = sanitize(material.location());
        String unlockName = "has_" + tagId;

        ShapedRecipeBuilder.shaped(category, result)
                .pattern("## ")
                .pattern("#| ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(unlockName, has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    // Sword 剑： #
    //           #
    //           |
    protected static void toolSwordFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike material
    )
    {
        ShapedRecipeBuilder.shaped(category, result)
                .pattern(" # ")
                .pattern(" # ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    protected static void toolSwordFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> material
    )
    {
        String tagId = sanitize(material.location());
        String unlockName = "has_" + tagId;

        ShapedRecipeBuilder.shaped(category, result)
                .pattern(" # ")
                .pattern(" # ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(unlockName, has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    // Hoe 锄：##
    //          |
    //          |
    protected static void toolHoeFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike material
    )
    {
        ShapedRecipeBuilder.shaped(category, result)
                .pattern("## ")
                .pattern(" | ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    protected static void toolHoeFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> material
    )
    {
        String tagId = sanitize(material.location());
        String unlockName = "has_" + tagId;

        ShapedRecipeBuilder.shaped(category, result)
                .pattern("## ")
                .pattern(" | ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(unlockName, has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    // Shovel 铲： #
    //            |
    //            |
    protected static void toolShovelFromItem(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            ItemLike material
    )
    {
        ShapedRecipeBuilder.shaped(category, result)
                .pattern(" # ")
                .pattern(" | ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(getHasName(material), has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    protected static void toolShovelFromTag(
            RecipeOutput recipeOutput,
            RecipeCategory category,
            ItemLike result,
            TagKey<Item> material
    )
    {
        String tagId = sanitize(material.location());
        String unlockName = "has_" + tagId;

        ShapedRecipeBuilder.shaped(category, result)
                .pattern(" # ")
                .pattern(" | ")
                .pattern(" | ")
                .define('#', material)
                .define('|', Tags.Items.RODS_WOODEN)
                .unlockedBy(unlockName, has(material))
                .save(recipeOutput, getCrafterPath(result, true));
    }

    // 锻造台辅助方法---------------------------------------------------------------
    protected static void smithingTransform(
            RecipeOutput out,
            RecipeCategory category,
            ItemLike template,
            ItemLike base,
            ItemLike addition,
            ItemLike result
    )
    {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(template),
                        Ingredient.of(base),
                        Ingredient.of(addition),
                        category,
                        result.asItem()
                )
                .unlocks(getHasName(base), has(base))
                .save(out, AE2CrystalScience.makeId(
                        "smithing/" + getItemName(result) + "_from_" + getItemName(base) + "_and_" + getItemName(addition)
                ));
    }


    // 熔炉样板辅助工具---------------------------------------------------------------
    protected static void smeltFood(RecipeCategory category, ItemLike input, ItemLike result, float experience, int cookingTime, RecipeOutput output)
    {
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(input), category, result, experience, cookingTime)
                .unlockedBy(getHasName(input), has(input))
                .save(output, AE2CrystalScience.makeId("smelt/food/" + getItemName(result) + "_from_" + getItemName(input)));
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
                .save(recipeOutput, AE2CrystalScience.makeId(recipeId));
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
                .save(recipeOutput, AE2CrystalScience.makeId(recipeId));
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

    // AE充能配方-------------------------------------------------------------------
    protected static void chargedRecipe(RecipeOutput consumer, ItemLike input, ItemLike output)
    {
        ResourceLocation id = AE2CrystalScience.makeId("charged/" + getItemName(input) + "_from_" + getItemName(output));
        ChargerRecipeBuilder.charge(consumer, id, input, output);
    }

    protected static void chargedRecipe(RecipeOutput consumer, TagKey<Item> input, ItemLike output)
    {
        String tagId = sanitize(input.location());

        ResourceLocation id = AE2CrystalScience.makeId("charged/" + getItemName(output) + "_from_" + tagId);
        ChargerRecipeBuilder.charge(consumer, id, input, output);
    }

    protected static void chargedRecipeWithAggregator(RecipeOutput consumer, ItemLike input, ItemLike output)
    {
        chargedRecipe(consumer, input, output);
        ResourceLocation id = AE2CrystalScience.makeId("aggregator/" + getItemName(input) + "_from_" + getItemName(output));
        CrystalAggregatorRecipeBuilder.aggregating(new ItemStack(output, 64), 102400)
                .require(input, 64)
                .save(consumer, id);
    }

    protected static void chargedRecipeWithAggregator(RecipeOutput consumer, TagKey<Item> input, ItemLike output)
    {
        chargedRecipe(consumer, input, output);
        String tagId = sanitize(input.location());
        ResourceLocation id = AE2CrystalScience.makeId("aggregator/" + getItemName(output) + "_from_" + tagId);
        CrystalAggregatorRecipeBuilder.aggregating(new ItemStack(output, 64), 102400)
                .require(input, 64)
                .save(consumer, id);
    }


    // id工具------------------------------------------------
    protected static ResourceLocation getCrafterPath(ItemLike output, boolean shaped)
    {
        String prefix = shaped ? "craft/shaped" : "craft/shapeless";
        return AE2CrystalScience.makeId(getPrefixedItemName(prefix, output));
    }

    protected static ResourceLocation getCrafterPath(ItemLike input, ItemLike output, boolean shaped)
    {
        String prefix = shaped ? "craft/shaped" : "craft/shapeless";
        String path = prefix + "/" + getItemName(output) + "_from_" + getItemName(input);
        return AE2CrystalScience.makeId(path);
    }

    /**
     * 给“互换”配方一个稳定的ID
     */
    private static ResourceLocation craftingConversionId(ItemLike result, ItemLike material)
    {
        return AE2CrystalScience.makeId("craft/shapeless/" + getConversionRecipeName(result, material) + "_swap");
    }

    protected static ResourceLocation getInscriberPath(ItemLike output)
    {
        return AE2CrystalScience.makeId(getPrefixedItemName("inscriber", output));
    }

    protected static ResourceLocation getTransformPath(ItemLike output)
    {
        return AE2CrystalScience.makeId(getPrefixedItemName("transform", output));
    }

    protected static String getPrefixedItemName(String prefix, ItemLike item)
    {
        return prefix + "/" + getItemName(item);
    }

    protected static String sanitize(ResourceLocation id)
    {
        // namespace:path/xxx -> namespace_path_xxx
        return id.getNamespace() + "_" + id.getPath().replace('/', '_');
    }

    // 其他工具
    public static ItemStack enchantedItem(HolderLookup.Provider registries, ItemLike item, int count, ResourceKey<Enchantment> enchantKey, int level)
    {
        return enchantedItem(registries, item, count, Map.of(enchantKey, level));
    }

    public static ItemStack enchantedItem(HolderLookup.Provider registries, ItemLike item, int count, Map<ResourceKey<Enchantment>, Integer> enchants)
    {
        ItemStack stack = new ItemStack(item, count);

        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (var e : enchants.entrySet())
        {
            var holder = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(e.getKey());
            int level = e.getValue() == null ? 1 : e.getValue();
            if (level > 0) mutable.set(holder, level);
        }

        if (stack.is(Items.ENCHANTED_BOOK))
        {
            stack.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        }
        else
        {
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        return stack;
    }
}
