package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import appeng.api.util.IConfigManager;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class EnderBroadcasterMenu extends UpgradeableMenu<EnderBroadcasterBlockEntity>
{
    private static final ClientActionKey<Integer> changeExpectedChannelsAction = new ClientActionKey<>("change_expected_channels");
    private static final ClientActionKey<Void> openFrequencyBandMenuAction = new ClientActionKey<>("open_frequency_band_menu");
    private static final ClientActionKey<Void> openFrequencyBandCreateMenuAction = new ClientActionKey<>("open_frequency_band_create_menu");
    private static final ClientActionKey<Void> openFrequencyBandManagerMenuAction = new ClientActionKey<>("open_frequency_band_manager_menu");
    private static final ClientActionKey<Void> toggleLinkerSideAction = new ClientActionKey<>("toggle_linker_side");
    private static final ClientActionKey<Void> cleanLinkerConnectionAction = new ClientActionKey<>("clean_linker_connection");

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
        registerClientAction(changeExpectedChannelsAction, ByteBufCodecs.INT, this::acceptChangeExpectedChannelsAction);
        registerClientAction(openFrequencyBandMenuAction, this::openFrequencyBandMenuAction);
        registerClientAction(openFrequencyBandCreateMenuAction, this::openFrequencyBandCreateMenuAction);
        registerClientAction(openFrequencyBandManagerMenuAction, this::openFrequencyBandManagerMenuAction);
        registerClientAction(toggleLinkerSideAction, this::toggleLinkerSideAction);
        registerClientAction(cleanLinkerConnectionAction, this::cleanLinkerConnectionAction);
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

    public void sendOpenFrequencyBandManagerMenuAction()
    {
        sendClientAction(openFrequencyBandManagerMenuAction);
    }

    public void sendToggleLinkerSideAction()
    {
        sendClientAction(toggleLinkerSideAction);
    }

    public void sendCleanLinkerConnectionAction()
    {
        sendClientAction(cleanLinkerConnectionAction);
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

    private void openFrequencyBandManagerMenuAction()
    {
        if (bandName != null && !bandName.isEmpty())
        {
            MenuOpener.open(AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get(), getPlayer(), getLocator());
        }
        else
        {
            getPlayer().sendOverlayMessage(Component.translatable("ae2cs.msg.broadcaster.sender.not_connect_any_band"));
        }
    }

    private void toggleLinkerSideAction()
    {
        if (bandName == null || bandName.isEmpty()) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandName);
        if (band == null) return;

        String targetBand = band.getName();
        if (getHost().getConnectionType() == EnderBroadcasterBlockEntity.ConnectionType.AS_RECEIVER)
        {
            getHost().cleanConnectionPermanent();
            getHost().connectToBand(targetBand, true);
        }
        else if (getHost().getConnectionType() == EnderBroadcasterBlockEntity.ConnectionType.AS_SENDER)
        {
            getHost().cleanConnectionPermanent();
            getHost().connectToBand(targetBand, false);
        }
    }

    private void cleanLinkerConnectionAction()
    {
        if (getHost().getBandName() != null && !getHost().getBandName().isEmpty())
        {
            getHost().cleanConnectionPermanent();
        }
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
