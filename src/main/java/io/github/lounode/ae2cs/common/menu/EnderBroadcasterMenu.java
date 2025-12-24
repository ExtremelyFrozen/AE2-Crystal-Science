package io.github.lounode.ae2cs.common.menu;

import appeng.api.util.IConfigManager;
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

    @GuiSync(10)
    public String bandName = "";

    @GuiSync(11)
    public EnderBroadcasterBlockEntity.ConnectionType connectionType = EnderBroadcasterBlockEntity.ConnectionType.NO_CONNECTION;

    @GuiSync(12)
    public int receiverExpectedChannels = 0;


    public EnderBroadcasterMenu(int id, Inventory playerInv, @NotNull EnderBroadcasterBlockEntity host)
    {
        super(AECSMenus.ENDER_BROADCASTER_MENU.get(), id, playerInv, host);
        registerClientAction(changeExpectedChannelsAction, Integer.class, this::acceptChangeExpectedChannelsAction);
    }


    @Override
    public void broadcastChanges()
    {
        EnderBroadcasterBlockEntity host = getHost();

        this.bandName = host.getBandName();
        this.connectionType = host.getConnectionType();
        this.receiverExpectedChannels = host.getExpectedChannels();

        super.broadcastChanges();
    }

    // 动作机制：客户端发送
    public void sendChangeExpectedChannels(int delta)
    {
        sendClientAction(changeExpectedChannelsAction, delta);
    }

    // 动作机制：服务端处理
    private void acceptChangeExpectedChannelsAction(int delta)
    {
        getHost().setExpectedChannels(receiverExpectedChannels + delta);
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
