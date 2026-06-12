package io.github.lounode.ae2cs.common.me.menuhost;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.item.ResonatingPatternConverterItem;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.items.contents.StackDependentSupplier;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.SupplierInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class ResonatingPatternConverterMenuHost extends ItemMenuHost<ResonatingPatternConverterItem> {

    private final SupplierInternalInventory<InternalInventory> inventory;

    public ResonatingPatternConverterMenuHost(ResonatingPatternConverterItem item, Player player, ItemMenuHostLocator locator) {
        super(item, player, locator);

        this.inventory = new SupplierInternalInventory<>(
                new StackDependentSupplier<>(
                        this::getItemStack,
                        stack -> createInventory(player, stack)));
    }

    private static InternalInventory createInventory(Player player, ItemStack stack) {
        AppEngInternalInventory inventory = new AppEngInternalInventory(new InternalInventoryHost() {

            @Override
            public void saveChangedInventory(AppEngInternalInventory inv) {
                stack.set(AECSDataComponents.RESONATING_CONVERTER_INV, inv.toItemContainerContents());
            }

            @Override
            public boolean isClientSide() {
                return player.level().isClientSide();
            }
        }, 18);
        inventory.fromItemContainerContents(stack.getOrDefault(AECSDataComponents.RESONATING_CONVERTER_INV, ItemContainerContents.EMPTY));

        for (int i = 0; i < inventory.size(); i++) {
            inventory.setMaxStackSize(i, 1);
        }

        inventory.setFilter(new IAEItemFilter() {

            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                return IAEItemFilter.super.allowInsert(inv, slot, stack) &&
                        (stack.getItem() == AECSItems.RESONATING_PATTERN.asItem() || stack.getItem() == AEItems.PROCESSING_PATTERN.asItem());
            }
        });
        return inventory;
    }

    public InternalInventory getInventory() {
        return inventory;
    }

    @Override
    protected double getPowerDrainPerTick() {
        return 0;
    }
}
