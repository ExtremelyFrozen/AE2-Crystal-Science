package io.github.lounode.ae2cs.common.inventory;

import io.github.lounode.ae2cs.common.init.AECSBlocks;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** A single stack of boosters, rather than one upgrade card per slot. */
public class EnderEmitterUpgradeInventory extends AppEngInternalInventory implements IUpgradeInventory {

    private final Runnable onChanged;

    public EnderEmitterUpgradeInventory(Runnable onChanged) {
        super(null, 1, 64);
        this.onChanged = onChanged;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && AEItems.WIRELESS_BOOSTER.is(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        onChanged.run();
    }

    @Override
    public ItemLike getUpgradableItem() {
        return AECSBlocks.ENDER_EMITTER_BLOCK;
    }

    @Override
    public int getInstalledUpgrades(ItemLike upgrade) {
        ItemStack stack = getStackInSlot(0);
        return upgrade.asItem() == AEItems.WIRELESS_BOOSTER.asItem() && AEItems.WIRELESS_BOOSTER.is(stack) ? Math.min(64, stack.getCount()) : 0;
    }

    @Override
    public int getMaxInstalled(ItemLike upgrade) {
        return upgrade.asItem() == AEItems.WIRELESS_BOOSTER.asItem() ? 64 : 0;
    }
}
