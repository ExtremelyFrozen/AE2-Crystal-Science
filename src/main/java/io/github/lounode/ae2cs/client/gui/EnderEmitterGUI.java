package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSServerSettingToggleButton;
import io.github.lounode.ae2cs.common.menu.EnderEmitterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class EnderEmitterGUI extends UpgradeableScreen<EnderEmitterMenu>
{
    private AECSIconButton addDistanceButton;
    private AECSIconButton reduceDistanceButton;
    private AECSServerSettingToggleButton<AutoLinkMode> autoModeButton;
    private AECSServerSettingToggleButton<AutoLinkCableMode> autoLinkCableButton;
    private AECSServerSettingToggleButton<ShowRangeMode> showRangeModeButton;
    private AECSIconButton trySacnAllButton;
    private AECSIconButton destroyAllButton;

    public EnderEmitterGUI(EnderEmitterMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        this.addDistanceButton = new AECSIconButton(button -> {
            int mult = hasShiftDown() ? 5 : 1;
            mult = hasControlDown() ? menu.linkDistance/2 : mult;
            mult = Math.max(1, mult);
            menu.sendChangeDistance(1 * mult);
        })
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.ADDITION_SIGN;
            }
        };
        this.addDistanceButton.setMessage(Component.translatable("ae2cs.menu.ender_emitter.button.add_distance"));
        this.widgets.add("add_distance_button", addDistanceButton);

        this.reduceDistanceButton = new AECSIconButton(button -> {
            int mult = hasShiftDown() ? 5 : 1;
            mult = hasControlDown() ? menu.linkDistance/2 : mult;
            mult = Math.max(1, mult);
            menu.sendChangeDistance(-1 * mult);
        })
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.SUBTRACTION_SIGN;
            }
        };
        this.reduceDistanceButton.setMessage(Component.translatable("ae2cs.menu.ender_emitter.button.reduce_distance"));
        this.widgets.add("reduce_distance_button", reduceDistanceButton);

        this.showRangeModeButton = new AECSServerSettingToggleButton<>(AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE);
        addToLeftToolbar(showRangeModeButton);

        this.autoModeButton = new AECSServerSettingToggleButton<>(AECSSettings.AUTO_LINK_MODE, AutoLinkMode.ENABLE);
        addToLeftToolbar(autoModeButton);

        this.autoLinkCableButton = new AECSServerSettingToggleButton<>(AECSSettings.AUTO_LINK_CABLE_MODE, AutoLinkCableMode.ENABLE);
        addToLeftToolbar(autoLinkCableButton);

        this.trySacnAllButton = new AECSIconButton(button -> menu.sendSacnAll())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.LINK_TO_ALL;
            }
        };
        this.trySacnAllButton.setMessage(Component.translatable("ae2cs.menu.ender_emitter.button.try_sacn_all"));
        addToLeftToolbar(trySacnAllButton);

        this.destroyAllButton = new AECSIconButton(button -> menu.sendDestroyAll())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.BREAK_ALL_LINKS;
            }
        };
        this.destroyAllButton.setMessage(Component.translatable("ae2cs.menu.ender_emitter.button.destroy_all"));
        addToLeftToolbar(destroyAllButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();
        this.autoModeButton.set(menu.autoMode);
        this.autoLinkCableButton.set(menu.autoLinkCableMode);
        this.showRangeModeButton.set(menu.showRangeMode);

        setTextContent("max_distance", Component.translatable("ae2cs.menu.ender_emitter.max_distance", menu.maxLinkDistance));
        setTextContent("distance", Component.translatable("ae2cs.menu.ender_emitter.distance", menu.linkDistance));
    }
}
