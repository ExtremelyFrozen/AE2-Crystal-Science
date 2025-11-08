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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.function.Supplier;

public class CrystalSeedItem extends Item
{
    private static final int OVERGROW_TICK = 900;

    private final Supplier<Item> growTo;
    private final int overGrowTick;

    public CrystalSeedItem(Properties properties, Supplier<Item> growTo)
    {
        this(properties, growTo, OVERGROW_TICK);
    }

    public CrystalSeedItem(Properties properties, Supplier<Item> growTo, int overGrowTick)
    {
        super(properties.component(AECSDataComponents.GROW_PROCESS, 0));
        this.growTo = growTo;
        this.overGrowTick = overGrowTick;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(
                Component.translatable("message.ae2_crystal_seeds.tooltip.seed_growth", String.format("%.2f", getGrowProcess(stack) * 100))
                        .withStyle(ChatFormatting.GRAY));
    }

    public int getGrowTicks(ItemStack stack)
    {
        if (stack.getItem() instanceof CrystalSeedItem)
        {
            return stack.getOrDefault(AECSDataComponents.GROW_PROCESS, 0);
        }
        return 0;
    }

    public void setGrowTicks(ItemStack stack, int tick)
    {
        stack.set(AECSDataComponents.GROW_PROCESS, tick);
    }

    public Item getGrowTo()
    {
        return this.growTo.get();
    }

    public int getOvergrowTick()
    {
        return this.overGrowTick;
    }

    public float getGrowProcess(ItemStack stack)
    {
        return Mth.clamp((float) getGrowTicks(stack) / getOvergrowTick(), 0F, 1.0F);
    }

    @EventBusSubscriber
    public static class EventHandler
    {

        @SubscribeEvent
        public static void onItemEntityTick(EntityTickEvent.Post event)
        {
            Entity entity = event.getEntity();
            if (!(entity instanceof ItemEntity itemEntity))
            {
                return;
            }
            if (!itemEntity.isInWater())
            {
                return;
            }
            ItemStack stack = itemEntity.getItem();
            if (!(stack.getItem() instanceof CrystalSeedItem seedItem))
            {
                return;
            }

            int ticksExcited = seedItem.getGrowTicks(stack);
            if (ticksExcited < seedItem.getOvergrowTick())
            {
                seedItem.setGrowTicks(stack, ++ticksExcited);
            }
            else
            {
                ItemStack newStack = new ItemStack(seedItem.getGrowTo());
                newStack.setCount(stack.getCount());
                itemEntity.setItem(newStack);
            }
        }
    }
}
