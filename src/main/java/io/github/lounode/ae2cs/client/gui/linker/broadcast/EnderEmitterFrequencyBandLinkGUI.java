package io.github.lounode.ae2cs.client.gui.linker.broadcast;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AETextField;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.EnderEmitterFrequencyBandLinkMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderEmitterFrequencyBandLinkGUI extends AEBaseScreen<EnderEmitterFrequencyBandLinkMenu>
{
    private final AETextField passwordInput;

    public EnderEmitterFrequencyBandLinkGUI(EnderEmitterFrequencyBandLinkMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/ender_emitter_frequency_band_link_menu.json"));
        AESubScreen.addBackButton(menu, "back_button", widgets);

        passwordInput = widgets.addTextField("password_input");
        passwordInput.setPlaceholder(Component.translatable("ae2cs.menu.frequency_band_menu.input_password"));
        widgets.addButton("confirm_button", Component.translatable("ae2cs.menu.frequency_band_menu.confirm"), this::confirm);
    }

    @Override
    protected void updateBeforeRender()
    {
        super.updateBeforeRender();
        setTextContent("link_state", menu.connected
                ? Component.translatable("ae2cs.menu.frequency_band_menu.connected")
                : Component.translatable("ae2cs.menu.frequency_band_menu.not_connected"));
        setTextContent("link_mode", Component.translatable("ae2cs.menu.ender_emitter.band_link_mode"));
    }

    private void confirm(Button button)
    {
        menu.sendLinkToBand(passwordInput.getValue());
    }
}
