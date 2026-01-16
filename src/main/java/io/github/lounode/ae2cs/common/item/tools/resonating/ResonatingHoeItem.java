package io.github.lounode.ae2cs.common.item.tools.resonating;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.hooks.IntrinsicEnchantItem;
import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.IntrinsicEnchantment;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResonatingHoeItem extends HoeItem implements LinkableTool, IntrinsicEnchantItem
{
    private final IntrinsicEnchantment intrinsicEnchantment = new IntrinsicEnchantment(Enchantments.EFFICIENCY, 5);

    public ResonatingHoeItem(Properties properties)
    {
        super(AECSToolType.RESONATING.getToolTier(), properties.attributes(createAttributes(AECSToolType.RESONATING.getToolTier(), -4.0F, 0.0F)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> lines, @NotNull TooltipFlag advancedTooltips)
    {
        super.appendHoverText(stack, context, lines, advancedTooltips);

        if (getLinkedPosition(stack) == null)
        {
            lines.add(Tooltips.of(GuiText.Unlinked, Tooltips.RED));
        }
        else
        {
            lines.add(Tooltips.of(GuiText.Linked, Tooltips.GREEN));
        }

        intrinsicEnchantment.appendHoverText(context, lines);
    }

    @Override
    public int getIntrinsicEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment)
    {
        return intrinsicEnchantment.getLevel(enchantment);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack)
    {
        return true;
    }
}