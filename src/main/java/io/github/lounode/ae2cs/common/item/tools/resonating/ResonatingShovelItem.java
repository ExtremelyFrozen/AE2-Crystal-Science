package io.github.lounode.ae2cs.common.item.tools.resonating;

import appeng.hooks.IntrinsicEnchantItem;
import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.IntrinsicEnchantment;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ResonatingShovelItem extends ShovelItem implements LinkableTool, IntrinsicEnchantItem
{
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment(Enchantments.SILK_TOUCH, 1);

    public ResonatingShovelItem(Properties properties)
    {
        super(AECSToolType.RESONATING.getToolTier(), 1.5f, -3.0f, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @Nullable Level level,
                                @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced)
    {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        intrinsicEnchantment.appendHoverText(tooltipComponents);
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Enchantment enchantment)
    {
        return intrinsicEnchantment.getLevel(enchantment);
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack)
    {
        // 精准采集只认这一条路径
        // 也许后续可以考虑把精准采集的附魔原生写入，不过这样会影响到tooltip和其他东西
        Map<Enchantment, Integer> base = super.getAllEnchantments(stack);

        if (base.getOrDefault(Enchantments.SILK_TOUCH, 0) >= 1) return base;

        base.put(Enchantments.SILK_TOUCH, 1);
        return base;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack)
    {
        return true;
    }
}