package io.github.lounode.ae2cs.common.item.tools;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface LinkableTool {

    @Nullable
    static GlobalPos getLinkedPositionGlobal(ItemStack item) {
        return ToolLinkableHandler.readLinkedTarget(item);
    }
}
