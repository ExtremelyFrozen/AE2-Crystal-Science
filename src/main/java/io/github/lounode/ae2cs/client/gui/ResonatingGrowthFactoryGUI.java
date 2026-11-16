package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.common.block.entity.ResonatingGrowthFactoryBlockEntity;
import io.github.lounode.ae2cs.common.menu.ResonatingGrowthFactoryMenu;

import appeng.client.gui.style.StyleManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ResonatingGrowthFactoryGUI extends ResonatingFactoryScreen<ResonatingGrowthFactoryMenu> {

    private final AECSIconButton growthStageButton;

    public ResonatingGrowthFactoryGUI(ResonatingGrowthFactoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/resonating_growth_factory_menu.json"));
        addToLeftToolbar(SideConfigGUI.iconButton());
        growthStageButton = new AECSIconButton(button -> menu.sendCycleGrowthStage()) {

            @Override
            protected IButtonIcon getIcon() {
                return switch (menu.growthStage) {
                    case 0 -> AECSIcon.GROW_SMALL;
                    case 1 -> AECSIcon.GROW_MIDDLE;
                    case 2 -> AECSIcon.GROW_BIG;
                    case ResonatingGrowthFactoryBlockEntity.DUST_TARGET -> AECSIcon.GROW_DUST;
                    case ResonatingGrowthFactoryBlockEntity.CRYSTAL_TARGET -> AECSIcon.GROW_CRYSTAL;
                    default -> AECSIcon.GROW_FULL;
                };
            }
        };
        addToLeftToolbar(growthStageButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        String targetKey = switch (menu.growthStage) {
            case ResonatingGrowthFactoryBlockEntity.DUST_TARGET -> "ae2cs.gui.resonating_growth_factory.target.dust";
            case ResonatingGrowthFactoryBlockEntity.CRYSTAL_TARGET -> "ae2cs.gui.resonating_growth_factory.target.crystal";
            default -> "ae2cs.gui.crystal_growth_accelerator.stage." + menu.growthStage;
        };
        growthStageButton.setMessage(Component.translatable(
                "ae2cs.gui.resonating_growth_factory.target", Component.translatable(targetKey)));
    }
}
