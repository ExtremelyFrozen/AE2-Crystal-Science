package io.github.lounode.ae2cs.client.gui.widgets;

import io.github.lounode.ae2cs.api.localization.AECSTexts;
import io.github.lounode.ae2cs.client.gui.icon.AECSIcon;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;

import appeng.api.orientation.RelativeSide;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

/**
 * 仅用于 {@link io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI} 的面配置按钮
 */
public class AECSSideConfigToggleButton extends AECSBackgroundToggleButton<SidePolicy> {

    private final SideConfigMenu menu;
    private final Direction dir;
    private final @Nullable RelativeSide relativeSide;

    public AECSSideConfigToggleButton(@NotNull SideConfigMenu menu, @NotNull Direction dir, @Nullable RelativeSide relativeSide) {
        super(SidePolicy.class, SidePolicy.values()[0]);
        this.menu = menu;
        this.dir = dir;
        this.relativeSide = relativeSide;

        for (SidePolicy policy : SidePolicy.values()) {
            // 背景贴图映射（每个状态一套三态背景）
            mapBackground(policy, backgroundForPolicy(policy));

            // tooltip映射
            if (relativeSide != null) {
                mapTooltip(policy, tooltipFor(relativeSide, policy));
            } else {
                mapTooltip(policy, tooltipFor(dir, policy));
            }
        }

        // 点击后发给服务端
        setOnValueChanged(next -> menu.sendChangeSideDirPolicy(new SideConfigMenu.SideConfigChoice(dir, next)));
    }

    /**
     * 由GUI手动调用
     */
    public void syncFromMenu() {
        var field = menu.sidePolicies;
        if (field == null) return;

        EnumMap<Direction, SidePolicy> map = field.sidePolicies();
        if (map == null) return;

        SidePolicy policy = map.get(dir);
        if (policy != null) {
            this.value = policy;
        }
    }

    @Override
    protected @Nullable Item getItemOverlay() {
        BlockEntity be = menu.getBlockEntity();
        if (be == null) return null;

        Level level = be.getLevel();
        if (level == null) return null;

        BlockPos pos = be.getBlockPos().relative(dir);
        var state = level.getBlockState(pos);

        Item item = state.getBlock().asItem();
        return item == Items.AIR ? null : item;
    }

    /**
     * 根据 SidePolicy 返回一套背景贴图（normal/focus/hover）。
     */
    private static BackgroundSet backgroundForPolicy(SidePolicy policy) {
        return switch (policy) {
            case NONE -> new BackgroundSet(
                    AECSIcon.BUTTON_ORIGINAL_BIG,
                    AECSIcon.BUTTON_ORIGINAL_BIG,
                    AECSIcon.BUTTON_ORIGINAL_BIG);
            case INSERT -> new BackgroundSet(
                    AECSIcon.BUTTON_RED_BIG,
                    AECSIcon.BUTTON_RED_BIG,
                    AECSIcon.BUTTON_RED_BIG);
            case EXTRACT -> new BackgroundSet(
                    AECSIcon.BUTTON_BLUE_BIG,
                    AECSIcon.BUTTON_BLUE_BIG,
                    AECSIcon.BUTTON_BLUE_BIG);
            case ALL -> new BackgroundSet(
                    AECSIcon.BUTTON_PURPLE_BIG,
                    AECSIcon.BUTTON_PURPLE_BIG,
                    AECSIcon.BUTTON_PURPLE_BIG);
        };
    }

    private static Component tooltipFor(Direction dir, SidePolicy policy) {
        Component dirText = AECSTexts.directionName(dir);
        Component policyText = AECSTexts.sidePolicyName(policy);
        return Component.translatable("ae2cs.button.side_config.tooltip", dirText, policyText);
    }

    private static Component tooltipFor(RelativeSide side, SidePolicy policy) {
        Component sideText = AECSTexts.relativeSideName(side);
        Component policyText = AECSTexts.sidePolicyName(policy);
        return Component.translatable("ae2cs.button.side_config.tooltip", sideText, policyText);
    }
}
