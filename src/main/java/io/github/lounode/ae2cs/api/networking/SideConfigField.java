package io.github.lounode.ae2cs.api.networking;

import appeng.menu.guisync.PacketWritable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumMap;

public record SideConfigField(EnumMap<Direction, SidePolicy> sidePolicies,
                              boolean autoImport,
                              boolean autoExport) implements PacketWritable
{
    private static final Codec<EnumMap<Direction, SidePolicy>> SIDE_POLICIES_CODEC =
            Codec.unboundedMap(Direction.CODEC, SidePolicy.CODEC)
                    .xmap(map -> {
                        EnumMap<Direction, SidePolicy> em = new EnumMap<>(Direction.class);
                        em.putAll(map);
                        return em;
                    }, em -> em);

    public static final Codec<SideConfigField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SIDE_POLICIES_CODEC.fieldOf("side_policies").forGetter(SideConfigField::sidePolicies),
            Codec.BOOL.optionalFieldOf("auto_import", false).forGetter(SideConfigField::autoImport),
            Codec.BOOL.optionalFieldOf("auto_export", false).forGetter(SideConfigField::autoExport)
    ).apply(instance, SideConfigField::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SideConfigField> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public SideConfigField(RegistryFriendlyByteBuf buf)
    {
        this(decode(buf));
    }

    /**
     * 用于转发委托
     */
    private SideConfigField(SideConfigField sideConfigField)
    {
        this(sideConfigField.sidePolicies, sideConfigField.autoImport, sideConfigField.autoExport);
    }

    private static SideConfigField decode(RegistryFriendlyByteBuf buf)
    {
        return STREAM_CODEC.decode(buf);
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf)
    {
        STREAM_CODEC.encode(buf, this);
    }
}