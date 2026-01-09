package io.github.lounode.ae2cs.common.menu;

import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.recipes.entropy.EntropyMode;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.common.block.entity.EntropyVariationReactionChamberBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class EntropyVariationReactionChamberMenu extends UpgradeableMenu<EntropyVariationReactionChamberBlockEntity>
{
    @GuiSync(10)
    public int recipeProgress;

    @GuiSync(11)
    public int recipeNeedTicks;

    @GuiSync(12)
    public double currentEnergy;

    @GuiSync(13)
    public double maxEnergy;

    @GuiSync(14)
    public EntropyMode entropyMode;

    public EntropyVariationReactionChamberMenu(MenuType<?> menuType, int id, Inventory ip, EntropyVariationReactionChamberBlockEntity host)
    {
        super(menuType, id, ip, host);
    }

    @Override
    protected void setupInventorySlots()
    {
        super.setupInventorySlots();

        InternalInventory inputInv = getHost().getInputInv().createMenuWrapper();
        InternalInventory outputInv = getHost().getOutputInv().createMenuWrapper();
        for (int i = 0; i < inputInv.size(); i++)
        {
            AppEngSlot inputSlot = new AppEngSlot(inputInv, i);
            this.addSlot(inputSlot, SlotSemantics.MACHINE_INPUT);
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
        this.entropyMode = cm.getSetting(AECSSettings.ENTROPY_CHANGE_MODE);
    }

    @Override
    public void broadcastChanges()
    {
        recipeNeedTicks = getHost().getActiveRecipeEnergyCost();
        recipeProgress = getHost().getRecipeProgress();
        maxEnergy = getHost().getAEMaxPower();
        currentEnergy = getHost().getAECurrentPower();
        entropyMode = getHost().getEntropyMode();

        super.broadcastChanges();
    }
}