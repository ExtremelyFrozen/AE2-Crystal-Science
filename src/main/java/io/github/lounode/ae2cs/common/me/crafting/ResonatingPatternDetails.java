package io.github.lounode.ae2cs.common.me.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.google.common.base.Preconditions;
import io.github.lounode.ae2cs.api.util.PatternHelper;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern.Target;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ResonatingPatternDetails implements IPatternDetails
{

    private final AEItemKey definition;

    private final List<GenericStack> sparseInputs;
    private final List<GenericStack> sparseOutputs;
    private final List<Optional<Target>> inputTargets;

    private final Input[] inputs;
    private final List<GenericStack> condensedOutputs;

    public ResonatingPatternDetails(AEItemKey definition)
    {
        this.definition = definition;

        var encoded = definition.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null)
        {
            throw new IllegalArgumentException("Given item does not encode a resonating pattern: " + definition);
        }
        else if (encoded.containsMissingContent())
        {
            throw new IllegalArgumentException("Pattern references missing content");
        }

        this.sparseInputs = encoded.sparseInputs();
        this.sparseOutputs = encoded.sparseOutputs();
        this.inputTargets = encoded.inputTargets();

        var condensedInputs = PatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.size()];
        for (int i = 0; i < inputs.length; i++)
        {
            inputs[i] = new Input(condensedInputs.get(i));
        }

        this.condensedOutputs = PatternHelper.condenseStacks(sparseOutputs);
    }

    public static int clampSelected(int selected, int sparseSize)
    {
        if (sparseSize <= 0) return 0;
        if (selected < 0) return 0;
        if (selected >= sparseSize) return sparseSize - 1;
        return selected;
    }

    /**
     * 把样板信息编码到itemstack上
     */
    public static void encode(ItemStack stack, List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs)
    {
        if (sparseInputs.stream().noneMatch(Objects::nonNull))
        {
            throw new IllegalArgumentException("At least one input must be non-null.");
        }
        Objects.requireNonNull(sparseOutputs.getFirst(), "The first (primary) output must be non-null.");

        var targets = new ArrayList<Optional<Target>>(sparseInputs.size());
        for (int i = 0; i < sparseInputs.size(); i++)
        {
            targets.add(Optional.empty());
        }

        stack.set(AECSDataComponents.ENCODED_RESONATING_PATTERN.get(),
                new EncodedResonatingPattern(sparseInputs, sparseOutputs, targets));
        stack.set(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), 0);
    }

    @Override
    public AEItemKey getDefinition()
    {
        return definition;
    }

    @Override
    public IInput[] getInputs()
    {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs()
    {
        return condensedOutputs;
    }

    public List<GenericStack> getSparseInputs()
    {
        return sparseInputs;
    }

    public List<GenericStack> getSparseOutputs()
    {
        return sparseOutputs;
    }

    public List<Optional<Target>> getInputTargets()
    {
        return inputTargets;
    }

    public Optional<Target> getTargetForSparseInputIndex(int sparseIndex)
    {
        if (sparseIndex < 0 || sparseIndex >= inputTargets.size())
        {
            return Optional.empty();
        }
        return inputTargets.get(sparseIndex);
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink)
    {
        if (sparseInputs.size() == inputs.length)
        {
            IPatternDetails.super.pushInputsToExternalInventory(inputHolder, inputSink);
            return;
        }

        var allInputs = new KeyCounter();
        for (var counter : inputHolder)
        {
            allInputs.addAll(counter);
        }

        for (var sparseInput : sparseInputs)
        {
            if (sparseInput == null) continue;

            var key = sparseInput.what();
            var amount = sparseInput.amount();
            long available = allInputs.get(key);

            if (available < amount)
            {
                throw new RuntimeException("Expected at least %d of %s when pushing pattern, but only %d available"
                        .formatted(amount, key, available));
            }

            inputSink.pushInput(key, amount);
            allInputs.remove(key, amount);
        }
    }

    public static PatternDetailsTooltip getInvalidPatternTooltip(ItemStack stack, Level level,
                                                                 @Nullable Exception cause, TooltipFlag flags)
    {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded != null)
        {
            encoded.sparseInputs().stream().filter(Objects::nonNull).forEach(tooltip::addInput);
            encoded.sparseOutputs().stream().filter(Objects::nonNull).forEach(tooltip::addOutput);
        }

        return tooltip;
    }

    private static class Input implements IInput
    {
        private final GenericStack[] template;
        private final long multiplier;

        private Input(GenericStack stack)
        {
            this.template = new GenericStack[]{new GenericStack(stack.what(), 1)};
            this.multiplier = stack.amount();
        }

        @Override
        public GenericStack[] getPossibleInputs()
        {
            return template;
        }

        @Override
        public long getMultiplier()
        {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level)
        {
            return input.matches(template[0]);
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template)
        {
            return null;
        }
    }

    private static ListTag encodeStackList(GenericStack[] stacks, HolderLookup.Provider registries)
    {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (var stack : stacks)
        {
            tag.add(GenericStack.writeTag(registries, stack));
            if (stack != null && stack.amount() > 0)
            {
                foundStack = true;
            }
        }
        Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
        return tag;
    }
}