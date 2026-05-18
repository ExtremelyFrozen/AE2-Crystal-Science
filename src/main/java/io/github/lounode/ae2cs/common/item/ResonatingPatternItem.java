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
import net.minecraft.world.InteractionResult ;
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

    /**
     * 右键方块进行标记
     */
    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // 只处理主手
        if (context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null) return InteractionResult.PASS;

        // 现在“Shift”留给滚轮切换：Shift+右键方块不做标记，直接放行
        if (InteractionUtil.isInAlternateUseMode(player))
        {
            return InteractionResult.PASS;
        }

        // 客户端动画
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        int n = encoded.sparseInputs().size();
        if (n <= 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_inputs")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }

        int validCount = countNonEmptyInputs(encoded);
        if (validCount <= 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }

        // 修正selected -> 指向一个非空输入槽（若当前指向空槽会自动跳到下一个非空并写回组件）
        int selected = resolveSelectedNonEmpty(stack, encoded);
        if (selected < 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED));
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

        player.sendOverlayMessage(
                nextTarget == null
                        ? Component.translatable("ae2cs.msg.resonating_pattern.unmarked",
                                ord, validCount, selectedInfo)
                        .withStyle(ChatFormatting.GRAY)
                        : Component.translatable("ae2cs.msg.resonating_pattern.marked",
                                ord, validCount, selectedInfo,
                                clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(),
                                face.getName())
                        .withStyle(ChatFormatting.GREEN)
        );

        // 拦截后续方块交互
        return InteractionResult.CONSUME;
    }

    /**
     * 右键空气：
     * - 仅在无编码时允许拆解
     */
    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        var encoded = stack.get(AECSDataComponents.ENCODED_RESONATING_PATTERN.get());
        if (encoded == null) // 无编码仍然允许直接拆解
        {
            return super.use(level, player, hand);
        }

        // 有编码的情况下，我们不允许拆解，防止误操作
        return InteractionResultHolder.pass(stack);
    }

    /**
     * useOn不承担任何逻辑
     */
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        return InteractionResult.PASS;
    }


    // -----------------------辅助操作方法---------------------------

    /**
     * 服务端调用：根据next(向后/向前)切换selectedInput，并给玩家发提示
     */
    public static void scrollSelectedInputAndToast(Player player, ItemStack stack,
                                                   EncodedResonatingPattern encoded, boolean next)
    {
        int n = encoded.sparseInputs().size();
        if (n <= 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_inputs")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        int validCount = countNonEmptyInputs(encoded);
        if (validCount <= 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        int cur = resolveSelectedNonEmpty(stack, encoded);
        if (cur < 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        int nextIdx = next ? findNextNonEmptyInput(encoded, cur) : findPrevNonEmptyInput(encoded, cur);
        if (nextIdx < 0)
        {
            player.sendOverlayMessage(Component.translatable("ae2cs.msg.resonating_pattern.no_valid_inputs")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        stack.set(AECSDataComponents.RESONATING_PATTERN_SELECTED_INPUT.get(), nextIdx);

        int ord = ordinalOfNonEmptyInput(encoded, nextIdx);
        var selectedInfo = buildSelectedInputInfo(encoded, nextIdx);
        var markInfo = buildSelectedInputMarkInfo(encoded, nextIdx);

        if (markInfo.isPresent())
        {
            var t = markInfo.get();
            var dimId = Component.translatable(t.pos().dimension().location().toLanguageKey("dimension"));
            var pos = t.pos().pos();
            player.sendOverlayMessage(
                    Component.translatable("ae2cs.msg.resonating_pattern.selected_input_marked",
                                    ord, validCount, selectedInfo,
                                    dimId,
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    t.face().getName())
                            .withStyle(ChatFormatting.GRAY)
            );
        }
        else
        {
            player.sendOverlayMessage(
                    Component.translatable("ae2cs.msg.resonating_pattern.selected_input_unmarked",
                                    ord, validCount, selectedInfo)
                            .withStyle(ChatFormatting.GRAY)
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
     * 从start往前找上一个非空输入（循环一圈），找不到返回 -1
     */
    private static int findPrevNonEmptyInput(EncodedResonatingPattern encoded, int start)
    {
        int n = encoded.sparseInputs().size();
        if (n <= 0) return -1;

        for (int step = 1; step <= n; step++)
        {
            int idx = (start - step) % n;
            if (idx < 0) idx += n;
            if (isNonEmptySparseInput(encoded, idx)) return idx;
        }
        return -1;
    }

    /**
     * 把sparseIndex映射为“第几个非空输入”。若该sparseIndex本身为空，返回 0。
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