package io.github.lounode.ae2cs.client.gui;

import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.client.gui.icon.IButtonIcon;
import io.github.lounode.ae2cs.client.gui.widgets.AECSIconButton;
import io.github.lounode.ae2cs.common.menu.CrystalGrowthAcceleratorMenu;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.StyleManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalGrowthAcceleratorGUI extends UpgradeableScreen<CrystalGrowthAcceleratorMenu> {

    private final AECSIconButton growthStageButton;

    public CrystalGrowthAcceleratorGUI(CrystalGrowthAcceleratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/crystal_growth_accelerator_menu.json"));
        this.growthStageButton = new AECSIconButton(button -> menu.sendCycleGrowthStage()) {

            @Override
            protected IButtonIcon getIcon() {
                return AECSIcon.ADDITION_SIGN;
            }
        };
        addToLeftToolbar(growthStageButton);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        growthStageButton.setMessage(Component.translatable(
                "ae2cs.gui.crystal_growth_accelerator.stage",
                Component.translatable("ae2cs.gui.crystal_growth_accelerator.stage." + menu.growthStage)));
    }
}
