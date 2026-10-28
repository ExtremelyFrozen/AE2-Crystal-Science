package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.ResonatingGrowthFactoryBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.slot.ResonatingStackingSlot;

import appeng.menu.SlotSemantics;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

public class ResonatingGrowthFactoryMenu extends ResonatingFactoryMenu<ResonatingGrowthFactoryBlockEntity> {

    // 按原图逐列定位；左右各四列之间比普通列距多留 1 像素。
    private static final int[] SLOT_COLUMNS = { 6, 27, 48, 69, 91, 112, 133, 154 };
    private static final int[] SLOT_ROWS = { 18, 36, 56, 74, 94, 112 };

    public ResonatingGrowthFactoryMenu(int id, Inventory playerInventory, ResonatingGrowthFactoryBlockEntity host) {
        super(AECSMenus.RESONATING_GROWTH_FACTORY_MENU.get(), id, playerInventory, host);
    }

    @Override
    protected void setupInventorySlots() {
        for (int index = 0; index < getHost().getInventory().size(); index++) {
            var slot = new ResonatingStackingSlot(getHost().getInventory(), index);
            // 机器槽保留这些坐标，屏幕样式仅负责下方的玩家库存槽。
            slot.x = SLOT_COLUMNS[index % SLOT_COLUMNS.length];
            slot.y = SLOT_ROWS[index / SLOT_COLUMNS.length];
            addSlot(slot, SlotSemantics.STORAGE);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        var host = getHost();
        return super.stillValid(player) && !host.isRemoved() && host.getLevel() != null && host.getLevel().getBlockEntity(host.getBlockPos()) == host && player.distanceToSqr(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5, host.getBlockPos().getZ() + 0.5) <= 64;
    }
}
