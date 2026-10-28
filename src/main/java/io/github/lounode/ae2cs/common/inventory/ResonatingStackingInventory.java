package io.github.lounode.ae2cs.common.inventory;

import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntSupplier;

/** Keeps oversized machine stacks out of the vanilla ItemStack persistence codec. */
public class ResonatingStackingInventory extends AppEngInternalInventory {

    private final IntSupplier capacity;
    private final Runnable onChanged;

    public ResonatingStackingInventory(int size, IntSupplier capacity, Runnable onChanged) {
        super(size);
        this.capacity = capacity;
        this.onChanged = onChanged;
    }

    @Override
    public int getSlotLimit(int slot) {
        return capacity.getAsInt();
    }

    public int getItemLimit(ItemStack stack) {
        return stack.isStackable() ? stack.getMaxStackSize() * capacity.getAsInt() / 64 : 1;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return stack;
        int count = Math.min(stack.getCount(), Math.max(0, getItemLimit(stack) - current.getCount()));
        if (count == 0) return stack;
        if (!simulate) setItemDirect(slot, stack.copyWithCount(current.getCount() + count));
        return stack.copyWithCount(stack.getCount() - count);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        onChanged.run();
    }

    @Override
    public void writeToNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        ListTag items = new ListTag();
        for (int slot = 0; slot < size(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            CompoundTag item = new CompoundTag();
            item.putInt("Slot", slot);
            item.putInt("ae2cs_count", stack.getCount());
            items.add(stack.copyWithCount(1).save(registries, item));
        }
        data.put(name, items);
    }

    @Override
    public void readFromNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        clear();
        for (Tag entry : data.getList(name, Tag.TAG_COMPOUND)) {
            CompoundTag item = (CompoundTag) entry;
            int slot = item.getInt("Slot");
            if (slot < 0 || slot >= size()) continue;
            ItemStack stack = ItemStack.parseOptional(registries, item);
            if (!stack.isEmpty() && item.contains("ae2cs_count")) stack.setCount(Math.max(0, item.getInt("ae2cs_count")));
            setItemDirect(slot, stack);
        }
    }
}
