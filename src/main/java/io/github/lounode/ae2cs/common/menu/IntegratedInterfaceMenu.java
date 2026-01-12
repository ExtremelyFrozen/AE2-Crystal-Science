package io.github.lounode.ae2cs.common.menu;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class IntegratedInterfaceMenu extends UpgradeableMenu<IntegratedInterfaceHost>
{
    public static final String ACTION_OPEN_SET_AMOUNT = "setAmount";

    public final boolean extended;

    @GuiSync(10)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(11)
    public YesNo showInAccessTerminal = YesNo.YES;
    @GuiSync(12)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(13)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;
    @GuiSync(14)
    public GenericStack unlockStack = null;

    private final IntegratedInterfaceLogic logic;

    public final int pageSize;
    public int pageIndex = 0;

    public IntegratedInterfaceMenu(int id, Inventory ip, IntegratedInterfaceHost host)
    {
        super(AECSMenus.INTEGRATED_INTERFACE_MENU.get(), id, ip, host);

        this.logic = host.getLogic();
        this.extended = host.isExtended();
        this.pageSize = extended ? 4 : 1;

        registerClientAction(ACTION_OPEN_SET_AMOUNT, Integer.class, this::openSetAmountMenu);
    }

    // 放除了升级槽之外的其他真实库存
    // 注：玩家槽位已经由UpgradeableMenu处理，不必再写
    @Override
    protected void setupInventorySlots()
    {
        for (int i = 0; i < getHost().getTerminalPatternInventory().size(); ++i)
        {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, getHost().getTerminalPatternInventory(), i), SlotSemantics.ENCODED_PATTERN);
        }
        InternalInventory configWrap = getHost().getConfigInv().createMenuWrapper();
        for (int i = 0; i < getHost().getConfigInv().size(); ++i)
        {
            this.addSlot(new FakeSlot(configWrap, i), SlotSemantics.CONFIG);
        }
        InternalInventory storageWrap = getHost().getStorageInv().createMenuWrapper();
        for (int i = 0; i < getHost().getStorageInv().size(); ++i)
        {
            this.addSlot(new AppEngSlot(storageWrap, i), SlotSemantics.STORAGE);
        }
        togglePage(pageIndex);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager configManager)
    {
        this.setFuzzyMode(configManager.getSetting(Settings.FUZZY_MODE));
        blockingMode = configManager.getSetting(Settings.BLOCKING_MODE);
        showInAccessTerminal = configManager.getSetting(Settings.PATTERN_ACCESS_TERMINAL);
        lockCraftingMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        craftingLockedReason = logic.getCraftingLockedReason();
        unlockStack = logic.getUnlockStack();
    }

    /**
     * 此函数客户端直接执行，不在乎服务端状态
     */
    public void onTogglePageButton(int delta)
    {
        pageIndex += delta;
        if (pageIndex >= pageSize) pageIndex = 0;
        else if (pageIndex < 0) pageIndex = pageSize - 1;

        togglePage(pageIndex);
    }

    /**
     * 根据翻页数启用和禁用slot
     */
    private void togglePage(int pageIndex)
    {
        int activeStart = pageIndex * 9;
        int activeEnd = activeStart + 9;

        var patternSlots = getSlots(SlotSemantics.ENCODED_PATTERN);
        for (int i = 0; i < patternSlots.size(); ++i)
        {
            if (!(patternSlots.get(i) instanceof AppEngSlot appEngSlot)) continue;
            appEngSlot.setSlotEnabled(i >= activeStart && i < activeEnd);
            appEngSlot.y = 115;
        }

        var configSlots = getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < configSlots.size(); ++i)
        {
            if (!(configSlots.get(i) instanceof AppEngSlot appEngSlot)) continue;
            appEngSlot.setSlotEnabled(i >= activeStart && i < activeEnd);
            appEngSlot.y = 53;
        }

        var storageSlots = getSlots(SlotSemantics.STORAGE);
        for (int i = 0; i < storageSlots.size(); ++i)
        {
            if (!(storageSlots.get(i) instanceof AppEngSlot appEngSlot)) continue;
            appEngSlot.setSlotEnabled(i >= activeStart && i < activeEnd);
            appEngSlot.y = 71;
        }
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

    /**
     * 打开用来设置数量的子菜单
     *
     * @param configSlot 设置第几个槽的数量
     */
    public void openSetAmountMenu(int configSlot)
    {
        if (isClientSide())
        {
            sendClientAction(ACTION_OPEN_SET_AMOUNT, configSlot);
        }
        else
        {
            var stack = getHost().getConfigInv().getStack(configSlot);
            if (stack != null)
            {
                IntegratedInterfaceSetStockAmountMenu.open((ServerPlayer) getPlayer(), getLocator(), configSlot,
                        stack.what(), (int) stack.amount());
            }
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return !getHost().getBlockEntity().isRemoved();
    }
}
