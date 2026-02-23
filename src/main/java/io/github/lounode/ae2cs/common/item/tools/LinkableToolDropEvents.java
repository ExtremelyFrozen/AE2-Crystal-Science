package io.github.lounode.ae2cs.common.item.tools;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID)
public final class LinkableToolDropEvents
{
    private static final Map<UUID, RecentBreak> RECENT_BREAKS = new HashMap<>();

    private LinkableToolDropEvents()
    {
    }

    private record RecentBreak(ResourceKey<Level> dimension, BlockPos pos, long gameTime, boolean offhand)
    {
    }

    /**
     * 击杀生物：把掉落尽可能塞进 ME；塞不完就照常掉落。
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event)
    {
        if (event.getEntity().level().isClientSide()) return;

        Entity src = event.getSource().getEntity();
        if (!(src instanceof Player player)) return;

        ItemStack tool = player.getMainHandItem();
        if (!(ToolLinkableHandler.INSTANCE.canLink(tool)))
        {
            tool = player.getOffhandItem();
            if (!(ToolLinkableHandler.INSTANCE.canLink(tool))) return;
        }

        tryInsertDrops(player, tool, event.getDrops().iterator());
    }

    // 1.20.1没有BlockDropsEvent，只能取巧
    /**
     * 确认破坏的方块
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack tool = player.getMainHandItem();
        boolean offhand = false;
        if (!(ToolLinkableHandler.INSTANCE.canLink(tool)))
        {
            tool = player.getOffhandItem();
            offhand = true;
            if (!(ToolLinkableHandler.INSTANCE.canLink(tool))) return;
        }

        var level = player.level();

        RECENT_BREAKS.put(
                player.getUUID(),
                new RecentBreak(
                        level.dimension(),
                        event.getPos().immutable(),
                        level.getGameTime(),
                        offhand
                )
        );
    }

    /**
     * 收集来自被破坏方块的掉落物
     */
    @SubscribeEvent
    public static void onItemSpawn(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        var level = event.getLevel();
        long now = level.getGameTime();
        ResourceKey<Level> dim = level.dimension();

        // 清理超时未收集的掉落物
        RECENT_BREAKS.entrySet().removeIf(e -> now - e.getValue().gameTime() > 2);

        for (var entry : RECENT_BREAKS.entrySet())
        {
            RecentBreak ctx = entry.getValue();
            if (!ctx.dimension().equals(dim)) continue;
            if (now - ctx.gameTime() > 2) continue;
            if (itemEntity.blockPosition().distSqr(ctx.pos()) > 9) continue;

            if (level.getServer() == null) continue;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.level() != level) continue;

            ItemStack tool = ctx.offhand() ? player.getOffhandItem() : player.getMainHandItem();
            if (!ToolLinkableHandler.INSTANCE.canLink(tool)) continue;

            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;

            long amount = stack.getCount();
            if (amount <= 0) continue;

            AEItemKey key = AEItemKey.of(stack);
            if (key == null) continue;

            long inserted = ToolLinkableHandler.insert(player, tool, key, amount, Actionable.MODULATE);
            if (inserted <= 0) continue;

            long remaining = amount - inserted;
            if (remaining <= 0)
            {
                event.setCanceled(true);
                itemEntity.discard();
            }
            else
            {
                stack.setCount((int) remaining);
                itemEntity.setItem(stack);
            }
            return;
        }
    }

    /**
     * 逐个 ItemEntity 尝试塞进 ME：
     * - 全塞进去：从 iterator 移除（不再生成掉落实体）
     * - 部分塞进去：把 ItemEntity 的 ItemStack count 改成剩余量
     * - 完全塞不进去：不动，照常掉落
     */
    private static void tryInsertDrops(Player player, ItemStack tool, Iterator<ItemEntity> it)
    {
        while (it.hasNext())
        {
            ItemEntity itemEntity = it.next();
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;

            long amount = stack.getCount();
            if (amount <= 0) continue;

            AEItemKey key = AEItemKey.of(stack);
            if (key == null) continue;

            long inserted = ToolLinkableHandler.insert(player, tool, key, amount, Actionable.MODULATE);
            if (inserted <= 0) continue;

            long remaining = amount - inserted;
            if (remaining <= 0)
            {
                // 全部塞进 ME：移除该掉落实体
                it.remove();
                itemEntity.discard();
            }
            else
            {
                // 只塞进去一部分：更新掉落数量
                stack.setCount((int) remaining);
                itemEntity.setItem(stack);
            }
        }
    }

}
