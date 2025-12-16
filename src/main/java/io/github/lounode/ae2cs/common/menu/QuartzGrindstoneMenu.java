package io.github.lounode.ae2cs.common.menu;

import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import io.github.lounode.ae2cs.common.block.entity.QuartzGrindstoneBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class QuartzGrindstoneMenu extends UpgradeableMenu<QuartzGrindstoneBlockEntity>
{
    @GuiSync(10)
    public int recipeProgress;

    @GuiSync(11)
    public int recipeNeedTicks;

    public QuartzGrindstoneMenu(int id, Inventory ip, QuartzGrindstoneBlockEntity host)
    {
        super(AECSMenus.QUARTZ_GRINDSTONE_MENU.get(), id, ip, host);
    }

    @Override
    protected void setupInventorySlots()
    {
        super.setupInventorySlots();

        AppEngInternalInventory inputInv = getHost().getInputInv();
        AppEngInternalInventory workingInv = getHost().getWorkingInv();
        AppEngInternalInventory outputInv = getHost().getOutputInv();
        for (int i = 0; i < inputInv.size(); i++)
        {
            AppEngSlot inputSlot = new AppEngSlot(inputInv, i);
            this.addSlot(inputSlot, SlotSemantics.MACHINE_INPUT);
        }
        for (int i = 0; i < workingInv.size(); i++)
        {
            AppEngSlot workingSlot = new AppEngSlot(workingInv, i);
            this.addSlot(workingSlot, SlotSemantics.STORAGE);
        }
        for (int i = 0; i < outputInv.size(); i++)
        {
            AppEngSlot outputSlot = new AppEngSlot(outputInv, i)
            {
                @Override
                public boolean mayPlace(ItemStack stack)
                {
                    return false;
                }
            };
            this.addSlot(outputSlot, SlotSemantics.MACHINE_OUTPUT);
        }

    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {
    }

    @Override
    public void broadcastChanges()
    {
        recipeNeedTicks = getHost().getActiveRecipeTime();
        recipeProgress = getHost().getRecipeProgress();

        super.broadcastChanges();
    }
}
