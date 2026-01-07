package io.github.lounode.ae2cs.common.menu;

import appeng.api.util.IConfigManager;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class EnderEmitterMenu extends UpgradeableMenu<EnderEmitterBlockEntity>
{
    private static final String changeDistanceAction = "change_distance";
    private static final String trySacnAllAction = "try_sacn_all";
    private static final String destroyAllAction = "destroy_all";

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

    public EnderEmitterMenu(MenuType<?> menuType, int id, Inventory ip, EnderEmitterBlockEntity host)
    {
        super(menuType, id, ip, host);

        registerClientAction(changeDistanceAction, Integer.class, this::onChangeDistance);
        registerClientAction(trySacnAllAction, this::onSacnAll);
        registerClientAction(destroyAllAction, this::onDestroyAll);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm)
    {
        this.autoMode = getHost().getConfigManager().getSetting(AECSSettings.AUTO_LINK_MODE);
        this.autoLinkCableMode = getHost().getConfigManager().getSetting(AECSSettings.AUTO_LINK_CABLE_MODE);
        this.showRangeMode = getHost().getConfigManager().getSetting(AECSSettings.SHOW_RANGE_MODE);
    }

    @Override
    public void broadcastChanges()
    {
        this.linkDistance = getHost().getLinkDistance();
        this.maxLinkDistance = EnderEmitterBlockEntity.maxLinkDistance;

        super.broadcastChanges();
    }

    public void sendChangeDistance(int delta)
    {
        sendClientAction(changeDistanceAction, delta);
    }

    public void sendSacnAll()
    {
        sendClientAction(trySacnAllAction);
    }

    public void sendDestroyAll()
    {
        sendClientAction(destroyAllAction);
    }

    private void onChangeDistance(int delta)
    {
        getHost().setLinkDistance(this.linkDistance + delta);
    }

    private void onSacnAll()
    {
        EnderEmitterBlockEntity.addAllRecentBEtoEmitter(getHost());
    }

    private void onDestroyAll()
    {
        EnderEmitterBlockEntity.removeAllLinkedFromEmitter(getHost());
    }
}
