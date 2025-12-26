package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import appeng.api.networking.IGrid;
import appeng.api.networking.pathing.ControllerState;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.locator.MenuHostLocator;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.FrequencyBandLinkInfo;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * 用于链接以及输入密码的菜单
 */
public class FrequencyBandLinkMenu extends AEBaseMenu implements CustomReturnableSubMenu
{
    private static final String LINK_TO_BAND = "link_to_band";

    private final EnderBroadcasterBlockEntity host;

    @GuiSync(1)
    public String selectedBand = "";

    @GuiSync(2)
    public boolean connected = false;

    public FrequencyBandLinkMenu(int id, Inventory playerInventory, EnderBroadcasterBlockEntity host)
    {
        super(AECSMenus.FREQUENCY_BAND_LINK_MENU.get(), id, playerInventory, host);
        this.host = host;

        registerClientAction(LINK_TO_BAND, FrequencyBandLinkInfo.class, this::linkToBand);
    }

    // 动作机制-客户端
    public void sendLinkToBand(FrequencyBandLinkInfo info)
    {
        sendClientAction(LINK_TO_BAND, info);
    }

    // 动作机制-服务端
    private void linkToBand(FrequencyBandLinkInfo info)
    {
        if (selectedBand == null || selectedBand.isEmpty()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(this.selectedBand);
        if (band == null) return;
        if (info == null) return;

        String password = info.password();
        boolean asSender = info.asSender();

        boolean permissionValid = band.validWhiteList(getPlayer().getUUID()) || band.validPassword(password);

        if (permissionValid)
        {
            IGrid bandGrid = band.getBindGrid();
            IGrid hostGrid = host.getMainNode().getGrid();
            // 进行状态验证
            if (asSender)
            {
                if (hostGrid == null) return;

                if (hostGrid.getPathingService().getControllerState() != ControllerState.CONTROLLER_ONLINE)
                {
                    getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.band_link.sender.grid_must_has_control"), true);
                    return;
                }
                if (bandGrid != null && bandGrid != hostGrid)
                {
                    getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.band_link.sender.grid_conflict"), true);
                    return;
                }
            }
            else
            {
                if (hostGrid != null
                        && hostGrid.getPathingService().getControllerState() == ControllerState.CONTROLLER_ONLINE
                        && hostGrid != bandGrid)
                {
                    getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.band_link.receiver.grid_conflict"), true);
                    return;
                }
            }
            getHost().connectToBand(this.selectedBand, asSender);
        }
        else
        {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.band_link.password_error"), true);
            return;
        }
    }

    @Override
    public void broadcastChanges()
    {
        connected = (this.selectedBand != null && !this.selectedBand.isEmpty() && this.selectedBand.equals(this.host.getBandName()));

        super.broadcastChanges();
    }

    public static void open(ServerPlayer player, MenuHostLocator locator, String selectedBand)
    {
        MenuOpener.open(AECSMenus.FREQUENCY_BAND_LINK_MENU.get(), player, locator);

        if (player.containerMenu instanceof FrequencyBandLinkMenu menu)
        {
            menu.setSelectedBand(selectedBand);
            menu.broadcastChanges();
        }
    }

    private void setSelectedBand(String band)
    {
        selectedBand = band;
    }

    @Override
    public MenuType<?> getReturnToMenuType()
    {
        return AECSMenus.FREQUENCY_BAND_MENU.get();
    }

    @Override
    public EnderBroadcasterBlockEntity getHost()
    {
        return this.host;
    }
}
