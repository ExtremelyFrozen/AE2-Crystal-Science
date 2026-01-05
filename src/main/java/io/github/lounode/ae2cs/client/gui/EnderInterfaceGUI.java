package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.Icon;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.client.gui.icon.AE2IconAdapter;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSToggleButton;
import io.github.lounode.ae2cs.common.menu.EnderInterfaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderInterfaceGUI extends InterfaceScreen<EnderInterfaceMenu>
{
    private AECSToggleButton blackListModeButton;
    private AECSToggleButton showRangeButton;
    private AECSIconButton addRangeButton;
    private AECSIconButton reduceRangeButton;

    public EnderInterfaceGUI(EnderInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        blackListModeButton = new AECSToggleButton(
                AECSIcon.BLACK_LIST_MODE, AECSIcon.WHITE_LIST_MODE,
                Component.translatable("ae2cs.menu.ender_interface.black_list_mode_title"),
                Component.translatable("ae2cs.menu.ender_interface.black_list_mode_desc"),
                menu::sendChangeBlackListMode);
        addToLeftToolbar(blackListModeButton);

        showRangeButton = new AECSToggleButton(
                new AE2IconAdapter(Icon.OVERLAY_ON), new AE2IconAdapter(Icon.OVERLAY_OFF),
                Component.translatable("ae2cs.menu.ender_interface.show_range_title"),
                Component.translatable("ae2cs.menu.ender_interface.show_range_desc"),
                menu::sendChangeShowRange);
        addToLeftToolbar(showRangeButton);

        addRangeButton = new AECSIconButton(button -> menu.sendChangeAbsorbRange(1))
        {
            @Override
            protected IButtonIcon getIcon()
            {
                return AECSIcon.ADDITION_SIGN;
            }
        };
        widgets.add("add_range_button", addRangeButton);

        reduceRangeButton = new AECSIconButton(button -> menu.sendChangeAbsorbRange(-1))
        {
            @Override
            protected IButtonIcon getIcon()
            {
                return AECSIcon.SUBTRACTION_SIGN;
            }
        };
        widgets.add("reduce_range_button", reduceRangeButton);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY)
    {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        guiGraphics.drawString(this.font, String.valueOf(menu.absorbRange), 139, 112, 4210752, false);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();
        blackListModeButton.setState(menu.blackListMode);
        showRangeButton.setState(menu.showRange);
    }
}
