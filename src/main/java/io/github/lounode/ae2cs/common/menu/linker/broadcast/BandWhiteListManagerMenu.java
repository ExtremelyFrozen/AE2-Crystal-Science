package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.networking.ServerPlayerInfo;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.util.ServerUtil;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

/**
 * 用来管理白名单
 */
public class BandWhiteListManagerMenu extends AEBaseMenu implements CustomReturnableSubMenu {

    private static final String changeWhiteListStateAction = "change_white_list_state";

    private final EnderBroadcasterBlockEntity host;
    private final BroadcastFrequencyBand band;

    @GuiSync(1)
    public ServerPlayerInfo whiteListInfo = new ServerPlayerInfo(new LinkedHashMap<>());

    @GuiSync(2)
    public ServerPlayerInfo otherPlayerInfo = new ServerPlayerInfo(new LinkedHashMap<>());

    public BandWhiteListManagerMenu(int id, Inventory playerInventory, EnderBroadcasterBlockEntity host) {
        super(AECSMenus.BAND_WHITE_LIST_MANAGER_MENU.get(), id, playerInventory, host);

        this.host = host;
        this.band = FrequencyBandManager.getBand(this.host.getBandName());

        registerClientAction(changeWhiteListStateAction, UUID.class, this::onChangeWhiteListStateAction);
    }

    public void sendChangeWhiteListStateAction(UUID playerId) {
        sendClientAction(changeWhiteListStateAction, playerId);
    }

    private void onChangeWhiteListStateAction(UUID playerId) {
        MinecraftServer server = getPlayer().getServer();
        UUID ownerId = band.getOwner();
        if (ownerId.equals(getPlayer().getUUID())) {
            if (!ownerId.equals(playerId)) {
                if (band.validWhiteList(playerId))
                    band.removeFromWhiteList(playerId);
                else if (server != null && server.getPlayerList().getPlayer(playerId) != null)
                    band.addToWhiteList(playerId);
            }
        } else {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.frequency_manager.you_not_owner"), true);
        }
    }

    @Override
    public void broadcastChanges() {
        MinecraftServer server = getPlayer().getServer();
        if (server != null) {
            // 白名单信息
            LinkedHashMap<UUID, String> wlMap = new LinkedHashMap<>();
            Set<UUID> wl = band.getWhiteList();
            for (UUID uuid : wl) {
                wlMap.put(uuid, ServerUtil.getPlayerNameByUUID(uuid, server));
            }
            this.whiteListInfo = new ServerPlayerInfo(wlMap);

            // 其它玩家信息 在线且不在白名单且不是owner
            LinkedHashMap<UUID, String> otherMap = new LinkedHashMap<>();
            UUID owner = band.getOwner();

            for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                UUID id = sp.getUUID();
                if (id.equals(owner)) continue;
                if (wl.contains(id)) continue;

                otherMap.put(id, sp.getGameProfile().getName());
            }
            this.otherPlayerInfo = new ServerPlayerInfo(otherMap);
        }
        super.broadcastChanges();
    }

    @Override
    public MenuType<?> getReturnToMenuType() {
        return AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get();
    }

    @Override
    public ISubMenuHost getHost() {
        return this.host;
    }
}
