package io.github.lounode.ae2cs.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PureCrystalItem extends Item {

    public static double energyMultiplier = 1;

    private static final double DEFAULT_ENERGY_PER_TICK = 1000;
    private static final int DEFAULT_BURN_TIME = 600;

    private final double energyPerTick;
    private final int burnTime;

    public PureCrystalItem(Item.Properties properties) {
        this(properties, DEFAULT_ENERGY_PER_TICK, DEFAULT_BURN_TIME);
    }

    public PureCrystalItem(Item.Properties properties, double energyPerTick, int burnTime) {
        super(properties);
        this.energyPerTick = energyPerTick;
        this.burnTime = burnTime;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @Nullable Level level,
                                @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("message.ae2cs.tooltip.burn_time", burnTime).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("message.ae2cs.tooltip.energy", String.format("%.0f", energyPerTick)).withStyle(ChatFormatting.GRAY));
    }

    public double getEnergyPerTick() {
        return energyPerTick * energyMultiplier;
    }

    public int getBurnTime() {
        return burnTime;
    }
}
