package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.api.IngredientReplacer;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Set;

@Mixin(Ingredient.class)
public abstract class IngredientMixin
{
    @Shadow
    @Nullable
    private ItemStack[] itemStacks;

    @Shadow
    @Nullable
    private IntList stackingIds;

    /**
     * 每个Ingredient实例最多patch一次，避免反复扫描/扩容。
     */
    @Unique
    private boolean aecs_patchedOnce = false;

    @Inject(method = "getItems", at = @At("RETURN"), cancellable = true)
    private void aecs$expandGetItems(CallbackInfoReturnable<ItemStack[]> cir)
    {
        if (aecs_patchedOnce) return;
        aecs_patchedOnce = true;

        if (!IngredientReplacer.hasAnyRules()) return;

        final ItemStack[] originalItems = cir.getReturnValue();
        if (originalItems == null || originalItems.length == 0) return;

        // 将要新增的Item
        Item[] added = null;
        int addedCount = 0;

        for (ItemStack stack : originalItems)
        {
            if (stack == null || stack.isEmpty()) continue;

            Item target = stack.getItem();
            Set<Item> extras = IngredientReplacer.getRule(target);
            if (extras == null || extras.isEmpty()) continue;

            for (Item extra : extras)
            {
                if (extra == null) continue;
                if (extra == target) continue; // 避免 A->A 的无意义规则

                // 已经在原 items 里就跳过
                if (aecs$containsItem(originalItems, extra)) continue;

                // 已经在待新增列表里也跳过
                if (addedCount > 0 && aecs$containsItem(added, addedCount, extra)) continue;

                // 记录到待新增
                if (added == null)
                {
                    added = new Item[4];
                }
                else if (addedCount == added.length)
                {
                    added = Arrays.copyOf(added, addedCount * 2);
                }
                added[addedCount++] = extra;
            }
        }

        if (addedCount == 0) return;

        // 一次性扩容并追加
        ItemStack[] expanded = Arrays.copyOf(originalItems, originalItems.length + addedCount);
        int idx = originalItems.length;
        for (int i = 0; i < addedCount; i++)
        {
            expanded[idx++] = new ItemStack(added[i]);
        }

        // 写回缓存 + 作废 stackingIds
        this.itemStacks = expanded;
        this.stackingIds = null;

        cir.setReturnValue(expanded);
    }

    @Unique
    private static boolean aecs$containsItem(ItemStack[] items, Item item)
    {
        for (ItemStack stack : items)
        {
            if (stack != null && !stack.isEmpty() && stack.getItem() == item) return true;
        }
        return false;
    }

    @Unique
    private static boolean aecs$containsItem(Item[] items, int size, Item item)
    {
        for (int i = 0; i < size; i++)
        {
            if (items[i] == item) return true;
        }
        return false;
    }
}