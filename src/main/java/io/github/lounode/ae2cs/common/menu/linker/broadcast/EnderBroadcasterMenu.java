package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.util.IConfigManager;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

public class EnderBroadcasterMenu extends UpgradeableMenu<EnderBroadcasterBlockEntity> {

    private static final String openFrequencyBandMenuAction = "open_frequency_band_menu";
    private static final String openFrequencyBandCreateMenuAction = "open_frequency_band_create_menu";
    private static final String openFrequencyBandManagerMenuAction = "open_frequency_band_manager_menu";
    private static final String toggleLinkerSideAction = "toggle_linker_side";
    private static final String cleanLinkerConnectionAction = "clean_linker_connection";

    @GuiSync(10)
    public String bandName = "";

    @GuiSync(11)
    public EnderBroadcasterBlockEntity.ConnectionType connectionType = EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION;

    @GuiSync(12)
    public int receiverTotalChannels = 0;

    @GuiSync(13)
    public int receiverUsedChannels = 0;

    @GuiSync(14)
    public int senderAvailableChannels = 0;

    @GuiSync(15)
    public long bandUsedChannels = 0;

    @GuiSync(16)
    public long bandTotalChannels = 0;

    public EnderBroadcasterMenu(int id, Inventory playerInv, @NotNull EnderBroadcasterBlockEntity host) {
        super(AECSMenus.ENDER_BROADCASTER_MENU.get(), id, playerInv, host);
        registerClientAction(openFrequencyBandMenuAction, this::openFrequencyBandMenuAction);
        registerClientAction(openFrequencyBandCreateMenuAction, this::openFrequencyBandCreateMenuAction);
        registerClientAction(openFrequencyBandManagerMenuAction, this::openFrequencyBandManagerMenuAction);
        registerClientAction(toggleLinkerSideAction, this::toggleLinkerSideAction);
        registerClientAction(cleanLinkerConnectionAction, this::cleanLinkerConnectionAction);
    }

    @Override
    public void broadcastChanges() {
        EnderBroadcasterBlockEntity host = getHost();

        this.bandName = host.getBandName();
        this.connectionType = host.getConnectionType();
        var node = host.getMainNode().getNode();
        this.receiverTotalChannels = node == null ? 0 : node.getMaxChannels();
        this.receiverUsedChannels = node == null ? 0 : node.getUsedChannels();
        this.senderAvailableChannels = host.getCouldSendChannels();

        BroadcastFrequencyBand band = bandName.isEmpty() ? null : FrequencyBandManager.getBand(bandName);
        this.bandUsedChannels = band == null ? 0 : band.getUsedChannels();
        this.bandTotalChannels = band == null ? 0 : band.getUsableChannels();

        super.broadcastChanges();
    }

    public void sendFrequencyBandMenuAction() {
        sendClientAction(openFrequencyBandMenuAction);
    }

    public void sendOpenFrequencyBandCreateMenuAction() {
        sendClientAction(openFrequencyBandCreateMenuAction);
    }

    public void sendOpenFrequencyBandManagerMenuAction() {
        sendClientAction(openFrequencyBandManagerMenuAction);
    }

    public void sendToggleLinkerSideAction() {
        sendClientAction(toggleLinkerSideAction);
    }

    public void sendCleanLinkerConnectionAction() {
        sendClientAction(cleanLinkerConnectionAction);
    }

    private void openFrequencyBandMenuAction() {
        MenuOpener.open(AECSMenus.FREQUENCY_BAND_MENU.get(), getPlayer(), getLocator());
    }

    private void openFrequencyBandCreateMenuAction() {
        MenuOpener.open(AECSMenus.FREQUENCY_BAND_CREATE_MENU.get(), getPlayer(), getLocator());
    }

    private void openFrequencyBandManagerMenuAction() {
        if (bandName != null && !bandName.isEmpty()) {
            MenuOpener.open(AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get(), getPlayer(), getLocator());
        } else {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.broadcaster.sender.not_connect_any_band"), true);
        }
    }

    private void toggleLinkerSideAction() {
        if (bandName == null || bandName.isEmpty()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandName);
        if (band == null) return;

        String targetBand = band.getName();
        if (getHost().getConnectionType() == EnderBroadcasterBlockEntity.ConnectionType.AS_RECEIVER) {
            getHost().cleanConnectionPermanent();
            getHost().connectToBand(targetBand, true);
        } else if (getHost().getConnectionType() == EnderBroadcasterBlockEntity.ConnectionType.AS_SENDER) {
            getHost().cleanConnectionPermanent();
            getHost().connectToBand(targetBand, false);
        }
    }

    private void cleanLinkerConnectionAction() {
        if (getHost().getBandName() != null && !getHost().getBandName().isEmpty()) {
            getHost().cleanConnectionPermanent();
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {}

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !getHost().isRemoved();
    }
}
