package io.github.lounode.ae2cs.common.menu;

import appeng.api.util.IConfigManager;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnderEmitterMenu extends UpgradeableMenu<EnderEmitterBlockEntity>
{
    private static final String changeAutoModeAction = "change_auto_mode";
    private static final String changeDistanceAction = "change_distance";
    private static final String changeAllowAutoLinkCableAction = "change_allow_auto_link_cable";

    @GuiSync(10)
    public int linkDistance;

    @GuiSync(11)
    public boolean autoMode;

    @GuiSync(12)
    public boolean allowAutoLinkCable;

    @GuiSync(13)
    public int maxLinkDistance;

    public EnderEmitterMenu(MenuType<?> menuType, int id, Inventory ip, EnderEmitterBlockEntity host)
    {
        super(menuType, id, ip, host);

        registerClientAction(changeAutoModeAction, Boolean.class, this::onChangeAutoMode);
        registerClientAction(changeDistanceAction, Integer.class, this::onChangeDistance);
        registerClientAction(changeAllowAutoLinkCableAction, Boolean.class, this::onChangeAllowAutoLinkCable);
    }

    @Override
    public void broadcastChanges()
    {
        this.linkDistance = getHost().getLinkDistance();
        this.autoMode = getHost().isAutoMode();
        this.allowAutoLinkCable = getHost().allowAutoLinkCableLike();
        this.maxLinkDistance = EnderEmitterBlockEntity.maxLinkDistance;

        super.broadcastChanges();
    }

    public void sendChangeAutoMode(boolean autoMode)
    {
        sendClientAction(changeAutoModeAction, autoMode);
    }

    public void sendChangeDistance(int delta)
    {
        sendClientAction(changeDistanceAction, delta);
    }

    public void sendAllowAutoLinkCable(boolean allowAutoLinkCable)
    {
        sendClientAction(changeAllowAutoLinkCableAction, allowAutoLinkCable);
    }

    private void onChangeAutoMode(boolean autoMode)
    {
        getHost().setAutoMode(autoMode);
    }

    private void onChangeDistance(int delta)
    {
        getHost().setLinkDistance(this.linkDistance + delta);
    }

    private void onChangeAllowAutoLinkCable(boolean allowAutoLinkCable)
    {
        getHost().setAllowAutoLinkCableLike(allowAutoLinkCable);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {

    }
}
