package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.BroadcastBandsField;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.menu.linker.broadcast.EnderEmitterFrequencyBandLinkMenu;

import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEItems;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.RestrictedInputSlot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.List;

public class EnderEmitterMenu extends UpgradeableMenu<EnderEmitterBlockEntity> {

    private static final String changeDistanceAction = "change_distance";
    private static final String trySacnAllAction = "try_sacn_all";
    private static final String destroyAllAction = "destroy_all";
    private static final String openBandMenuAction = "open_band_menu";
    private static final String cleanBandConnectionAction = "clean_band_connection";
    private static final String selectBandAction = "select_band";
    private static final String changeExpectedChannelsAction = "change_expected_channels";

    @GuiSync(10)
    public int linkDistance;

    @GuiSync(11)
    public AutoLinkMode autoMode;

    @GuiSync(12)
    public AutoLinkCableMode autoLinkCableMode;

    @GuiSync(13)
    public int maxLinkDistance;

    @GuiSync(14)
    public ShowRangeMode showRangeMode;

    @GuiSync(15)
    public String bandName = "";

    @GuiSync(16)
    public long bandUsedChannels = 0;

    @GuiSync(17)
    public long bandTotalChannels = 0;

    @GuiSync(18)
    public int emitterUsedChannels = 0;

    @GuiSync(19)
    public int emitterTotalChannels = 0;

    @GuiSync(20)
    public int expectedChannels = 32;

    @GuiSync(21)
    public BroadcastBandsField bandsInfo = new BroadcastBandsField(List.of());

    @GuiSync(22)
    public int boosterCount;

    @GuiSync(23)
    public BroadcastFrequencyBand.BandError bandError = BroadcastFrequencyBand.BandError.NO_SENDER;

    public EnderEmitterMenu(MenuType<?> menuType, int id, Inventory ip, EnderEmitterBlockEntity host) {
        super(menuType, id, ip, host);

        registerClientAction(changeDistanceAction, Integer.class, this::onChangeDistance);
        registerClientAction(trySacnAllAction, this::onSacnAll);
        registerClientAction(destroyAllAction, this::onDestroyAll);
        registerClientAction(openBandMenuAction, this::onOpenBandMenu);
        registerClientAction(cleanBandConnectionAction, this::onCleanBandConnection);
        registerClientAction(selectBandAction, String.class, this::onSelectBand);
        registerClientAction(changeExpectedChannelsAction, Integer.class, this::onChangeExpectedChannels);
    }

    @Override
    protected void setupUpgrades() {
        // Wireless boosters are not upgrade cards; use the same filter as AE2's access point.
        var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.RANGE_BOOSTER, getHost().getUpgrades(), 0);
        slot.setStackLimit(64);
        addSlot(slot, SlotSemantics.UPGRADE);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.autoMode = getHost().getConfigManager().getSetting(AECSSettings.AUTO_LINK_MODE);
        this.autoLinkCableMode = getHost().getConfigManager().getSetting(AECSSettings.AUTO_LINK_CABLE_MODE);
        this.showRangeMode = getHost().getConfigManager().getSetting(AECSSettings.SHOW_RANGE_MODE);
    }

    @Override
    public void broadcastChanges() {
        if (!isServerSide()) return;
        this.linkDistance = getHost().getLinkDistance();
        this.maxLinkDistance = getHost().getMaxLinkDistance();
        this.bandName = getHost().getBandName();
        this.emitterUsedChannels = getHost().getUsedLinkChannels();
        this.emitterTotalChannels = getHost().getDisplayedMaxLinkChannels();

        BroadcastFrequencyBand band = bandName.isEmpty() ? null : FrequencyBandManager.getBand(bandName);
        this.bandUsedChannels = band == null ? 0 : band.getUsedChannels();
        this.bandTotalChannels = band == null ? 0 : band.getUsableChannels();
        this.bandError = band == null ? BroadcastFrequencyBand.BandError.NO_SENDER : band.getErrorState();
        this.expectedChannels = getHost().getExpectedChannels();
        this.boosterCount = getHost().getUpgrades().getInstalledUpgrades(AEItems.WIRELESS_BOOSTER);
        BroadcastBandsField visibleBands = FrequencyBandManager.getBandsInfoByPlayer(getPlayer());
        this.bandsInfo = visibleBands == null ? new BroadcastBandsField(List.of()) : visibleBands;

        super.broadcastChanges();
    }

    public void sendChangeDistance(int delta) {
        sendClientAction(changeDistanceAction, delta);
    }

    public void sendSacnAll() {
        sendClientAction(trySacnAllAction);
    }

    public void sendDestroyAll() {
        sendClientAction(destroyAllAction);
    }

    public void sendOpenBandMenu() {
        sendClientAction(openBandMenuAction);
    }

    public void sendCleanBandConnection() {
        sendClientAction(cleanBandConnectionAction);
    }

    public void sendSelectBand(String name) {
        sendClientAction(selectBandAction, name);
    }

    public void sendChangeExpectedChannels(int delta) {
        sendClientAction(changeExpectedChannelsAction, delta);
    }

    private void onSelectBand(String name) {
        BroadcastBandsField visibleBands = FrequencyBandManager.getBandsInfoByPlayer(getPlayer());
        if (visibleBands == null || visibleBands.bands().stream().noneMatch(entry -> entry.name().equals(name))) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(name);
        if (band == null) return;
        if (band.validWhiteList(getPlayer().getUUID()) || band.validPassword("")) {
            getHost().connectToBand(name);
        } else {
            EnderEmitterFrequencyBandLinkMenu.open((ServerPlayer) getPlayer(), getLocator(), name);
        }
    }

    private void onChangeExpectedChannels(int delta) {
        if (delta == -10 || delta == -1 || delta == 1 || delta == 10) getHost().changeExpectedChannels(delta);
    }

    private void onChangeDistance(int delta) {
        if (delta == -1 || delta == 1) getHost().setLinkDistance(getHost().getLinkDistance() + delta);
    }

    private void onSacnAll() {
        EnderEmitterBlockEntity.addAllRecentBEtoEmitter(getHost());
    }

    private void onDestroyAll() {
        EnderEmitterBlockEntity.removeAllLinkedFromEmitter(getHost());
    }

    private void onOpenBandMenu() {
        MenuOpener.open(AECSMenus.ENDER_EMITTER_FREQUENCY_BAND_MENU.get(), getPlayer(), getLocator());
    }

    private void onCleanBandConnection() {
        getHost().cleanConnectionPermanent();
    }
}
