package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.parts.IPartHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record MirroredPatternProviderTarget(GlobalPos pos, @Nullable Direction side) {

    private static final String TAG_MIRROR = "mirror_target";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POS = "pos";
    private static final String TAG_HAS_SIDE = "has_side";
    private static final String TAG_SIDE = "side";

    public static final Codec<MirroredPatternProviderTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(MirroredPatternProviderTarget::pos),
            Direction.CODEC.optionalFieldOf("side").forGetter(target -> Optional.ofNullable(target.side()))).apply(instance, (pos, side) -> new MirroredPatternProviderTarget(pos, side.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, MirroredPatternProviderTarget> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC,
            MirroredPatternProviderTarget::pos,
            ByteBufCodecs.optional(ByteBufCodecs.idMapper(Direction.BY_ID, Direction::get3DDataValue)),
            target -> Optional.ofNullable(target.side()),
            (pos, side) -> new MirroredPatternProviderTarget(pos, side.orElse(null)));

    public static @Nullable MirroredPatternProviderTarget read(CompoundTag root) {
        if (!root.contains(TAG_MIRROR, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag tag = root.getCompound(TAG_MIRROR);
        if (!tag.contains(TAG_DIMENSION, Tag.TAG_STRING)) {
            return null;
        }

        ResourceLocation dimId = ResourceLocation.tryParse(tag.getString(TAG_DIMENSION));
        if (dimId == null) {
            return null;
        }

        Direction readSide = null;
        if (tag.getBoolean(TAG_HAS_SIDE)) {
            if (!tag.contains(TAG_SIDE, Tag.TAG_INT)) {
                return null;
            }
            int sideId = tag.getInt(TAG_SIDE);
            if (sideId < 0 || sideId >= Direction.values().length) {
                return null;
            }
            readSide = Direction.from3DDataValue(sideId);
        }

        var pos = readTargetPosition(tag);
        return pos.isEmpty() ? null : new MirroredPatternProviderTarget(
                GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimId), pos.get()), readSide);
    }

    private static Optional<BlockPos> readTargetPosition(CompoundTag tag) {
        if (!tag.contains(TAG_POS, Tag.TAG_COMPOUND)) {
            return NbtUtils.readBlockPos(tag, TAG_POS);
        }

        // Older saves may use a compound instead of the current three-element integer array.
        CompoundTag pos = tag.getCompound(TAG_POS);
        if (!pos.contains("X", Tag.TAG_INT) || !pos.contains("Y", Tag.TAG_INT) || !pos.contains("Z", Tag.TAG_INT)) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(pos.getInt("X"), pos.getInt("Y"), pos.getInt("Z")));
    }

    public static void write(@Nullable MirroredPatternProviderTarget target, CompoundTag root) {
        if (target == null) {
            root.remove(TAG_MIRROR);
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_DIMENSION, target.pos.dimension().location().toString());
        tag.put(TAG_POS, NbtUtils.writeBlockPos(target.pos.pos()));
        tag.putBoolean(TAG_HAS_SIDE, target.side != null);
        if (target.side != null) {
            tag.putInt(TAG_SIDE, target.side.get3DDataValue());
        }
        root.put(TAG_MIRROR, tag);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(pos.dimension().location());
        buf.writeBlockPos(pos.pos());
        buf.writeBoolean(side != null);
        if (side != null) {
            buf.writeEnum(side);
        }
    }

    public static MirroredPatternProviderTarget read(RegistryFriendlyByteBuf buf) {
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        var pos = buf.readBlockPos();
        Direction side = buf.readBoolean() ? buf.readEnum(Direction.class) : null;
        return new MirroredPatternProviderTarget(GlobalPos.of(dim, pos), side);
    }

    public @Nullable PatternProviderLogicHost resolve(Level currentLevel) {
        Level level = currentLevel;
        if (level == null || !level.dimension().equals(pos.dimension())) {
            MinecraftServer server = currentLevel == null ? null : currentLevel.getServer();
            if (server == null) {
                return null;
            }
            level = server.getLevel(pos.dimension());
        }

        if (!(level instanceof ServerLevel) || !level.hasChunkAt(pos.pos())) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(pos.pos());
        if (side == null) {
            return be instanceof PatternProviderLogicHost host ? host : null;
        }

        if (be instanceof IPartHost partHost) {
            var part = partHost.getPart(side);
            if (part instanceof PatternProviderLogicHost host) {
                return host;
            }
        }

        return null;
    }

    public @Nullable MenuHostLocator toMenuLocator(Level currentLevel) {
        PatternProviderLogicHost host = resolve(currentLevel);
        if (host == null) {
            return null;
        }

        if (host instanceof AEBasePart part) {
            return MenuLocators.forPart(part);
        }
        if (host instanceof BlockEntity be) {
            return MenuLocators.forBlockEntity(be);
        }
        return null;
    }
}
