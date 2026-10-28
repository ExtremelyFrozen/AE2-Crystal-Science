package io.github.lounode.ae2cs.common.inventory;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.IntPredicate;

/** Four dedicated meteorite slots followed by two dedicated stacking slots. */
public class ResonatingFactoryUpgradeInventory extends AppEngInternalInventory implements IUpgradeInventory {

    private final Runnable onChanged;
    private final IntPredicate canRemoveStackingCard;

    public ResonatingFactoryUpgradeInventory(Runnable onChanged, IntPredicate canRemoveStackingCard) {
        super(null, 6, 1);
        this.onChanged = onChanged;
        this.canRemoveStackingCard = canRemoveStackingCard;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot >= 0 && slot < size() && stack.is(slot < 4 ? AECSItems.OVERLOAD_CARD : AECSItems.RESONATING_STACKING_CARD);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= 4 && !getStackInSlot(slot).isEmpty()
                && !canRemoveStackingCard.test(getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD) - 1)) {
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
        return AECSBlocks.RESONATING_AGGREGATOR_FACTORY_BLOCK;
    }

    @Override
    public int getInstalledUpgrades(ItemLike upgrade) {
        int count = 0;
        for (int slot = 0; slot < size(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (isItemValid(slot, stack) && stack.is(upgrade.asItem())) count++;
        }
        return count;
    }

    @Override
    public int getMaxInstalled(ItemLike upgrade) {
        if (upgrade.asItem() == AECSItems.OVERLOAD_CARD.asItem()) return 4;
        return upgrade.asItem() == AECSItems.RESONATING_STACKING_CARD.asItem() ? 2 : 0;
    }
}
