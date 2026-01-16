package io.github.lounode.ae2cs.common.item.tools;

import appeng.api.ids.AEComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;


public interface LinkableTool
{
    @Nullable
    default GlobalPos getLinkedPosition(ItemStack item)
    {
        return item.get(AEComponents.WIRELESS_LINK_TARGET);
    }
}
