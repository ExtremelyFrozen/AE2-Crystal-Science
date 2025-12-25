package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.FrequencyBandCreateInfo;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.UUID;

public class FrequencyBandCreateMenu extends AEBaseMenu implements CustomReturnableSubMenu
{
    private static final String CREATE_BAND_ACTION = "create_band";

    private final EnderBroadcasterBlockEntity host;

    public FrequencyBandCreateMenu(int id, Inventory playerInventory, EnderBroadcasterBlockEntity host)
    {
        super(AECSMenus.FREQUENCY_BAND_CREATE_MENU.get(), id, playerInventory, host);
        this.host = host;

        registerClientAction(CREATE_BAND_ACTION, FrequencyBandCreateInfo.class, this::createBand);
    }

    public void sendCreateBand(FrequencyBandCreateInfo info)
    {
        sendClientAction(CREATE_BAND_ACTION, info);
    }

    private void createBand(FrequencyBandCreateInfo info)
    {
        String name = info.name();
        String password = info.password();
        UUID ownerId = info.ownerId();
        boolean isPublic = info.isPublic();
        boolean allowedMemoryCardCopy = info.allowedMemoryCardCopy();

        if (name.isEmpty() || ownerId == null) return;

        if (FrequencyBandManager.isBandPresent(info.name())) return;

        if (FrequencyBandManager.tryCreateBand(name, password, ownerId, isPublic, allowedMemoryCardCopy) != null)
        {
            // 创建成功，关闭此菜单
            getHost().returnToMainMenu(getPlayer(), this);
        }
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
