package io.github.lounode.ae2cs.common.machine.component;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum SidePolicy implements StringRepresentable
{
    INSERT(true, false),
    EXTRACT(false, true),
    NONE(false, false),
    ALL(true, true);

    final boolean allowInsert;
    final boolean allowExtract;

    SidePolicy(boolean allowInsert, boolean allowExtract)
    {
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    public boolean allowExtract()
    {
        return allowExtract;
    }

    public boolean allowInsert()
    {
        return allowInsert;
    }

    @Override
    public @NotNull String getSerializedName()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}