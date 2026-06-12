package io.github.lounode.ae2cs.common.machine.component;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum SidePolicy implements StringRepresentable {

    INSERT(true, false),
    EXTRACT(false, true),
    NONE(false, false),
    ALL(true, true);

    public static final Codec<SidePolicy> CODEC = StringRepresentable.fromEnum(SidePolicy::values);
    public static final StreamCodec<ByteBuf, SidePolicy> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(SidePolicy::valueOf, Enum::name);

    final boolean allowInsert;
    final boolean allowExtract;

    SidePolicy(boolean allowInsert, boolean allowExtract) {
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    public boolean allowExtract() {
        return allowExtract;
    }

    public boolean allowInsert() {
        return allowInsert;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
