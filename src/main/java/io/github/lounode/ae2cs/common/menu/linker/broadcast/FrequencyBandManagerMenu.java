package io.github.lounode.ae2cs.common.menu.linker.broadcast;

import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.networking.FrequencyBandDetailInfo;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenu;
import io.github.lounode.ae2cs.api.util.GlobalPosJson;
import io.github.lounode.ae2cs.common.block.entity.EnderBroadcasterBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSMenus;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.List;
import java.util.Set;

/**
 * 用来管理单一频道，包括修改设置、增改白名单
 */
public class FrequencyBandManagerMenu extends AEBaseMenu implements CustomReturnableSubMenu {

    private static final String changePasswordAction = "change_password";
    private static final String changePublicAction = "change_public";
    private static final String changeAllowMemoryCardAction = "change_allow_memory_card";
    private static final String tryDisconnectBroadcasterAction = "try_disconnect_broadcast";
    private static final String tryTapToBroadcasterAction = "try_tap_to_broadcaster";
    private static final String openBandWhiteManagerAction = "open_band_white_manager";
    private static final String removeBandAction = "remove_band";

    private final EnderBroadcasterBlockEntity host;
    private final BroadcastFrequencyBand band;
    private int tick = 5; // 作限流，减少同步频率（限流之后UI手感太tm奇怪了，所以实际上我还没有应用它）

    @GuiSync(1)
    public FrequencyBandDetailInfo bandDetailInfo = new FrequencyBandDetailInfo("", false, false, false, BroadcastFrequencyBand.BandError.NO_SENDER, List.of(), List.of(), List.of());

    @GuiSync(2)
    public long usableChannels = 0;

    @GuiSync(3)
    public long usedChannels = 0;

    public FrequencyBandManagerMenu(int id, Inventory playerInventory, EnderBroadcasterBlockEntity host) {
        super(AECSMenus.FREQUENCY_BAND_MANAGER_MENU.get(), id, playerInventory, host);
        this.host = host;
        this.band = FrequencyBandManager.getBand(this.host.getBandName());

        registerClientAction(changePasswordAction, String.class, this::changePasswordAction);
        registerClientAction(changePublicAction, Boolean.class, this::changePublicAction);
        registerClientAction(changeAllowMemoryCardAction, Boolean.class, this::changeAllowMemoryCardAction);
        registerClientAction(tryDisconnectBroadcasterAction, GlobalPosJson.class, this::tryDisconnectBroadcasterAction);
        registerClientAction(tryTapToBroadcasterAction, GlobalPosJson.class, this::tryTapToBroadcasterAction);
        registerClientAction(openBandWhiteManagerAction, this::openBandWhiteManagerAction);
        registerClientAction(removeBandAction, this::onRemoveBand);
    }

    // 动作机制-客户端
    public void sendChangePasswordAction(String newPassword) {
        sendClientAction(changePasswordAction, newPassword);
    }

    public void sendChangePublicAction(boolean publicMode) {
        sendClientAction(changePublicAction, publicMode);
    }

    public void sendChangeAllowMemoryCardAction(boolean allowMemoryCard) {
        sendClientAction(changeAllowMemoryCardAction, allowMemoryCard);
    }

    public void sendDisconnectBroadcasterAction(GlobalPos pos) {
        GlobalPosJson json = GlobalPosJson.from(pos);
        sendClientAction(tryDisconnectBroadcasterAction, json);
    }

    public void sendTapToBroadcasterAction(GlobalPos pos) {
        GlobalPosJson json = GlobalPosJson.from(pos);
        sendClientAction(tryTapToBroadcasterAction, json);
    }

    public void sendOpenBandManagerMenu() {
        sendClientAction(openBandWhiteManagerAction);
    }

    public void sendDeleteBand() {
        sendClientAction(removeBandAction);
    }

    // 动作机制-服务端
    private void changePasswordAction(String newPassword) {
        if (band.getOwner().equals(getPlayer().getUUID())) {
            band.setPassword(newPassword);
        } else {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.frequency_manager.you_not_owner"), true);
        }
    }

    private void changePublicAction(boolean publicMode) {
        if (band.getOwner().equals(getPlayer().getUUID())) {
            band.setPublic(publicMode);
        } else {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.frequency_manager.you_not_owner"), true);
        }
    }

    private void changeAllowMemoryCardAction(boolean allowMemoryCard) {
        if (band.getOwner().equals(getPlayer().getUUID())) {
            band.setAllowedMemoryCardCopy(allowMemoryCard);
        } else {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.frequency_manager.you_not_owner"), true);
        }
    }

    private void tryDisconnectBroadcasterAction(GlobalPosJson jsonPos) {
        MinecraftServer server = getPlayer().getServer();
        if (server == null) return;

        GlobalPos globalPos = jsonPos.toGlobalPos();
        ServerLevel level = server.getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();
        if (level == null) return;

        if (!level.isLoaded(pos)) {
            level.getChunk(pos);
        }
        if (!(level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be)) return;

        be.cleanConnectionPermanent();
    }

    private void tryTapToBroadcasterAction(GlobalPosJson jsonPos) {
        MinecraftServer server = getPlayer().getServer();
        if (server == null) return;

        GlobalPos globalPos = jsonPos.toGlobalPos();
        ServerLevel level = server.getLevel(globalPos.dimension());
        BlockPos pos = globalPos.pos();
        if (level == null) return;

        if (!level.isLoaded(pos)) {
            level.getChunk(pos);
        }
        if (!(level.getBlockEntity(pos) instanceof EnderBroadcasterBlockEntity be)) return;

        if (!getPlayer().hasPermissions(2)) return;

        getPlayer().closeContainer();
        getPlayer().teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), getPlayer().getYRot(), getPlayer().getXRot());
    }

    private void openBandWhiteManagerAction() {
        MenuOpener.open(AECSMenus.BAND_WHITE_LIST_MANAGER_MENU.get(), getPlayer(), getLocator());
    }

    private void onRemoveBand() {
        if (band.getOwner().equals(getPlayer().getUUID())) {
            getPlayer().closeContainer();
            FrequencyBandManager.deleteBand(band.getName());
        } else {
            getPlayer().displayClientMessage(Component.translatable("ae2cs.msg.frequency_manager.you_not_owner"), true);
        }
    }

    @Override
    public void broadcastChanges() {
        this.tick++;
        if (this.tick >= 1 && getPlayer().getServer() != null) {
            bandDetailInfo = new FrequencyBandDetailInfo(band.getName(),
                    !band.getPassword().isEmpty(),
                    band.isPublic(),
                    band.isAllowedMemoryCardCopy(),
                    band.getErrorState(),
                    List.of(), // 这个UI中我们用不到它
                    band.getDeclaredSenders().stream().toList(),
                    band.getDeclaredReceivers().stream().toList());
            this.tick = 0;
        }

        usableChannels = band.getUsableChannels();
        usedChannels = band.getUsedChannels();

        super.broadcastChanges();
    }

    @Override
    public MenuType<?> getReturnToMenuType() {
        return AECSMenus.ENDER_BROADCASTER_MENU.get();
    }

    @Override
    public ISubMenuHost getHost() {
        return this.host;
    }
}
