package io.github.lounode.ae2cs.common.menu;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.primitives.Ints;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// AE原版的SetStockAmountMenu的IntegratedInterface版本
public class IntegratedInterfaceSetStockAmountMenu extends AEBaseMenu implements ISubMenu
{

    public static final String ACTION_SET_STOCK_AMOUNT = "setStockAmount";

    /**
     * This slot is used to synchronize a visual representation of what is to be stocked to the client.
     */
    private final Slot stockedItem;

    /**
     * This item (server-only) indicates what should actually be crafted.
     */
    private AEKey whatToStock;

    @GuiSync(1)
    private int initialAmount = -1;

    @GuiSync(2)
    private int maxAmount = -1;

    private int slot;

    private final IntegratedInterfaceHost host;

    public IntegratedInterfaceSetStockAmountMenu(int id, Inventory ip, IntegratedInterfaceHost host) {
        super(AECSMenus.INTEGRATED_INTERFACE_SET_STOCK_AMOUNT_MENU.get(), id, ip, host);
        registerClientAction(ACTION_SET_STOCK_AMOUNT, Integer.class, this::confirm);
        this.host = host;
        this.stockedItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.addSlot(this.stockedItem, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public IntegratedInterfaceHost getHost() {
        return host;
    }

    /**
     * Opens the screen to enter the stocked amount for the given player.
     */
    public static void open(ServerPlayer player, MenuHostLocator locator,
                            int slot,
                            AEKey whatToStock, int initialAmount) {
        MenuOpener.open(AECSMenus.INTEGRATED_INTERFACE_SET_STOCK_AMOUNT_MENU.get(), player, locator);

        if (player.containerMenu instanceof IntegratedInterfaceSetStockAmountMenu cca) {
            cca.setWhatToStock(slot, whatToStock, initialAmount);
            cca.broadcastChanges();
        }
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level();
    }

    private void setWhatToStock(int slot, AEKey whatToStock, int initialAmount) {
        this.slot = slot;
        this.whatToStock = Objects.requireNonNull(whatToStock, "whatToStock");
        this.initialAmount = initialAmount;
        this.maxAmount = Ints.saturatedCast(host.getConfigInv().getMaxAmount(whatToStock));
        this.stockedItem.set(whatToStock.wrapForDisplayOrFilter());
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Changes the amount to be stocked.
     *
     * @param amount The number of items to stock.
     */
    public void confirm(int amount) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_STOCK_AMOUNT, amount);
            return;
        }

        var config = host.getConfigInv();

        // In case the config changed don't set anything
        if (!Objects.equals(config.getKey(this.slot), whatToStock)) {
            host.returnToMainMenu(getPlayer(), this);
            return;
        }

        amount = (int) Math.min(amount, config.getMaxAmount(whatToStock));

        if (amount <= 0) {
            config.setStack(slot, null);
        } else {
            config.setStack(slot, new GenericStack(whatToStock, amount));
        }
        host.returnToMainMenu(getPlayer(), this);
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    @Nullable
    public AEKey getWhatToStock() {
        var stack = GenericStack.fromItemStack(stockedItem.getItem());
        return stack != null ? stack.what() : null;
    }
}

