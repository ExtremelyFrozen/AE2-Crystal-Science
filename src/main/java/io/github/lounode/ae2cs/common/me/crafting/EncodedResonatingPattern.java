package io.github.lounode.ae2cs.common.me.crafting;

import appeng.api.stacks.GenericStack;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * target的size必须与inputs相等以方便进行一一对应，但是target的optional可为空以代表无指定目标
 */
public record EncodedResonatingPattern(
        List<GenericStack> sparseInputs,
        List<GenericStack> sparseOutputs,
        List<Optional<Target>> inputTargets)
{
    private static final String TAG_SPARSE_INPUTS = "sparseInputs";
    private static final String TAG_SPARSE_OUTPUTS = "sparseOutputs";
    private static final String TAG_INPUT_TARGETS = "inputTargets";

    private static final String TAG_TARGET_HAS_VALUE = "hasValue";
    private static final String TAG_TARGET_FACE = "face";
    private static final String TAG_TARGET_DIMENSION = "dimension";
    private static final String TAG_TARGET_POS = "pos";


    public EncodedResonatingPattern
    {
        sparseInputs = Collections.unmodifiableList(Objects.requireNonNull(sparseInputs, "sparseInputs"));
        sparseOutputs = Collections.unmodifiableList(Objects.requireNonNull(sparseOutputs, "sparseOutputs"));
        inputTargets = Collections.unmodifiableList(Objects.requireNonNull(inputTargets, "inputTargets"));

        if (inputTargets.size() != sparseInputs.size())
        {
            throw new IllegalArgumentException("inputTargets must have same size as sparseInputs. targets=%d inputs=%d"
                    .formatted(inputTargets.size(), sparseInputs.size()));
        }
    }

    public record Target(GlobalPos pos, Direction face)
    {
    }

    public static CompoundTag writeToNBT(EncodedResonatingPattern pattern)
    {
        CompoundTag tag = new CompoundTag();

        ListTag inputList = new ListTag();
        for (GenericStack stack : pattern.sparseInputs())
        {
            inputList.add(GenericStack.writeTag(stack));
        }
        tag.put(TAG_SPARSE_INPUTS, inputList);

        ListTag outputList = new ListTag();
        for (GenericStack stack : pattern.sparseOutputs())
        {
            outputList.add(GenericStack.writeTag(stack));
        }
        tag.put(TAG_SPARSE_OUTPUTS, outputList);

        ListTag targetList = new ListTag();
        for (Optional<Target> target : pattern.inputTargets())
        {
            targetList.add(serializeTarget(target.orElse(null)));
        }
        tag.put(TAG_INPUT_TARGETS, targetList);

        return tag;
    }

    public static @Nullable EncodedResonatingPattern readFromNBT(@Nullable CompoundTag tag)
    {
        if (tag == null)
        {
            return null;
        }

        List<GenericStack> sparseInputs = new ArrayList<>();
        ListTag inputList = tag.getList(TAG_SPARSE_INPUTS, 10);
        for (int i = 0; i < inputList.size(); i++)
        {
            sparseInputs.add(GenericStack.readTag(inputList.getCompound(i)));
        }

        List<GenericStack> sparseOutputs = new ArrayList<>();
        ListTag outputList = tag.getList(TAG_SPARSE_OUTPUTS, 10);
        for (int i = 0; i < outputList.size(); i++)
        {
            sparseOutputs.add(GenericStack.readTag(outputList.getCompound(i)));
        }

        List<Optional<Target>> inputTargets = new ArrayList<>();
        ListTag targetList = tag.getList(TAG_INPUT_TARGETS, 10);
        for (int i = 0; i < targetList.size(); i++)
        {
            inputTargets.add(deserializeTarget(targetList.getCompound(i)));
        }

        if (inputTargets.size() < sparseInputs.size())
        {
            while (inputTargets.size() < sparseInputs.size())
            {
                inputTargets.add(Optional.empty());
            }
        }
        else if (inputTargets.size() > sparseInputs.size())
        {
            inputTargets = new ArrayList<>(inputTargets.subList(0, sparseInputs.size()));
        }

        return new EncodedResonatingPattern(sparseInputs, sparseOutputs, inputTargets);
    }

    public static void writeToNetwork(FriendlyByteBuf buf, EncodedResonatingPattern pattern)
    {
        buf.writeNbt(writeToNBT(pattern));
    }

    public static @Nullable EncodedResonatingPattern readFromNetwork(FriendlyByteBuf buf)
    {
        return readFromNBT(buf.readNbt());
    }

    private static CompoundTag serializeTarget(@Nullable Target target)
    {
        CompoundTag tag = new CompoundTag();
        if (target == null)
        {
            tag.putBoolean(TAG_TARGET_HAS_VALUE, false);
            return tag;
        }

        tag.putBoolean(TAG_TARGET_HAS_VALUE, true);
        tag.putInt(TAG_TARGET_FACE, target.face().get3DDataValue());
        tag.putString(TAG_TARGET_DIMENSION, target.pos().dimension().location().toString());
        tag.put(TAG_TARGET_POS, NbtUtils.writeBlockPos(target.pos().pos()));
        return tag;
    }

    private static Optional<Target> deserializeTarget(CompoundTag tag)
    {
        if (!tag.getBoolean(TAG_TARGET_HAS_VALUE))
        {
            return Optional.empty();
        }
        if (!tag.contains(TAG_TARGET_FACE, 3)
                || !tag.contains(TAG_TARGET_DIMENSION, 8)
                || !tag.contains(TAG_TARGET_POS, 10))
        {
            return Optional.empty();
        }

        Direction face = Direction.from3DDataValue(tag.getInt(TAG_TARGET_FACE));
        ResourceLocation dimId = ResourceLocation.tryParse(tag.getString(TAG_TARGET_DIMENSION));
        if (dimId == null)
        {
            return Optional.empty();
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimId);
        return Optional.of(new Target(GlobalPos.of(dimension, NbtUtils.readBlockPos(tag.getCompound(TAG_TARGET_POS))), face));
    }

    public Optional<Target> targetOfSparseInput(int sparseIndex)
    {
        if (sparseIndex < 0 || sparseIndex >= inputTargets.size())
        {
            return Optional.empty();
        }
        return inputTargets.get(sparseIndex);
    }

    public EncodedResonatingPattern withTarget(int sparseIndex, @Nullable Target newTarget)
    {
        if (sparseIndex < 0 || sparseIndex >= inputTargets.size())
        {
            return this;
        }
        var copy = new ArrayList<>(inputTargets);
        copy.set(sparseIndex, newTarget == null ? Optional.empty() : Optional.of(newTarget));
        return new EncodedResonatingPattern(sparseInputs, sparseOutputs, copy);
    }
}
