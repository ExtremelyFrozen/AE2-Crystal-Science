package io.github.lounode.ae2cs.client.gui.subGUI;

import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.AdaptedAE2Icon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSAutoModeToggleButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.client.gui.widgets.AECSSideConfigToggleButton;
import io.github.lounode.ae2cs.common.init.AECSPackets;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;
import io.github.lounode.ae2cs.network.c2s.SideConfigMenuOpenPacket;

import appeng.api.orientation.RelativeSide;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;

public class SideConfigGUI extends AEBaseScreen<SideConfigMenu> {

    private final AECSAutoModeToggleButton autoImportButton;
    private final AECSAutoModeToggleButton autoExportButton;
    private final AECSIconButton clearSideDirPolicyButton;

    private final AECSSideConfigToggleButton topSideButton;
    private final AECSSideConfigToggleButton bottomSideButton;
    private final AECSSideConfigToggleButton leftSideButton;
    private final AECSSideConfigToggleButton rightSideButton;
    private final AECSSideConfigToggleButton frontSideButton;
    private final AECSSideConfigToggleButton backSideButton;

    public SideConfigGUI(SideConfigMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        AESubScreen.addBackButton(menu, "back_button", widgets);

        autoImportButton = new AECSAutoModeToggleButton(false, Component.translatable("ae2cs.menu.side_config.button.import"), menu::sendChangeAutoImport);
        autoImportButton.mapIcon(AECSAutoModeToggleButton.State.ENABLED, AECSIcon.RECEIVER_STATE);
        autoImportButton.mapIcon(AECSAutoModeToggleButton.State.DISABLED, AECSIcon.RECEIVER_STATE);
        autoExportButton = new AECSAutoModeToggleButton(false, Component.translatable("ae2cs.menu.side_config.button.export"), menu::sendChangeAutoExport);
        autoExportButton.mapIcon(AECSAutoModeToggleButton.State.ENABLED, AECSIcon.SENDER_STATE);
        autoExportButton.mapIcon(AECSAutoModeToggleButton.State.DISABLED, AECSIcon.SENDER_STATE);
        clearSideDirPolicyButton = new AECSIconButton(button -> menu.sendClearSideDirPolicy()) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AdaptedAE2Icon.CLEAR;
            }
        };
        this.clearSideDirPolicyButton.setMessage(Component.translatable("ae2cs.menu.side_config.button.clear"));

        if (menu.getBlockEntity() instanceof AEBaseBlockEntity aeBaseBlockEntity) {
            // 注意：这里反转了左右，因为ae的给出方向是基于方块的，而这里UI中需要符合玩家视角直觉
            topSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.TOP), RelativeSide.TOP);
            bottomSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.BOTTOM), RelativeSide.BOTTOM);
            leftSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.RIGHT), RelativeSide.LEFT);
            rightSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.LEFT), RelativeSide.RIGHT);
            frontSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.FRONT), RelativeSide.FRONT);
            backSideButton = new AECSSideConfigToggleButton(menu, aeBaseBlockEntity.getOrientation().getSide(RelativeSide.BACK), RelativeSide.BACK);
        } else {
            topSideButton = new AECSSideConfigToggleButton(menu, Direction.UP, null);
            bottomSideButton = new AECSSideConfigToggleButton(menu, Direction.DOWN, null);
            leftSideButton = new AECSSideConfigToggleButton(menu, Direction.EAST, null);
            rightSideButton = new AECSSideConfigToggleButton(menu, Direction.WEST, null);
            frontSideButton = new AECSSideConfigToggleButton(menu, Direction.NORTH, null);
            backSideButton = new AECSSideConfigToggleButton(menu, Direction.SOUTH, null);
        }

        addToLeftToolbar(autoImportButton);
        addToLeftToolbar(autoExportButton);
        addToLeftToolbar(clearSideDirPolicyButton);
        widgets.add("top_side_button", topSideButton);
        widgets.add("bottom_side_button", bottomSideButton);
        widgets.add("left_side_button", leftSideButton);
        widgets.add("right_side_button", rightSideButton);
        widgets.add("front_side_button", frontSideButton);
        widgets.add("back_side_button", backSideButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (menu.sidePolicies != null) {
            autoImportButton.setBoolean(menu.sidePolicies.autoImport());
            autoExportButton.setBoolean(menu.sidePolicies.autoExport());

            topSideButton.syncFromMenu();
            bottomSideButton.syncFromMenu();
            leftSideButton.syncFromMenu();
            rightSideButton.syncFromMenu();
            frontSideButton.syncFromMenu();
            backSideButton.syncFromMenu();
        }
    }

    public static AECSIconButton iconButton() {
        AECSIconButton iconButton = new AECSIconButton(button -> AECSPackets.INSTANCE.sendToServer(new SideConfigMenuOpenPacket())) {

            @Override
            protected @NotNull IButtonIcon getIcon() {
                return AECSIcon.SIDE_CONFIG;
            }
        };
        iconButton.setMessage(Component.translatable("ae2cs.menu.side_config.button.open"));
        return iconButton;
    }
}
