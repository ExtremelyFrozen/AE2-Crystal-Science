package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.SetStockAmountMenu;
import io.github.lounode.ae2cs.common.menu.IntegratedInterfaceSetStockAmountMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class IntegratedInterfaceSetStockAmountGUI extends AEBaseScreen<IntegratedInterfaceSetStockAmountMenu>
{

    private final NumberEntryWidget amount;

    private boolean amountInitialized;

    public IntegratedInterfaceSetStockAmountGUI(IntegratedInterfaceSetStockAmountMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/set_stock_amount.json"));

        widgets.addButton("save", GuiText.Set.text(), this::confirm);

        AESubScreen.addBackButton(menu, "back", widgets);

        this.amount = widgets.addNumberEntryWidget("amountToStock", NumberEntryType.UNITLESS);
        this.amount.setLongValue(1);
        this.amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        this.amount.setMinValue(0);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (!this.amountInitialized) {
            var whatToStock = menu.getWhatToStock();
            if (whatToStock != null) {
                this.amount.setType(NumberEntryType.of(whatToStock));
                this.amount.setLongValue(menu.getInitialAmount());

                this.amount.setMaxValue(menu.getMaxAmount());
                this.amountInitialized = true;
            }
        }
    }

    private void confirm() {
        this.amount.getIntValue().ifPresent(menu::confirm);
    }
}
