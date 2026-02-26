package io.github.lounode.ae2cs.common.menu;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * 防止被他人mixin PatternProviderMenu的副作用影响
 */
public class UpgradeablePatternProviderMenu extends AEBaseMenu
{

    protected final PatternProviderLogic logic;
    private final ToolboxMenu toolbox;

    @GuiSync(3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(4)
    public YesNo showInAccessTerminal = YesNo.YES;
    @GuiSync(5)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(6)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;
    @GuiSync(7)
    public GenericStack unlockStack = null;

    public UpgradeablePatternProviderMenu(MenuType<? extends UpgradeablePatternProviderMenu> menuType, int id, Inventory playerInventory,
                                          PatternProviderLogicHost host)
    {
        super(menuType, id, playerInventory, host);
        this.toolbox = new ToolboxMenu(this);

        this.createPlayerInventorySlots(playerInventory);

        this.logic = host.getLogic();

        if (host instanceof IUpgradeableObject upgradeableHost)
        {
            setupUpgrades(upgradeableHost.getUpgrades());
        }

        var patternInv = logic.getPatternInv();
        for (int x = 0; x < patternInv.size(); x++)
        {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN,
                            patternInv, x),
                    SlotSemantics.ENCODED_PATTERN);
        }

        var returnInv = logic.getReturnInv().createMenuWrapper();
        for (int i = 0; i < PatternProviderReturnInventory.NUMBER_OF_SLOTS; i++)
        {
            if (i < returnInv.size())
            {
                this.addSlot(new AppEngSlot(returnInv, i), SlotSemantics.STORAGE);
            }
        }
    }

    @Override
    public void broadcastChanges()
    {
        if (isServerSide())
        {
            blockingMode = logic.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInAccessTerminal = logic.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL);
            lockCraftingMode = logic.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
            craftingLockedReason = logic.getCraftingLockedReason();
            unlockStack = logic.getUnlockStack();
        }
        this.toolbox.tick();
        super.broadcastChanges();
    }

    public GenericStackInv getReturnInv()
    {
        return logic.getReturnInv();
    }

    public YesNo getBlockingMode()
    {
        return blockingMode;
    }

    public LockCraftingMode getLockCraftingMode()
    {
        return lockCraftingMode;
    }

    public LockCraftingMode getCraftingLockedReason()
    {
        return craftingLockedReason;
    }

    public GenericStack getUnlockStack()
    {
        return unlockStack;
    }

    public YesNo getShowInAccessTerminal()
    {
        return showInAccessTerminal;
    }

    public ToolboxMenu getToolbox()
    {
        return toolbox;
    }

    public IUpgradeInventory getUpgrades()
    {
        if (logic instanceof IUpgradeableObject upgradeableLogic)
            return upgradeableLogic.getUpgrades();
        else
            return UpgradeInventories.empty();
    }
}
