package io.github.lounode.ae2cs.client.gui.subGUI;

import appeng.api.orientation.RelativeSide;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSAutoModeToggleButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSSideConfigToggleButton;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;
import io.github.lounode.ae2cs.network.c2s.SideConfigMenuOpenPacket;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class SideConfigGUI extends AEBaseScreen<SideConfigMenu>
{
    private final AECSAutoModeToggleButton autoImportButton;
    private final AECSAutoModeToggleButton autoExportButton;

    private final AECSSideConfigToggleButton topSideButton;
    private final AECSSideConfigToggleButton bottomSideButton;
    private final AECSSideConfigToggleButton leftSideButton;
    private final AECSSideConfigToggleButton rightSideButton;
    private final AECSSideConfigToggleButton frontSideButton;
    private final AECSSideConfigToggleButton backSideButton;

    public SideConfigGUI(SideConfigMenu menu, Inventory playerInventory, Component title, ScreenStyle style)
    {
        super(menu, playerInventory, title, style);

        AESubScreen.addBackButton(menu, "back_button", widgets);

        autoImportButton = new AECSAutoModeToggleButton(false, menu::sendChangeAutoImport);
        autoExportButton = new AECSAutoModeToggleButton(false, menu::sendChangeAutoExport);

        if (menu.getBlockEntity() instanceof AEBaseBlockEntity aeBaseBlockEntity)
        {
            topSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.TOP), RelativeSide.TOP);
            bottomSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.BOTTOM), RelativeSide.BOTTOM);
            leftSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.LEFT), RelativeSide.LEFT);
            rightSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.RIGHT), RelativeSide.RIGHT);
            frontSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.FRONT), RelativeSide.FRONT);
            backSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.BACK), RelativeSide.BACK);
        }
        else
        {
            topSideButton = new AECSSideConfigToggleButton(menu, Direction.UP, null);
            bottomSideButton = new AECSSideConfigToggleButton(menu, Direction.DOWN, null);
            leftSideButton = new AECSSideConfigToggleButton(menu, Direction.WEST, null);
            rightSideButton = new AECSSideConfigToggleButton(menu, Direction.EAST, null);
            frontSideButton = new AECSSideConfigToggleButton(menu, Direction.NORTH, null);
            backSideButton = new AECSSideConfigToggleButton(menu, Direction.SOUTH, null);
        }

        addToLeftToolbar(autoImportButton);
        addToLeftToolbar(autoExportButton);
        widgets.add("top_side_button", topSideButton);
        widgets.add("bottom_side_button", bottomSideButton);
        widgets.add("left_side_button", leftSideButton);
        widgets.add("right_side_button", rightSideButton);
        widgets.add("front_side_button", frontSideButton);
        widgets.add("back_side_button", backSideButton);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();

        if (menu.sidePolicies != null)
        {
            autoImportButton.setValue(menu.sidePolicies.autoImport() ? AECSAutoModeToggleButton.State.ENABLED : AECSAutoModeToggleButton.State.DISABLED);
            autoExportButton.setValue(menu.sidePolicies.autoExport() ? AECSAutoModeToggleButton.State.ENABLED : AECSAutoModeToggleButton.State.DISABLED);

            topSideButton.syncFromMenu();
            bottomSideButton.syncFromMenu();
            leftSideButton.syncFromMenu();
            rightSideButton.syncFromMenu();
            frontSideButton.syncFromMenu();
            backSideButton.syncFromMenu();
        }
    }

    public static AECSIconButton iconButton()
    {
        AECSIconButton iconButton = new AECSIconButton(button -> PacketDistributor.sendToServer(new SideConfigMenuOpenPacket()))
        {
            @Override
            protected @NotNull IButtonIcon getIcon()
            {
                return AECSIcon.SIDE_CONFIG;
            }
        };
        iconButton.setMessage(Component.translatable("ae2cs.menu.side_config.button.open"));
        return iconButton;
    }
}
