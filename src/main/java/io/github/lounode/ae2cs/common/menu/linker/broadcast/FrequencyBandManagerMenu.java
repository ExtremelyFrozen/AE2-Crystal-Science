package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.FrequencyBandDetailInfo;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.util.ServerUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * 用来管理单一频道，包括修改设置、增改白名单
 */
public class FrequencyBandManagerMenu extends AEBaseMenu implements CustomReturnableSubMenu
{
    private final EnderBroadcasterBlockEntity host;
    private final BroadcastFrequencyBand band;

    @GuiSync(1)
    public FrequencyBandDetailInfo bandDetailInfo;

    public FrequencyBandManagerMenu(int id, Inventory playerInventory, EnderBroadcasterBlockEntity host)
    {
        super(AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get(), id, playerInventory, host);
        this.host = host;
        this.band = FrequencyBandManager.getBand(this.host.getBandName());
    }

    @Override
    public void broadcastChanges()
    {
        if (getPlayer().getServer() != null)
        {
            bandDetailInfo = new FrequencyBandDetailInfo(band.getName(),
                    !band.getPassword().isEmpty(),
                    band.isPublic(),
                    band.isAllowedMemoryCardCopy(),
                    band.getWhiteList().stream().map(uuid -> ServerUtil.getPlayerNameByUUID(uuid, getPlayer().getServer())).toList(),
                    band.getDeclaredSenders().stream().toList(),
                    band.getDeclaredReceivers().stream().toList()
            );
        }
        super.broadcastChanges();
    }

    @Override
    public MenuType<?> getReturnToMenuType()
    {
        return AECSMenus.ENDER_BROADCASTER_MENU.get();
    }

    @Override
    public ISubMenuHost getHost()
    {
        return this.host;
    }
}
