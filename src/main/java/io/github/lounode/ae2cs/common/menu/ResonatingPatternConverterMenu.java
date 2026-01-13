package io.github.lounode.ae2cs.common.menu;

import appeng.api.ids.AEComponents;
import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import io.github.lounode.ae2cs.common.me.menuhost.ResonatingPatternConverterMenuHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ResonatingPatternConverterMenu extends UpgradeableMenu<ResonatingPatternConverterMenuHost>
{
    private static final String converterPattern = "converter_pattern";
    private static final int LEFT_BOUNDARY = 9;

    public ResonatingPatternConverterMenu(MenuType<?> menuType, int id, Inventory playerInventory, ResonatingPatternConverterMenuHost host)
    {
        super(menuType, id, playerInventory, host);

        registerClientAction(converterPattern, this::onConverterPattern);
    }

    public void onConverterPattern()
    {
        if (isClientSide())
        {
            sendClientAction(converterPattern);
        }
        else
        {
            // 这里样板插入仓库的最大堆叠量始终为1，因此可以在回设槽位时轻松一些
            InternalInventory inv = getHost().getInventory();
            for (int i = 0; i < LEFT_BOUNDARY && i < inv.size(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.isEmpty()) continue;

                // 谐振样板直接转移至右侧，也许以后可以加一个用于清空位置信息的功能，不过我现在懒得写了
                if (stack.getItem() == AECSItems.RESONATING_PATTERN.asItem())
                {
                    ItemStack remaining = insertToRange(inv, stack, LEFT_BOUNDARY, inv.size(), false);
                    if (remaining.isEmpty())
                    {
                        inv.setItemDirect(i, ItemStack.EMPTY);
                    }
                    continue;
                }

                if (stack.getItem() == AEItems.PROCESSING_PATTERN.asItem() && stack.has(AEComponents.ENCODED_PROCESSING_PATTERN))
                {
                    ItemStack resonating = ResonatingPatternDetails.encode(stack);
                    if (resonating.isEmpty()) continue;

                    ItemStack remaining = insertToRange(inv, resonating, LEFT_BOUNDARY, inv.size(), false);
                    if (remaining.isEmpty())
                    {
                        inv.setItemDirect(i, ItemStack.EMPTY);
                    }
                    continue;
                }
            }
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {
    }

    @Override
    protected void setupInventorySlots()
    {
        super.setupInventorySlots();

        InternalInventory inv = getHost().getInventory();
        for (int i = 0; i < inv.size(); i++)
        {
            if (i < LEFT_BOUNDARY)
                this.addSlot(new AppEngSlot(inv, i), SlotSemantics.MACHINE_INPUT);
            else
                this.addSlot(new AppEngSlot(inv, i), SlotSemantics.MACHINE_OUTPUT);
        }
    }

    // 辅助方法

    /**
     * 尝试把物品添加到指定的槽位范围，返回余量
     *
     * @param startIndex 起点，包含位置
     * @param endIndex   终点，不包含此位置
     */
    protected ItemStack insertToRange(InternalInventory inventory, ItemStack stack, int startIndex, int endIndex, boolean simulate)
    {
        if (startIndex > endIndex) throw new IllegalArgumentException("startIndex > endIndex");
        if (startIndex < 0) throw new IllegalArgumentException("startIndex < 0");
        if (endIndex > inventory.size()) throw new IllegalArgumentException("endIndex > inventory.size");

        ItemStack remaining = stack.copy();
        for (int i = startIndex; i < endIndex; i++)
        {
            if (remaining.isEmpty()) break;
            remaining = inventory.insertItem(i, remaining, simulate);
        }
        return remaining;
    }

    ;
}
