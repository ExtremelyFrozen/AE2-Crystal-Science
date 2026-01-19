package io.github.lounode.ae2cs.common.item.tools;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class LinkableToolTooltipEvents
{
    private LinkableToolTooltipEvents()
    {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!ToolLinkableHandler.INSTANCE.canLink(stack)) return;

        var targetPos = LinkableTool.getLinkedPositionGlobal(stack);
        List<Component> lines = event.getToolTip();

        var line = (targetPos == null)
                ? Tooltips.of(GuiText.Unlinked, Tooltips.RED)
                : Tooltips.of(GuiText.Linked, Tooltips.GREEN);

        int index = Math.min(2, lines.size());
        lines.add(index, line);
    }
}
