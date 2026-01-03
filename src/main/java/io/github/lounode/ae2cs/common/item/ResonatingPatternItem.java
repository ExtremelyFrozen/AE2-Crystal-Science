package io.github.lounode.ae2cs.common.item;

import appeng.crafting.pattern.EncodedPatternItem;
import appeng.util.InteractionUtil;
import io.github.lounode.ae2cs.common.init.AECSDataComponents;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern.Target;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ResonatingPatternItem extends EncodedPatternItem<ResonatingPatternDetails>
{
    public ResonatingPatternItem(Properties properties)
    {
        super(properties.stacksTo(1),
                (what, level) -> new ResonatingPatternDetails(what),
                ResonatingPatternDetails::getInvalidPatternTooltip);
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        // 取消掉AE2原版在此的拆解逻辑，使交互管线向后继续运行
        return InteractionResult.PASS;
    }

    /**
     * 右键空气：
     * - 非Shift：切换selectedInput
     * - Shift：交给AE原版触发拆解逻辑
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null)
        {
            return super.use(level, player, hand);
        }

        // Shift 交给AE原版拆解
        if (InteractionUtil.isInAlternateUseMode(player))
        {
            return super.use(level, player, hand);
        }

        // 非Shift 切换selectedInput
        if (!level.isClientSide())
        {
            cycleSelectedInputAndToast(player, stack, encoded);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 右键方块：
     * - 非Shift：切换selectedInput
     * - Shift：标记/取消标记
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context)
    {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        var stack = context.getItemInHand();

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null) return InteractionResult.PASS;

        // 非Shift：只做切换
        if (!InteractionUtil.isInAlternateUseMode(player))
        {
            if (!level.isClientSide())
            {
                cycleSelectedInputAndToast(player, stack, encoded);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Shift+右键方块：标记/取消标记
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        int n = encoded.sparseInputs().size();
        if (n <= 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        int validCount = countNonEmptyInputs(encoded);
        if (validCount <= 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        // 修正selected -> 指向一个非空输入槽（若当前指向空槽会自动跳到下一个非空并写回组件）
        int selected = resolveSelectedNonEmpty(stack, encoded);
        if (selected < 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        int ord = ordinalOfNonEmptyInput(encoded, selected);
        var selectedInfo = buildSelectedInputInfo(encoded, selected);

        var clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        var target = new Target(GlobalPos.of(level.dimension(), clickedPos), face);

        Optional<Target> current = encoded.targetOfSparseInput(selected);

        Target nextTarget;
        if (current.isPresent()
                && current.get().pos().equals(target.pos())
                && current.get().face() == target.face())
        {
            nextTarget = null; // 同位置同面 -> 取消标记
        }
        else
        {
            nextTarget = target; // 覆盖/新增
        }

        var updated = encoded.withTarget(selected, nextTarget);
        stack.set(AECSDataComponents.ENCODED_RESONATING_PATTERN.get(), updated);

        player.displayClientMessage(
                nextTarget == null
                        ? Component.translatable("ae2cs.msg.resonating_pattern.unmarked",
                                ord, validCount, selectedInfo)
                        .withStyle(ChatFormatting.GRAY)
                        : Component.translatable("ae2cs.msg.resonating_pattern.marked",
                                ord, validCount, selectedInfo,
                                clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                                face.getName())
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        return InteractionResult.CONSUME;
    }

    private static void cycleSelectedInputAndToast(Player player, ItemStack stack,
                                                   EncodedResonatingPattern encoded)
    {
        int n = encoded.sparseInputs().size();
        if (n <= 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        int validCount = countNonEmptyInputs(encoded);
        if (validCount <= 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        // 先把当前 selected 修正到一个非空输入上（防旧数据/异常）
        int cur = resolveSelectedNonEmpty(stack, encoded);
        if (cur < 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        int next = findNextNonEmptyInput(encoded, cur);
        if (next < 0)
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        stack.set(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), next);

        int ord = ordinalOfNonEmptyInput(encoded, next);
        var selectedInfo = buildSelectedInputInfo(encoded, next);
        var markInfo = buildSelectedInputMarkInfo(encoded, next);

        if (markInfo.isPresent())
        {
            var t = markInfo.get();
            var dimId = t.pos().dimension().location().toString();
            var pos = t.pos().pos();
            player.displayClientMessage(
                    Component.translatable("ae2cs.msg.resonating_pattern.selected_input_marked",
                                    ord, validCount, selectedInfo,
                                    dimId,
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    t.face().getName())
                            .withStyle(ChatFormatting.GRAY),
                    true
            );
        }
        else
        {
            player.displayClientMessage(
                    Component.translatable("ae2cs.msg.resonating_pattern.selected_input_unmarked",
                                    ord, validCount, selectedInfo)
                            .withStyle(ChatFormatting.GRAY),
                    true
            );
        }
    }

    private static boolean isNonEmptySparseInput(EncodedResonatingPattern encoded, int sparseIndex)
    {
        if (sparseIndex < 0 || sparseIndex >= encoded.sparseInputs().size()) return false;
        var gs = encoded.sparseInputs().get(sparseIndex);
        return gs != null && gs.amount() > 0;
    }

    private static int countNonEmptyInputs(EncodedResonatingPattern encoded)
    {
        int count = 0;
        for (int i = 0; i < encoded.sparseInputs().size(); i++)
        {
            if (isNonEmptySparseInput(encoded, i)) count++;
        }
        return count;
    }

    /**
     * 从start（当前index）往后找下一个非空输入（循环一圈）
     * 找不到返回 -1
     */
    private static int findNextNonEmptyInput(EncodedResonatingPattern encoded, int start)
    {
        int n = encoded.sparseInputs().size();
        if (n <= 0) return -1;

        for (int step = 1; step <= n; step++)
        {
            int idx = (start + step) % n;
            if (isNonEmptySparseInput(encoded, idx)) return idx;
        }
        return -1;
    }

    /**
     * 把sparseIndex映射为“第几个非空输入”。
     * 若该sparseIndex本身为空，返回 0。
     */
    private static int ordinalOfNonEmptyInput(EncodedResonatingPattern encoded, int sparseIndex)
    {
        if (!isNonEmptySparseInput(encoded, sparseIndex)) return 0;

        int ord = 0;
        for (int i = 0; i <= sparseIndex && i < encoded.sparseInputs().size(); i++)
        {
            if (isNonEmptySparseInput(encoded, i)) ord++;
        }
        return ord;
    }

    /**
     * 保证当前selected指向一个非空输入；如果当前为空则自动跳到下一个非空输入并写回组件。
     * 没有任何非空输入则返回 -1。
     */
    private static int resolveSelectedNonEmpty(ItemStack stack, EncodedResonatingPattern encoded)
    {
        int n = encoded.sparseInputs().size();
        if (n <= 0) return -1;

        int sel = stack.getOrDefault(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), 0);
        sel = ResonatingPatternDetails.clampSelected(sel, n);

        if (isNonEmptySparseInput(encoded, sel))
        {
            return sel;
        }

        int next = findNextNonEmptyInput(encoded, sel - 1);
        if (next >= 0)
        {
            stack.set(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), next);
        }
        return next;
    }


    private static Optional<Target> buildSelectedInputMarkInfo(EncodedResonatingPattern encoded, int sparseIndex)
    {
        if (sparseIndex < 0 || sparseIndex >= encoded.inputTargets().size())
        {
            return Optional.empty();
        }
        return encoded.inputTargets().get(sparseIndex);
    }


    /**
     * 构造一个“当前选中输入”的显示信息（尽量显示物品名；如果为空则显示“空槽”）
     */
    private static Component buildSelectedInputInfo(EncodedResonatingPattern encoded, int sparseIndex)
    {
        if (sparseIndex < 0 || sparseIndex >= encoded.sparseInputs().size())
        {
            return Component.translatable("ae2cs.msg.resonating_pattern.input_invalid");
        }

        var gs = encoded.sparseInputs().get(sparseIndex);
        if (gs == null || gs.amount() <= 0)
        {
            return Component.translatable("ae2cs.msg.resonating_pattern.input_empty");
        }

        return gs.what().getDisplayName();
    }
}