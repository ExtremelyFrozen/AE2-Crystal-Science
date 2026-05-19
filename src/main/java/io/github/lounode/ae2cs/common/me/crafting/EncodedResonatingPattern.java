package io.github.lounode.ae2cs.common.me.crafting;

import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * target的size必须与inputs相等以方便进行一一对应，但是target的optional可为空以代表无指定目标
 */
public record EncodedResonatingPattern(
        List<GenericStack> sparseInputs,
        List<GenericStack> sparseOutputs,
        List<Optional<Target>> inputTargets)
{

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

    public static final Codec<Target> TARGET_CODEC = RecordCodecBuilder.create(b -> b.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(Target::pos),
            Direction.CODEC.fieldOf("face").forGetter(Target::face)
    ).apply(b, Target::new));

    public static final Codec<Optional<Target>> OPTIONAL_TARGET_CODEC =
            Codec.either(TARGET_CODEC, MapCodec.unitCodec(Unit.INSTANCE))
                    .xmap(
                            e -> e.left().map(Optional::of).orElse(Optional.empty()),
                            opt -> opt.<Either<Target, Unit>>map(
                                    Either::left
                            ).orElseGet(() -> Either.right(Unit.INSTANCE))
                    );

    public static final Codec<EncodedResonatingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC.fieldOf("sparseInputs")
                    .forGetter(EncodedResonatingPattern::sparseInputs),
            GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC.fieldOf("sparseOutputs")
                    .forGetter(EncodedResonatingPattern::sparseOutputs),
            OPTIONAL_TARGET_CODEC.listOf().fieldOf("inputTargets")
                    .forGetter(EncodedResonatingPattern::inputTargets)
    ).apply(builder, EncodedResonatingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Target> TARGET_STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC,
            Target::pos,
            ByteBufCodecs.idMapper(Direction.BY_ID, Direction::get3DDataValue),
            Target::face,
            Target::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Target>> OPTIONAL_TARGET_STREAM_CODEC =
            ByteBufCodecs.optional(TARGET_STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedResonatingPattern> STREAM_CODEC = StreamCodec
            .composite(
                    GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    EncodedResonatingPattern::sparseInputs,
                    GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    EncodedResonatingPattern::sparseOutputs,
                    OPTIONAL_TARGET_STREAM_CODEC.apply(ByteBufCodecs.list()),
                    EncodedResonatingPattern::inputTargets,
                    EncodedResonatingPattern::new
            );

    public boolean containsMissingContent()
    {
        return Stream.concat(sparseInputs.stream(), sparseOutputs.stream())
                .anyMatch(stack -> stack != null && AEItems.MISSING_CONTENT.is(stack.what()));
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
