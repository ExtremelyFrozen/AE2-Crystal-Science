package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CrystalSeedItem extends Item
{
    private static final int DEFAULT_OVERGROW_TICK = 600;

    private final Supplier<? extends Item> growTo;
    private final int overGrowTick;

    public CrystalSeedItem(Properties properties, Supplier<? extends Item> growTo)
    {
        this(properties, growTo, DEFAULT_OVERGROW_TICK);
    }

    public CrystalSeedItem(Properties properties, Supplier<? extends Item> growTo, int overGrowTick)
    {
        super(properties.component(AECSDataComponents.GROW_PROCESS, 0));
        this.growTo = growTo;
        this.overGrowTick = overGrowTick;
    }

    @Override
    public void appendHoverText(ItemStack itemStack,
                                TooltipContext context,
                                TooltipDisplay display,
                                Consumer<Component> builder,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        builder.accept(
                Component.translatable("message.ae2_crystal_seeds.tooltip.seed_growth", String.format("%.2f", getGrowProcess(itemStack) * 100))
                        .withStyle(ChatFormatting.GRAY));
    }

    /**
     * 获取经过的生长刻
     */
    public int getGrowTicks(ItemStack stack)
    {
        if (stack.getItem() instanceof CrystalSeedItem)
        {
            return stack.getOrDefault(AECSDataComponents.GROW_PROCESS, 0);
        }
        return 0;
    }

    /**
     * 设置经过的生长刻
     */
    public void setGrowTicks(ItemStack stack, int tick)
    {
        stack.set(AECSDataComponents.GROW_PROCESS, tick);
    }

    /**
     * 获取完全生长后的物品
     */
    public Item getGrowTo()
    {
        return this.growTo.get();
    }

    /**
     * 获取生长所需刻数
     */
    public int getOvergrowTick()
    {
        return this.overGrowTick;
    }

    /**
     * 获取百分比的生长进度
     */
    public float getGrowProcess(ItemStack stack)
    {
        return Mth.clamp((float) getGrowTicks(stack) / getOvergrowTick(), 0F, 1.0F);
    }

    /**
     * 使目标生长一定刻，并返回结束后的物品堆
     */
    public static @NotNull ItemStack grow(@NotNull ItemStack stack, int ticks)
    {
        if (!(stack.getItem() instanceof CrystalSeedItem seedItem)) return stack;

        int ticksExcited = seedItem.getGrowTicks(stack);
        seedItem.setGrowTicks(stack, ticksExcited + ticks);

        if (ticksExcited + ticks >= seedItem.getOvergrowTick())
        {
            ItemStack newStack = new ItemStack(seedItem.getGrowTo());
            newStack.setCount(stack.getCount());
            return newStack;
        }
        return stack;
    }

    @EventBusSubscriber
    public static class EventHandler
    {
        /**
         * 处理水中生长的情况
         */
        @SubscribeEvent
        public static void onItemEntityTick(EntityTickEvent.Post event)
        {
            Entity entity = event.getEntity();
            if (!(entity instanceof ItemEntity itemEntity)) return;
            if (!itemEntity.isInWater()) return;

            ItemStack stack = itemEntity.getItem();
            itemEntity.setItem(grow(stack, 1));
        }
    }
}
