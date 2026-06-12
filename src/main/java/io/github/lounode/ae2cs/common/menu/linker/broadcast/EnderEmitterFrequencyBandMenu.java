package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.BroadcastBandsField;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.List;

public class EnderEmitterFrequencyBandMenu extends AEBaseMenu implements CustomReturnableSubMenu {

    private static final String TRY_CONNECT_BAND = "try_connect_band";

    private final EnderEmitterBlockEntity host;

    @GuiSync(1)
    public BroadcastBandsField bandsInfo = new BroadcastBandsField(List.of());

    public EnderEmitterFrequencyBandMenu(int id, Inventory ip, EnderEmitterBlockEntity host) {
        super(AECSMenus.ENDER_EMITTER_FREQUENCY_BAND_MENU.get(), id, ip, host);
        this.host = host;

        registerClientAction(TRY_CONNECT_BAND, String.class, this::tryConnectBand);
    }

    @Override
    public void broadcastChanges() {
        BroadcastBandsField newBandsInfo = FrequencyBandManager.getBandsInfoByPlayer(getPlayer());
        if (newBandsInfo != null) this.bandsInfo = newBandsInfo;

        super.broadcastChanges();
    }

    public void sendTryConnectBand(String bandId) {
        sendClientAction(TRY_CONNECT_BAND, bandId);
    }

    private void tryConnectBand(String bandId) {
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        if (band == null) return;

        EnderEmitterFrequencyBandLinkMenu.open((ServerPlayer) getPlayer(), getLocator(), bandId);
    }

    @Override
    public EnderEmitterBlockEntity getHost() {
        return host;
    }

    @Override
    public MenuType<?> getReturnToMenuType() {
        return AECSMenus.ENDER_EMITTER_MENU.get();
    }
}
