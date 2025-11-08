package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.api.AE2CrystalSeedsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lounode.ae2cs.common.util.resourcelocation.ResourceLocationUtil.prefix;

public class CrystalSeedItem extends Item {

    public static final DataComponentType<Integer> GROW_PROCESS =
            DataComponentType.<Integer>builder()
                    .persistent(ExtraCodecs.POSITIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build();

    public static final int OVERGROW_TICK = 900;

    private final Item growTo;
    private final int overGrowTick;

    public CrystalSeedItem(Properties properties, Item growTo) {
        this(properties, growTo, OVERGROW_TICK);
    }

    public CrystalSeedItem(Properties properties, Item growTo, int overGrowTick) {
        super(properties.component(GROW_PROCESS, 0));
        this.growTo = growTo;
        this.overGrowTick = overGrowTick;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(
                Component.translatable("message.ae2_crystal_seeds.tooltip.seed_growth", String.format("%.2f", getGrowProcess(stack) * 100))
                        .withStyle(ChatFormatting.GRAY));
    }

    public int getGrowTicks(ItemStack stack) {
        if (stack.getItem() instanceof CrystalSeedItem) {
            return stack.getOrDefault(GROW_PROCESS, 0);
        }
        return 0;
    }

    public void setGrowTicks(ItemStack stack, int tick) {
        stack.set(GROW_PROCESS, tick);
    }

    public Item getGrowTo() {
        return this.growTo;
    }

    public int getOvergrowTick() {
        return this.overGrowTick;
    }

    public float getGrowProcess(ItemStack stack) {
        return Mth.clamp((float) getGrowTicks(stack) / getOvergrowTick(), 0F, 1.0F);
    }

    @EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onItemEntityTick(EntityTickEvent.Post event) {
            Entity entity = event.getEntity();
            if (!(entity instanceof ItemEntity itemEntity)) {
                return;
            }
            if (!itemEntity.isInWater()) {
                return;
            }
            ItemStack stack = itemEntity.getItem();
            if (!(stack.getItem() instanceof CrystalSeedItem seedItem)) {
                return;
            }

            int ticksExcited = seedItem.getGrowTicks(stack);
            if (ticksExcited < seedItem.getOvergrowTick()) {
                seedItem.setGrowTicks(stack, ++ticksExcited);
            } else {
                ItemStack newStack = new ItemStack(seedItem.getGrowTo());
                newStack.setCount(stack.getCount());
                itemEntity.setItem(newStack);
            }
        }
    }

    public static class ClientEventHandler {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                Set<CrystalSeedItem> seeds = BuiltInRegistries.ITEM.stream()
                        .filter(i -> AE2CrystalSeedsAPI.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
                        .filter(i -> i instanceof CrystalSeedItem)
                        .map(item -> (CrystalSeedItem) item)
                        .collect(Collectors.toSet());

                for (var seed : seeds) {
                    ItemProperties.register(seed, prefix("age"), (stack, level, player, s) -> {
                        return seed.getGrowProcess(stack);
                    });
                }
            });
        }
    }
}
