package io.github.lounode.ae2cs.common.me.crafting;

import io.github.lounode.ae2cs.common.init.AECSDataComponents;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ResonatingProviderDefaults {

    public static final int DEFAULT_INPUT_SLOTS = 81;

    private ResonatingProviderDefaults() {}

    public static List<Optional<EncodedResonatingPattern.Target>> readTargets(ItemStack stack) {
        return stack.getOrDefault(AECSDataComponents.RESONATING_PROVIDER_DEFAULTS.get(), Defaults.EMPTY).targets();
    }

    public static void writeTargets(ItemStack stack, List<Optional<EncodedResonatingPattern.Target>> targets) {
        var current = stack.getOrDefault(AECSDataComponents.RESONATING_PROVIDER_DEFAULTS.get(), Defaults.EMPTY);
        stack.set(AECSDataComponents.RESONATING_PROVIDER_DEFAULTS.get(), new Defaults(current.selectedInput(), targets));
    }

    public static int getSelectedInput(ItemStack stack) {
        return stack.getOrDefault(AECSDataComponents.RESONATING_PROVIDER_DEFAULTS.get(), Defaults.EMPTY).selectedInput();
    }

    public static void setSelectedInput(ItemStack stack, int selected) {
        var current = stack.getOrDefault(AECSDataComponents.RESONATING_PROVIDER_DEFAULTS.get(), Defaults.EMPTY);
        stack.set(AECSDataComponents.RESONATING_PROVIDER_DEFAULTS.get(), new Defaults(selected, current.targets()));
    }

    public static int clampSelected(int selected) {
        if (selected < 0) return 0;
        if (selected >= DEFAULT_INPUT_SLOTS) return DEFAULT_INPUT_SLOTS - 1;
        return selected;
    }

    public static boolean hasAnyTarget(List<Optional<EncodedResonatingPattern.Target>> targets) {
        return targets.stream().anyMatch(Optional::isPresent);
    }

    public static void writeTargets(RegistryFriendlyByteBuf buf, List<Optional<EncodedResonatingPattern.Target>> targets) {
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

    public static List<Optional<EncodedResonatingPattern.Target>> readTargets(RegistryFriendlyByteBuf buf) {
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

    public record Defaults(int selectedInput, List<Optional<EncodedResonatingPattern.Target>> targets) {

        public static final Defaults EMPTY = new Defaults(0, List.of());

        public Defaults {
            selectedInput = clampSelected(selectedInput);
            targets = normalizeTargets(targets);
        }

        public static final Codec<Defaults> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("selected_input").forGetter(Defaults::selectedInput),
                EncodedResonatingPattern.OPTIONAL_TARGET_CODEC.listOf().fieldOf("targets").forGetter(Defaults::targets)).apply(instance, Defaults::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Defaults> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                Defaults::selectedInput,
                EncodedResonatingPattern.OPTIONAL_TARGET_STREAM_CODEC.apply(ByteBufCodecs.list(DEFAULT_INPUT_SLOTS)),
                Defaults::targets,
                Defaults::new);

        private static List<Optional<EncodedResonatingPattern.Target>> normalizeTargets(
                                                                                        List<Optional<EncodedResonatingPattern.Target>> input) {
            var out = new ArrayList<Optional<EncodedResonatingPattern.Target>>(DEFAULT_INPUT_SLOTS);
            for (int i = 0; i < DEFAULT_INPUT_SLOTS; i++) {
                out.add(i < input.size() ? input.get(i) : Optional.empty());
            }
            return List.copyOf(out);
        }
    }
}
