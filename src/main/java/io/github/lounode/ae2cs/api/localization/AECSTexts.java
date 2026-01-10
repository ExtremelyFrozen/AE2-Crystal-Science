package io.github.lounode.ae2cs.api.localization;

import appeng.api.orientation.RelativeSide;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class AECSTexts
{
    public static Component directionName(Direction dir)
    {
        return Component.translatable("ae2cs.text.direction." + dir.getSerializedName().toLowerCase(Locale.ROOT));
    }

    public static Component relativeSideName(RelativeSide side)
    {
        return Component.translatable("ae2cs.text.relative_side." + side.name().toLowerCase(Locale.ROOT));
    }

    public static Component sidePolicyName(SidePolicy policy)
    {
        return Component.translatable("ae2cs.text.side_policy." + policy.name().toLowerCase(Locale.ROOT));
    }

}
