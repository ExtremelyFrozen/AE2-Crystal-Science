package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.parts.IPartHost;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 保存谐振绑定器当前编辑的供应器位置；部件供应器还会记录其所在面。
 */
public record ResonatingPatternProviderReference(GlobalPos pos, @Nullable Direction side) {

    public static final Codec<ResonatingPatternProviderReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(ResonatingPatternProviderReference::pos),
            Direction.CODEC.optionalFieldOf("side").forGetter(reference -> Optional.ofNullable(reference.side())))
            .apply(instance, (pos, side) -> new ResonatingPatternProviderReference(pos, side.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonatingPatternProviderReference> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC,
            ResonatingPatternProviderReference::pos,
            ByteBufCodecs.optional(ByteBufCodecs.idMapper(Direction.BY_ID, Direction::get3DDataValue)),
            reference -> Optional.ofNullable(reference.side()),
            (pos, side) -> new ResonatingPatternProviderReference(pos, side.orElse(null)));

    /**
     * 解析当前已加载世界中的供应器。客户端仅解析当前维度，服务端可解析其他已加载维度。
     */
    public @Nullable ResonatingPatternProviderHost resolve(Level currentLevel) {
        Level level = currentLevel;
        if (!level.dimension().equals(pos.dimension())) {
            MinecraftServer server = level.getServer();
            if (server == null) {
                return null;
            }
            level = server.getLevel(pos.dimension());
            if (level == null) {
                return null;
            }
        }

        if (!level.hasChunkAt(pos.pos())) {
            return null;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos.pos());
        if (side == null) {
            return blockEntity instanceof ResonatingPatternProviderHost host ? host : null;
        }
        if (blockEntity instanceof IPartHost partHost) {
            return partHost.getPart(side) instanceof ResonatingPatternProviderHost host ? host : null;
        }
        return null;
    }
}
