package io.github.lounode.ae2cs.common.inventory;

import io.github.lounode.ae2cs.common.init.AECSItems;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.IntPredicate;

/** Six shared slots with independent limits for each factory upgrade. */
public class ResonatingFactoryUpgradeInventory extends AppEngInternalInventory implements IUpgradeInventory {

    private final Runnable onChanged;
    private final IntPredicate canRemoveStackingCard;
    private final ItemLike machine;

    public ResonatingFactoryUpgradeInventory(ItemLike machine, Runnable onChanged, IntPredicate canRemoveStackingCard) {
        super(null, 6, 1);
        this.machine = machine;
        this.onChanged = onChanged;
        this.canRemoveStackingCard = canRemoveStackingCard;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot < 0 || slot >= size() || stack.isEmpty()) return false;
        var item = stack.getItem();
        int max = getMaxInstalled(item);
        int installed = getInstalledUpgrades(item);
        return max > 0 && (getStackInSlot(slot).is(item) || installed < max);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (getStackInSlot(slot).is(AECSItems.RESONATING_STACKING_CARD) && !canRemoveStackingCard.test(getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD) - 1)) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        onChanged.run();
    }

    @Override
    public ItemLike getUpgradableItem() {
        return machine;
    }

    @Override
    public int getInstalledUpgrades(ItemLike upgrade) {
        int count = 0;
        for (int slot = 0; slot < size(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.is(upgrade.asItem())) count++;
        }
        return count;
    }

    @Override
    public int getMaxInstalled(ItemLike upgrade) {
        if (upgrade.asItem() == AECSItems.OVERLOAD_CARD.asItem()) return 4;
        return upgrade.asItem() == AECSItems.RESONATING_STACKING_CARD.asItem() ? 2 : 0;
    }
}
