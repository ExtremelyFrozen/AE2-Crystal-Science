package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.common.block.entity.ResonatingGrowthFactoryBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.slot.ResonatingStackingSlot;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ResonatingGrowthFactoryMenu extends ResonatingFactoryMenu<ResonatingGrowthFactoryBlockEntity> {

    // 按原图逐列定位；左右各四列之间比普通列距多留 1 像素。
    private static final int[] SLOT_COLUMNS = { 6, 27, 48, 69, 91, 112, 133, 154 };
    private static final int[] SLOT_ROWS = { 18, 36, 56, 74, 94, 112 };
    private static final String CYCLE_GROWTH_STAGE = "cycle_growth_stage";

    @GuiSync(10)
    public int growthStage = 3;

    public ResonatingGrowthFactoryMenu(int id, Inventory playerInventory, ResonatingGrowthFactoryBlockEntity host) {
        super(AECSMenus.RESONATING_GROWTH_FACTORY_MENU.get(), id, playerInventory, host);
        registerClientAction(CYCLE_GROWTH_STAGE, host::cycleTargetStage);
    }

    public void sendCycleGrowthStage() {
        sendClientAction(CYCLE_GROWTH_STAGE);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) growthStage = getHost().getTargetStage();
        super.broadcastChanges();
    }

    @Override
    protected void setupInventorySlots() {
        for (int index = 0; index < ResonatingGrowthFactoryBlockEntity.PAIR_COUNT; index++) {
            int x = SLOT_COLUMNS[index % SLOT_COLUMNS.length];
            int row = index / SLOT_COLUMNS.length * 2;
            var output = new ResonatingStackingSlot(getHost().getOutputInv(), index) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
            output.x = x;
            output.y = SLOT_ROWS[row];
            addSlot(output, SlotSemantics.MACHINE_OUTPUT);

            var input = new ResonatingStackingSlot(getHost().getInputInv(), index);
            input.x = x;
            input.y = SLOT_ROWS[row + 1];
            addSlot(input, SlotSemantics.MACHINE_INPUT);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        var host = getHost();
        return super.stillValid(player) && !host.isRemoved() && host.getLevel() != null && host.getLevel().getBlockEntity(host.getBlockPos()) == host && player.distanceToSqr(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5, host.getBlockPos().getZ() + 0.5) <= 64;
    }
}
