package io.github.lounode.ae2cs.common.me.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEProcessingPattern;
import com.google.common.base.Preconditions;
import io.github.lounode.ae2cs.api.util.PatternHelper;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern.Target;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ResonatingPatternDetails implements IPatternDetails
{

    private final AEItemKey definition;

    private final List<GenericStack> sparseInputs;
    private final List<GenericStack> sparseOutputs;
    private final List<Optional<Target>> inputTargets;

    private final Input[] inputs;
    private final List<GenericStack> condensedOutputs;
    private final GenericStack[] outputArray;

    public ResonatingPatternDetails(AEItemKey definition)
    {
        this.definition = definition;

        EncodedResonatingPattern encoded = AECSDataComponents.getEncodedResonatingPattern(definition.toStack());
        if (encoded == null)
        {
            throw new IllegalArgumentException("Given item does not encode a resonating pattern: " + definition);
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
        this.outputArray = this.condensedOutputs.toArray(new GenericStack[0]);
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
        Objects.requireNonNull(sparseOutputs.get(0), "The first (primary) output must be non-null.");

        var targets = new ArrayList<Optional<Target>>(sparseInputs.size());
        for (int i = 0; i < sparseInputs.size(); i++)
        {
            targets.add(Optional.empty());
        }

        AECSDataComponents.setEncodedResonatingPattern(stack,
                new EncodedResonatingPattern(sparseInputs, sparseOutputs, targets));
        AECSDataComponents.setResonatingPatternSelectedInput(stack, 0);
    }

    /**
     * 把信息从处理样板编码到谐振样板上
     */
    public static boolean encode(@NotNull ItemStack patternItem, @NotNull ItemStack resonatingPattern)
    {
        if (patternItem.isEmpty() || resonatingPattern.isEmpty()) return false;

        if (!patternItem.is(AEItems.PROCESSING_PATTERN.asItem())
                || !resonatingPattern.is(AECSItems.RESONATING_PATTERN.get())
                || patternItem.getTag() == null
        )
            return false;

        AEProcessingPattern src = PatternHelper.getAEProcessingPattern(patternItem);
        if (src == null) return false;

        List<Optional<EncodedResonatingPattern.Target>> targets = new ArrayList<>(src.getSparseInputs().length);
        for (int i = 0; i < src.getSparseInputs().length; i++)
        {
            targets.add(Optional.empty());
        }

        AECSDataComponents.setEncodedResonatingPattern(resonatingPattern,
                new EncodedResonatingPattern(List.copyOf(Arrays.asList(src.getSparseInputs())), List.copyOf(Arrays.asList(src.getSparseOutputs())), targets));
        AECSDataComponents.setResonatingPatternSelectedInput(resonatingPattern, 0);
        return true;
    }

    /**
     * 从处理样板获取一个对应的谐振样板
     */
    public static ItemStack encode(@NotNull ItemStack patternItem)
    {
        if (patternItem.isEmpty()) return ItemStack.EMPTY;

        if (!patternItem.is(AEItems.PROCESSING_PATTERN.asItem()))
            return ItemStack.EMPTY;

        ItemStack resonatingItem = AECSItems.RESONATING_PATTERN.get().getDefaultInstance();

        if (encode(patternItem, resonatingItem))
        {
            return resonatingItem;
        }
        return ItemStack.EMPTY;
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
    public GenericStack[] getOutputs()
    {
        return this.outputArray;
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

    private static ListTag encodeStackList(GenericStack[] stacks)
    {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (var stack : stacks)
        {
            tag.add(GenericStack.writeTag(stack));
            if (stack != null && stack.amount() > 0)
            {
                foundStack = true;
            }
        }
        Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
        return tag;
    }
}
