package io.github.lounode.ae2cs.common.item;

import io.github.lounode.ae2cs.Config;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class PureCrystalItem extends Item
{
    private static final double DEFAULT_ENERGY_PER_TICK = 1000;
    private static final int DEFAULT_BURN_TIME = 600;

    private final double energyPerTick;
    private final int burnTime;

    public PureCrystalItem(Item.Properties properties)
    {
        this(properties, DEFAULT_ENERGY_PER_TICK, DEFAULT_BURN_TIME);
    }

    public PureCrystalItem(Item.Properties properties, double energyPerTick, int burnTime)
    {
        super(properties.component(AECSDataComponents.GROW_PROCESS, 0));
        this.energyPerTick = energyPerTick * Config.INSTANCE.startUpConfig.pureCrystalBurnMultiplier.getAsDouble();
        this.burnTime = burnTime;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        builder.accept(Component.translatable("message.ae2cs.tooltip.burn_time", burnTime).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("message.ae2cs.tooltip.energy", String.format("%.0f", energyPerTick)).withStyle(ChatFormatting.GRAY));
    }

    public double getEnergyPerTick()
    {
        return energyPerTick;
    }

    public int getBurnTime()
    {
        return burnTime;
    }
}
