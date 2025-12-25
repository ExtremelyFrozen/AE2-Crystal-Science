package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import appeng.api.util.IConfigManager;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class EnderBroadcasterMenu extends UpgradeableMenu<EnderBroadcasterBlockEntity>
{
    private static final String changeExpectedChannelsAction = "change_expected_channels";
    private static final String openFrequencyBandMenuAction = "open_frequency_band_menu";
    private static final String openFrequencyBandCreateMenuAction = "open_frequency_band_create_menu";

    @GuiSync(10)
    public String bandName = "";

    @GuiSync(11)
    public EnderBroadcasterBlockEntity.ConnectionType connectionType = EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION;

    @GuiSync(12)
    public int receiverExpectedChannels = 0;

    @GuiSync(13)
    public int receiverActualChannels = 0;

    @GuiSync(14)
    public int senderSentChannels = 0;


    public EnderBroadcasterMenu(int id, Inventory playerInv, @NotNull EnderBroadcasterBlockEntity host)
    {
        super(AECSMenus.ENDER_BROADCASTER_MENU.get(), id, playerInv, host);
        registerClientAction(changeExpectedChannelsAction, Integer.class, this::acceptChangeExpectedChannelsAction);
        registerClientAction(openFrequencyBandMenuAction, this::openFrequencyBandMenuAction);
        registerClientAction(openFrequencyBandCreateMenuAction, this::openFrequencyBandCreateMenuAction);
    }


    @Override
    public void broadcastChanges()
    {
        EnderBroadcasterBlockEntity host = getHost();

        this.bandName = host.getBandName();
        this.connectionType = host.getConnectionType();
        this.receiverExpectedChannels = host.getExpectedChannels();
        this.receiverActualChannels = host.isEnabledCustomChannel() ? host.getMaxChannels() : 0; // 即为CustomChannelProvider中被Band设定的值
        this.senderSentChannels = host.getCouldSendChannels();

        super.broadcastChanges();
    }

    // 动作机制：客户端发送
    public void sendChangeExpectedChannels(int delta)
    {
        sendClientAction(changeExpectedChannelsAction, delta);
    }

    public void sendFrequencyBandMenuAction()
    {
        sendClientAction(openFrequencyBandMenuAction);
    }

    public void sendOpenFrequencyBandCreateMenuAction()
    {
        sendClientAction(openFrequencyBandCreateMenuAction);
    }

    // 动作机制：服务端处理
    private void acceptChangeExpectedChannelsAction(int delta)
    {
        getHost().setExpectedChannels(receiverExpectedChannels + delta);
    }

    private void openFrequencyBandMenuAction()
    {
        MenuOpener.open(AECSMenus.FREQUENCY_BAND_MENU.get(), getPlayer(), getLocator());
    }

    private void openFrequencyBandCreateMenuAction()
    {
        MenuOpener.open(AECSMenus.FREQUENCY_BAND_CREATE_MENU.get(), getPlayer(), getLocator());
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return !getHost().isRemoved();
    }
}
