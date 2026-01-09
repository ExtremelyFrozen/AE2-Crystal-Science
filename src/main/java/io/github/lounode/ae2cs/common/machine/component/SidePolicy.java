package io.github.lounode.ae2cs.common.machine.component;

public enum SidePolicy
{
    INSERT(true, false),
    EXTRACT(false, true),
    NONE(false, false),
    ALL(false, true);


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
}