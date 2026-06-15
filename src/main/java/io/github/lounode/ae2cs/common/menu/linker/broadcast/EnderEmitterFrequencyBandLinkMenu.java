package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.networking.IGrid;
import appeng.api.networking.pathing.ControllerState;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.locator.MenuHostLocator;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnderEmitterFrequencyBandLinkMenu extends AEBaseMenu implements CustomReturnableSubMenu {

    private static final String LINK_TO_BAND = "link_to_band";

    private final EnderEmitterBlockEntity host;

    @GuiSync(1)
    public String selectedBand = "";

    @GuiSync(2)
    public boolean connected = false;

    public EnderEmitterFrequencyBandLinkMenu(int id, Inventory playerInventory, EnderEmitterBlockEntity host) {
        super(AECSMenus.ENDER_EMITTER_FREQUENCY_BAND_LINK_MENU.get(), id, playerInventory, host);
        this.host = host;

        registerClientAction(LINK_TO_BAND, String.class, this::linkToBand);
    }

    public void sendLinkToBand(String password) {
        sendClientAction(LINK_TO_BAND, password);
    }

    private void linkToBand(String password) {
        if (selectedBand == null || selectedBand.isEmpty()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(this.selectedBand);
        if (band == null) return;

        boolean permissionValid = band.validWhiteList(getPlayer().getUUID()) || band.validPassword(password);
        if (!permissionValid) {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.band_link.password_error"), true);
            return;
        }

        IGrid bandGrid = band.getBindGrid();
        IGrid hostGrid = host.getMainNode().getGrid();
        if (hostGrid != null && hostGrid.getPathingService().getControllerState() == ControllerState.CONTROLLER_ONLINE && hostGrid != bandGrid) {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.band_link.receiver.grid_conflict"), true);
            return;
        }

        host.connectToBand(this.selectedBand);
    }

    @Override
    public void broadcastChanges() {
        connected = selectedBand != null && !selectedBand.isEmpty() && selectedBand.equals(host.getBandName());
        super.broadcastChanges();
    }

    public static void open(ServerPlayer player, MenuHostLocator locator, String selectedBand) {
        MenuOpener.open(AECSMenus.ENDER_EMITTER_FREQUENCY_BAND_LINK_MENU.get(), player, locator);

        if (player.containerMenu instanceof EnderEmitterFrequencyBandLinkMenu menu) {
            menu.setSelectedBand(selectedBand);
            menu.broadcastChanges();
        }
    }

    private void setSelectedBand(String band) {
        selectedBand = band;
    }

    @Override
    public MenuType<?> getReturnToMenuType() {
        return AECSMenus.ENDER_EMITTER_FREQUENCY_BAND_MENU.get();
    }

    @Override
    public EnderEmitterBlockEntity getHost() {
        return host;
    }
}
