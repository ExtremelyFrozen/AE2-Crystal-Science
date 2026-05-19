package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.guisync.GuiSync;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.BroadcastBandsField;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.List;

/**
 * 用于展示服务器中所有频段，并提供连接按钮；
 * 作为{@link EnderBroadcasterMenu}的子菜单
 */
public class FrequencyBandMenu extends AEBaseMenu implements CustomReturnableSubMenu
{
    private static final ClientActionKey<String> TRY_CONNECT_BAND = new ClientActionKey<>("try_connect_band");

    private final EnderBroadcasterBlockEntity host;

    @GuiSync(1)
    public BroadcastBandsField bandsInfo = new BroadcastBandsField(List.of());

    public FrequencyBandMenu(int id, Inventory ip, EnderBroadcasterBlockEntity host)
    {
        super(AECSMenus.FREQUENCY_BAND_MENU.get(), id, ip, host);
        this.host = host;

        registerClientAction(TRY_CONNECT_BAND, ByteBufCodecs.STRING_UTF8, this::tryConnectBand);
    }

    @Override
    public void broadcastChanges()
    {
        BroadcastBandsField newBandsInfo = FrequencyBandManager.getBandsInfoByPlayer(getPlayer());
        if (newBandsInfo != null) this.bandsInfo = newBandsInfo;


        super.broadcastChanges();
    }

    // 动作机制-客户端
    public void sendTryConnectBand(String bandId)
    {
        sendClientAction(TRY_CONNECT_BAND, bandId);
    }

    // 动作机制-服务端
    private void tryConnectBand(String bandId)
    {
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        if (band == null) return;

        FrequencyBandLinkMenu.open((ServerPlayer) getPlayer(), getLocator(), bandId);
    }


    @Override
    public EnderBroadcasterBlockEntity getHost()
    {
        return host;
    }

    @Override
    public MenuType<?> getReturnToMenuType()
    {
        return AECSMenus.ENDER_BROADCASTER_MENU.get();
    }
}
