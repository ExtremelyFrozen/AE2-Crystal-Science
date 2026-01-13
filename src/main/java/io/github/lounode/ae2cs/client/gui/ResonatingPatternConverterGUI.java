package io.github.lounode.ae2cs.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.common.menu.ResonatingPatternConverterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ResonatingPatternConverterGUI extends UpgradeableScreen<ResonatingPatternConverterMenu>
{
    private final AECSIconButton converterButton;

    public ResonatingPatternConverterGUI(ResonatingPatternConverterMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        converterButton = new AECSIconButton(button -> menu.onConverterPattern())
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AdaptedAE2Icon.SCHEDULING_RANDOM;
            }
        };
        this.widgets.add("converter", converterButton);
    }
}
