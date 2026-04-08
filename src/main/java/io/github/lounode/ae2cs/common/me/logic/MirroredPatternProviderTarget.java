package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.parts.IPartHost;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public record MirroredPatternProviderTarget(GlobalPos pos, @Nullable Direction side)
{
    private static final String TAG_MIRROR = "mirror_target";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POS = "pos";
    private static final String TAG_HAS_SIDE = "has_side";
    private static final String TAG_SIDE = "side";

    public static @Nullable MirroredPatternProviderTarget read(CompoundTag root)
    {
        if (!root.contains(TAG_MIRROR, Tag.TAG_COMPOUND))
        {
            return null;
        }

        CompoundTag tag = root.getCompound(TAG_MIRROR);
        if (!tag.contains(TAG_DIMENSION, Tag.TAG_STRING) || !tag.contains(TAG_POS, Tag.TAG_COMPOUND))
        {
            return null;
        }

        ResourceLocation dimId = ResourceLocation.tryParse(tag.getString(TAG_DIMENSION));
        if (dimId == null)
        {
            return null;
        }

        Direction side = null;
        if (tag.getBoolean(TAG_HAS_SIDE) && tag.contains(TAG_SIDE, Tag.TAG_INT))
        {
            side = Direction.from3DDataValue(tag.getInt(TAG_SIDE));
        }

        return new MirroredPatternProviderTarget(
                GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimId), NbtUtils.readBlockPos(tag.getCompound(TAG_POS))),
                side);
    }

    public static void write(@Nullable MirroredPatternProviderTarget target, CompoundTag root)
    {
        if (target == null)
        {
            root.remove(TAG_MIRROR);
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_DIMENSION, target.pos.dimension().location().toString());
        tag.put(TAG_POS, NbtUtils.writeBlockPos(target.pos.pos()));
        tag.putBoolean(TAG_HAS_SIDE, target.side != null);
        if (target.side != null)
        {
            tag.putInt(TAG_SIDE, target.side.get3DDataValue());
        }
        root.put(TAG_MIRROR, tag);
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(pos.dimension().location());
        buf.writeBlockPos(pos.pos());
        buf.writeBoolean(side != null);
        if (side != null)
        {
            buf.writeEnum(side);
        }
    }

    public static MirroredPatternProviderTarget read(FriendlyByteBuf buf)
    {
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        var pos = buf.readBlockPos();
        Direction side = buf.readBoolean() ? buf.readEnum(Direction.class) : null;
        return new MirroredPatternProviderTarget(GlobalPos.of(dim, pos), side);
    }

    public @Nullable PatternProviderLogicHost resolve(Level currentLevel)
    {
        Level level = currentLevel;
        if (level == null || !level.dimension().equals(pos.dimension()))
        {
            MinecraftServer server = currentLevel == null ? null : currentLevel.getServer();
            if (server == null)
            {
                return null;
            }
            level = server.getLevel(pos.dimension());
        }

        if (!(level instanceof ServerLevel) || !level.hasChunkAt(pos.pos()))
        {
            return null;
        }

        BlockEntity be = level.getBlockEntity(pos.pos());
        if (side == null)
        {
            return be instanceof PatternProviderLogicHost host ? host : null;
        }

        if (be instanceof IPartHost partHost)
        {
            var part = partHost.getPart(side);
            if (part instanceof PatternProviderLogicHost host)
            {
                return host;
            }
        }

        return null;
    }

    public @Nullable MenuLocator toMenuLocator(Level currentLevel)
    {
        PatternProviderLogicHost host = resolve(currentLevel);
        if (host == null)
        {
            return null;
        }

        if (host instanceof appeng.parts.AEBasePart part)
        {
            return MenuLocators.forPart(part);
        }
        if (host instanceof BlockEntity be)
        {
            return MenuLocators.forBlockEntity(be);
        }
        return null;
    }
}
