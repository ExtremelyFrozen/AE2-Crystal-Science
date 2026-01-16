package io.github.lounode.ae2cs.common.item.tools;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.Iterator;

@EventBusSubscriber(modid = AECSConstants.MODID)
public final class LinkableToolDropEvents
{
    private LinkableToolDropEvents()
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
        if (!(tool.getItem() instanceof LinkableTool))
        {
            tool = player.getOffhandItem();
            if (!(tool.getItem() instanceof LinkableTool)) return;
        }

        tryInsertDrops(player, tool, event.getDrops().iterator());
    }

    /**
     * 破坏方块收集
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event)
    {
        if (event.getLevel().isClientSide()) return;

        Entity breaker = event.getBreaker();
        if (!(breaker instanceof Player player)) return;

        ItemStack tool = event.getTool();
        if (!(tool.getItem() instanceof LinkableTool)) return;

        tryInsertDrops(player, tool, event.getDrops().iterator());
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