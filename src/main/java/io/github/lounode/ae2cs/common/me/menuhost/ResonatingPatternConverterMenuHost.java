package io.github.lounode.ae2cs.common.me.menuhost;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.AECSItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ResonatingPatternConverterMenuHost extends ItemMenuHost implements InternalInventoryHost
{
    private final AppEngInternalInventory inventory = new AppEngInternalInventory(18);

    public ResonatingPatternConverterMenuHost(Player player, @Nullable Integer slot, ItemStack is)
    {
        super(player, slot, is);

        // 初始化仓库
        CompoundTag nbt = is.getTag();
        if (nbt != null)
        {
            this.inventory.readFromNBT(nbt, AECSDataComponents.TAG_RESONATING_CONVERTER_INV);
        }
        for (int i = 0; i < this.inventory.size(); i++)
        {
            this.inventory.setMaxStackSize(i, 1);
        }
        inventory.setFilter(new IAEItemFilter()
        {
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
            {
                return IAEItemFilter.super.allowInsert(inv, slot, stack) &&
                        (stack.getItem() == AECSItems.RESONATING_PATTERN.get()
                                || stack.getItem() == AEItems.PROCESSING_PATTERN.asItem());
            }
        });

        setPowerDrainPerTick(0);
    }

    @Override
    public void saveChanges()
    {
        CompoundTag nbt = this.getItemStack().getOrCreateTag();
        this.inventory.writeToNBT(nbt, AECSDataComponents.TAG_RESONATING_CONVERTER_INV);
    }

    @Override
    public void onChangeInventory(InternalInventory internalInventory, int i)
    {
        CompoundTag nbt = this.getItemStack().getOrCreateTag();
        if (internalInventory == this.inventory)
        {
            this.inventory.writeToNBT(nbt, AECSDataComponents.TAG_RESONATING_CONVERTER_INV);
        }
    }

    public InternalInventory getInventory()
    {
        return inventory;
    }
}
