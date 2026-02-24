/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.github.lounode.ae2cs.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 对于1.21.1 SizedIngredient的移植。
 * <p>
 * 为其添加了1.20.1可用的序列化和反序列化方法，并删除原CODEC和STREAM_CODEC部分
 */
public final class SizedIngredient
{

    public static SizedIngredient of(ItemLike item, int count)
    {
        return new SizedIngredient(Ingredient.of(item), count);
    }

    public static SizedIngredient of(TagKey<Item> tag, int count)
    {
        return new SizedIngredient(Ingredient.of(tag), count);
    }

    private final Ingredient ingredient;
    private final int count;
    @Nullable
    private ItemStack[] cachedStacks;

    public SizedIngredient(Ingredient ingredient, int count)
    {
        if (count <= 0)
        {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient ingredient()
    {
        return ingredient;
    }

    public int count()
    {
        return count;
    }

    public boolean test(ItemStack stack)
    {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    public ItemStack[] getItems()
    {
        if (cachedStacks == null)
        {
            cachedStacks = Stream.of(ingredient.getItems())
                    .map(s -> s.copyWithCount(count))
                    .toArray(ItemStack[]::new);
        }
        return cachedStacks;
    }

    public static SizedIngredient fromJson(JsonObject obj)
    {
        int count = GsonHelper.getAsInt(obj, "count", 1);
        JsonElement ingEl = GsonHelper.getNonNull(obj, "ingredient");
        Ingredient ing = Ingredient.fromJson(ingEl);
        return new SizedIngredient(ing, count);
    }

    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();
        obj.add("ingredient", ingredient.toJson()); // 注意返回 JsonElement
        obj.addProperty("count", count);
        return obj;
    }

    public static SizedIngredient fromNetwork(FriendlyByteBuf buf)
    {
        Ingredient ing = Ingredient.fromNetwork(buf);
        int count = buf.readVarInt();
        return new SizedIngredient(ing, count);
    }

    public void toNetwork(FriendlyByteBuf buf)
    {
        ingredient.toNetwork(buf);
        buf.writeVarInt(count);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SizedIngredient other)) return false;
        return count == other.count && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ingredient, count);
    }

    @Override
    public String toString()
    {
        return count + "x " + ingredient;
    }
}
