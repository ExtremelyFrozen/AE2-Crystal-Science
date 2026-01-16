package io.github.lounode.ae2cs.common.item.tools.ender;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import io.github.lounode.ae2cs.common.item.tools.AECSToolType;
import io.github.lounode.ae2cs.common.item.tools.LinkableTool;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnderAxeItem extends AxeItem implements LinkableTool
{
    public EnderAxeItem(Properties properties)
    {
        super(AECSToolType.ENDER.getToolTier(), properties.attributes(createAttributes(AECSToolType.ENDER.getToolTier(), 5.0F, -3.0F)));
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
    }
}
