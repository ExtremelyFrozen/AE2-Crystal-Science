package io.github.lounode.ae2cs.common.item.tools;

import appeng.api.config.Actionable;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ToolLinkableHandler implements IGridLinkableHandler
{
    public static final IGridLinkableHandler INSTANCE = new ToolLinkableHandler();

    private ToolLinkableHandler()
    {
    }

    @Override
    public boolean canLink(ItemStack stack)
    {
        return stack.getItem() instanceof LinkableTool;
    }

    @Override
    public void link(ItemStack itemStack, GlobalPos pos)
    {
        itemStack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
    }

    @Override
    public void unlink(ItemStack itemStack)
    {
        itemStack.remove(AEComponents.WIRELESS_LINK_TARGET);
    }

    @Nullable
    public static MEStorage getLinkMEStorage(ItemStack stack, Player player)
    {
        if (!(stack.getItem() instanceof LinkableTool)) return null;

        var targetPos = stack.get(AEComponents.WIRELESS_LINK_TARGET);
        if (targetPos == null) return null;

        if (!(player.level() instanceof ServerLevel playerLevel)) return null;

        var server = playerLevel.getServer();

        // 目标维度
        var linkedLevel = server.getLevel(targetPos.dimension());
        if (linkedLevel == null) return null;

        var be = Platform.getTickingBlockEntity(linkedLevel, targetPos.pos());
        if (!(be instanceof IWirelessAccessPoint accessPoint)) return null;

        var grid = accessPoint.getGrid();
        if (grid == null) return null;

        // 不检查距离、维度，直接返回仓库
        return grid.getStorageService().getInventory();
    }

    public static long insert(Player player, ItemStack toolStack, AEKey what, long amount, Actionable mode)
    {
        if (player.level().isClientSide()) return 0;

        var inv = getLinkMEStorage(toolStack, player);
        if (inv == null) return 0;

        return inv.insert(what, amount, mode, new PlayerSource(player));
    }
}