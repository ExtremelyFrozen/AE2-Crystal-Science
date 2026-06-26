package io.github.lounode.ae2cs.common.me.crafting;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ResonatingProviderDefaults {

    public static final int DEFAULT_INPUT_SLOTS = 81;

    private static final String TAG_DEFAULT_INPUT_TARGETS = "provider_default_input_targets";
    private static final String TAG_SELECTED_INPUT = "provider_selected_input";
    private static final String TAG_TARGET_HAS_VALUE = "hasValue";
    private static final String TAG_TARGET_FACE = "face";
    private static final String TAG_TARGET_DIMENSION = "dimension";
    private static final String TAG_TARGET_POS = "pos";

    private ResonatingProviderDefaults() {}

    public static List<Optional<EncodedResonatingPattern.Target>> readTargets(ItemStack stack) {
        return readTargets(stack.getTag());
    }

    public static List<Optional<EncodedResonatingPattern.Target>> readTargets(@Nullable CompoundTag tag) {
        var targets = emptyTargets();
        if (tag == null || !tag.contains(TAG_DEFAULT_INPUT_TARGETS, Tag.TAG_LIST)) {
            return targets;
        }

        ListTag targetList = tag.getList(TAG_DEFAULT_INPUT_TARGETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(DEFAULT_INPUT_SLOTS, targetList.size()); i++) {
            targets.set(i, deserializeTarget(targetList.getCompound(i)));
        }
        return targets;
    }

    public static void writeTargets(ItemStack stack, List<Optional<EncodedResonatingPattern.Target>> targets) {
        stack.getOrCreateTag().put(TAG_DEFAULT_INPUT_TARGETS, writeTargetsTag(targets));
    }

    public static void writeTargets(CompoundTag tag, List<Optional<EncodedResonatingPattern.Target>> targets) {
        tag.put(TAG_DEFAULT_INPUT_TARGETS, writeTargetsTag(targets));
    }

    public static int getSelectedInput(ItemStack stack) {
        return getSelectedInput(stack.getTag());
    }

    public static int getSelectedInput(@Nullable CompoundTag tag) {
        if (tag == null) {
            return 0;
        }
        return clampSelected(tag.getInt(TAG_SELECTED_INPUT));
    }

    public static void setSelectedInput(ItemStack stack, int selected) {
        stack.getOrCreateTag().putInt(TAG_SELECTED_INPUT, clampSelected(selected));
    }

    public static void setSelectedInput(CompoundTag tag, int selected) {
        tag.putInt(TAG_SELECTED_INPUT, clampSelected(selected));
    }

    public static int clampSelected(int selected) {
        if (selected < 0) return 0;
        if (selected >= DEFAULT_INPUT_SLOTS) return DEFAULT_INPUT_SLOTS - 1;
        return selected;
    }

    public static boolean hasAnyTarget(List<Optional<EncodedResonatingPattern.Target>> targets) {
        return targets.stream().anyMatch(Optional::isPresent);
    }

    public static void writeTargets(FriendlyByteBuf buf, List<Optional<EncodedResonatingPattern.Target>> targets) {
        buf.writeVarInt(DEFAULT_INPUT_SLOTS);
        for (int i = 0; i < DEFAULT_INPUT_SLOTS; i++) {
            var target = i < targets.size() ? targets.get(i).orElse(null) : null;
            buf.writeBoolean(target != null);
            if (target != null) {
                buf.writeResourceLocation(target.pos().dimension().location());
                buf.writeBlockPos(target.pos().pos());
                buf.writeEnum(target.face());
            }
        }
    }

    public static List<Optional<EncodedResonatingPattern.Target>> readTargets(FriendlyByteBuf buf) {
        int size = Math.max(DEFAULT_INPUT_SLOTS, buf.readVarInt());
        var targets = new ArrayList<Optional<EncodedResonatingPattern.Target>>(size);
        for (int i = 0; i < size; i++) {
            if (!buf.readBoolean()) {
                targets.add(Optional.empty());
                continue;
            }

            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
            targets.add(Optional.of(new EncodedResonatingPattern.Target(
                    GlobalPos.of(dimension, buf.readBlockPos()), buf.readEnum(Direction.class))));
        }

        if (targets.size() > DEFAULT_INPUT_SLOTS) {
            return new ArrayList<>(targets.subList(0, DEFAULT_INPUT_SLOTS));
        }

        while (targets.size() < DEFAULT_INPUT_SLOTS) {
            targets.add(Optional.empty());
        }
        return targets;
    }

    public static List<Optional<EncodedResonatingPattern.Target>> emptyTargets() {
        var out = new ArrayList<Optional<EncodedResonatingPattern.Target>>(DEFAULT_INPUT_SLOTS);
        for (int i = 0; i < DEFAULT_INPUT_SLOTS; i++) {
            out.add(Optional.empty());
        }
        return out;
    }

    private static ListTag writeTargetsTag(List<Optional<EncodedResonatingPattern.Target>> targets) {
        ListTag list = new ListTag();
        for (int i = 0; i < DEFAULT_INPUT_SLOTS; i++) {
            var target = i < targets.size() ? targets.get(i).orElse(null) : null;
            list.add(serializeTarget(target));
        }
        return list;
    }

    private static CompoundTag serializeTarget(@Nullable EncodedResonatingPattern.Target target) {
        CompoundTag tag = new CompoundTag();
        if (target == null) {
            tag.putBoolean(TAG_TARGET_HAS_VALUE, false);
            return tag;
        }

        tag.putBoolean(TAG_TARGET_HAS_VALUE, true);
        tag.putInt(TAG_TARGET_FACE, target.face().get3DDataValue());
        tag.putString(TAG_TARGET_DIMENSION, target.pos().dimension().location().toString());
        tag.put(TAG_TARGET_POS, NbtUtils.writeBlockPos(target.pos().pos()));
        return tag;
    }

    private static Optional<EncodedResonatingPattern.Target> deserializeTarget(CompoundTag tag) {
        if (!tag.getBoolean(TAG_TARGET_HAS_VALUE)) {
            return Optional.empty();
        }
        if (!tag.contains(TAG_TARGET_FACE, Tag.TAG_INT) || !tag.contains(TAG_TARGET_DIMENSION, Tag.TAG_STRING) || !tag.contains(TAG_TARGET_POS, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        Direction face = Direction.from3DDataValue(tag.getInt(TAG_TARGET_FACE));
        ResourceLocation dimId = ResourceLocation.tryParse(tag.getString(TAG_TARGET_DIMENSION));
        if (dimId == null) {
            return Optional.empty();
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimId);
        return Optional.of(new EncodedResonatingPattern.Target(
                GlobalPos.of(dimension, NbtUtils.readBlockPos(tag.getCompound(TAG_TARGET_POS))), face));
    }
}
