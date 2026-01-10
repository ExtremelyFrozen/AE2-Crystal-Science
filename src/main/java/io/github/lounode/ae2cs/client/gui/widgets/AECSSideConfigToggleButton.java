package io.github.lounode.ae2cs.client.gui.widgets;

import appeng.api.orientation.RelativeSide;
import io.github.lounode.ae2cs.common.machine.component.SidePolicy;
import io.github.lounode.ae2cs.common.menu.submenu.SideConfigMenu;
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
import java.util.Locale;

/**
 * 仅用于 {@link io.github.lounode.ae2cs.client.gui.subGUI.SideConfigGUI} 的面配置按钮
 */
public class AECSSideConfigToggleButton extends AECSColorToggleButton<SidePolicy>
{
    private final SideConfigMenu menu;
    private final Direction dir;
    private final @Nullable RelativeSide relativeSide;

    public AECSSideConfigToggleButton(@NotNull SideConfigMenu menu, @NotNull Direction dir, @Nullable RelativeSide relativeSide)
    {
        super(SidePolicy.class, SidePolicy.values()[0]);
        this.menu = menu;
        this.dir = dir;
        this.relativeSide = relativeSide;

        for (SidePolicy policy : SidePolicy.values())
        {
            mapBackgroundTint(policy, tintForPolicy(policy));
            if (relativeSide != null)
            {
                mapTooltip(policy, tooltipFor(relativeSide, policy));
            }
            else
            {
                mapTooltip(policy, tooltipFor(dir, policy));
            }
        }

        // 点击后发给服务端
        setOnValueChanged(next ->
                menu.sendChangeSideDirPolicy(new SideConfigMenu.SideConfigChoice(dir, next))
        );
    }

    /**
     * 由GUI手动调用
     */
    public void syncFromMenu()
    {
        var field = menu.sidePolicies;
        if (field == null) return;

        EnumMap<Direction, SidePolicy> map = field.sidePolicies();
        if (map == null) return;

        SidePolicy policy = map.get(dir);
        if (policy != null)
        {
            this.value = policy;
        }
    }

    @Override
    public void renderWidget(@NotNull net.minecraft.client.gui.GuiGraphics gg, int mouseX, int mouseY, float partial)
    {
        super.renderWidget(gg, mouseX, mouseY, partial);
    }

    @Override
    protected @Nullable Item getItemOverlay()
    {
        BlockEntity be = menu.getBlockEntity();
        if (be == null) return null;

        Level level = be.getLevel();
        if (level == null) return null;

        BlockPos pos = be.getBlockPos().relative(dir);
        var state = level.getBlockState(pos);

        Item item = state.getBlock().asItem();
        return item == Items.AIR ? null : item;
    }

    private static int tintForPolicy(SidePolicy policy)
    {
        return switch (policy)
        {
            case NONE -> 0x00000000;   // 不覆盖色
            case INSERT -> 0xA0D07070;
            case EXTRACT -> 0xA070A0E0;
            case ALL -> 0xA0A080D0;
        };
    }

    private static Component tooltipFor(Direction dir, SidePolicy policy)
    {
        String dirKey = dir.getName().toLowerCase(Locale.ROOT);
        String policyKey = policy.name().toLowerCase(Locale.ROOT);
        return Component.translatable("ae2cs.side_config.tooltip", dirKey, policyKey);
    }

    private static Component tooltipFor(RelativeSide side, SidePolicy policy)
    {
        String dirKey = side.name().toLowerCase(Locale.ROOT);
        String policyKey = policy.name().toLowerCase(Locale.ROOT);
        return Component.translatable("ae2cs.side_config.tooltip", dirKey, policyKey);
    }
}