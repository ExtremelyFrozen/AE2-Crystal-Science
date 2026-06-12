package io.github.lounode.ae2cs.common.menu;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.util.IConfigManager;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnderEmitterMenu extends UpgradeableMenu<EnderEmitterBlockEntity> {

    private static final String changeDistanceAction = "change_distance";
    private static final String trySacnAllAction = "try_sacn_all";
    private static final String destroyAllAction = "destroy_all";
    private static final String openBandMenuAction = "open_band_menu";
    private static final String cleanBandConnectionAction = "clean_band_connection";

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

    public EnderEmitterMenu(MenuType<?> menuType, int id, Inventory ip, EnderEmitterBlockEntity host) {
        super(menuType, id, ip, host);

        registerClientAction(changeDistanceAction, Integer.class, this::onChangeDistance);
        registerClientAction(trySacnAllAction, this::onSacnAll);
        registerClientAction(destroyAllAction, this::onDestroyAll);
        registerClientAction(openBandMenuAction, this::onOpenBandMenu);
        registerClientAction(cleanBandConnectionAction, this::onCleanBandConnection);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.autoMode = getHost().getConfigManager().getSetting(AECSSettings.AUTO_LINK_MODE);
        this.autoLinkCableMode = getHost().getConfigManager().getSetting(AECSSettings.AUTO_LINK_CABLE_MODE);
        this.showRangeMode = getHost().getConfigManager().getSetting(AECSSettings.SHOW_RANGE_MODE);
    }

    @Override
    public void broadcastChanges() {
        this.linkDistance = getHost().getLinkDistance();
        this.maxLinkDistance = EnderEmitterBlockEntity.maxLinkDistance.get();
        this.bandName = getHost().getBandName();
        this.emitterUsedChannels = getHost().getUsedLinkChannels();
        this.emitterTotalChannels = getHost().getMaxLinkChannels();

        BroadcastFrequencyBand band = bandName.isEmpty() ? null : FrequencyBandManager.getBand(bandName);
        this.bandUsedChannels = band == null ? 0 : band.getUsedChannels();
        this.bandTotalChannels = band == null ? 0 : band.getUsableChannels();

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

    private void onChangeDistance(int delta) {
        getHost().setLinkDistance(this.linkDistance + delta);
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
