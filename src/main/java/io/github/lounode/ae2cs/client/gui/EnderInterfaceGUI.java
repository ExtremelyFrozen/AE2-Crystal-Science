package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.BlackListMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.EnderInterfaceMenu;

import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderInterfaceGUI extends InterfaceScreen<EnderInterfaceMenu> {

    private AECSServerSettingToggleButton<BlackListMode> blackListModeButton;
    private AECSServerSettingToggleButton<ShowRangeMode> showRangeButton;
    private AECSIconButton addRangeButton;
    private AECSIconButton reduceRangeButton;

    public EnderInterfaceGUI(EnderInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        blackListModeButton = new AECSServerSettingToggleButton<>(AECSSettings.BLACK_LIST_MODE, BlackListMode.WHITELIST);
        addToLeftToolbar(blackListModeButton);

        showRangeButton = new AECSServerSettingToggleButton<>(AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE);
        addToLeftToolbar(showRangeButton);

        addRangeButton = new AECSIconButton(button -> menu.sendChangeAbsorbRange(1)) {

            @Override
            protected IButtonIcon getIcon() {
                return AECSIcon.ADDITION_SIGN;
            }
        };
        widgets.add("add_range_button", addRangeButton);

        reduceRangeButton = new AECSIconButton(button -> menu.sendChangeAbsorbRange(-1)) {

            @Override
            protected IButtonIcon getIcon() {
                return AECSIcon.SUBTRACTION_SIGN;
            }
        };
        widgets.add("reduce_range_button", reduceRangeButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        blackListModeButton.set(menu.blackListMode);
        showRangeButton.set(menu.showRange);

        setTextContent("absorb_range", Component.literal(String.valueOf(menu.absorbRange)));
    }
}
