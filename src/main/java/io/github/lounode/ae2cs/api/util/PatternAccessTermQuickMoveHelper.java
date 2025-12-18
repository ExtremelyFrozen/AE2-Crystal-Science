package io.github.lounode.ae2cs.api.util;

import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.world.level.ItemLike;

import java.util.HashSet;
import java.util.Set;

public class PatternAccessTermQuickMoveHelper
{
    private static Set<ItemLike> allowedItems = new HashSet<>();

    public static void init()
    {
        add(AECSBlocks.METEORITE_CRAFTER_BLOCK);
    }

    public static void add(ItemLike item)
    {
        allowedItems.add(item.asItem());
    }

    public static boolean contains(ItemLike item)
    {
        return allowedItems.contains(item.asItem());
    }
}
