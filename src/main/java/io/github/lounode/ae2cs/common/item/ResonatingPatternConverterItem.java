package io.github.lounode.ae2cs.common.item;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.menuhost.ResonatingPatternConverterMenuHost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 谐振样板转换器 -> 用于打开一个批量转换样板的UI
 */
public class ResonatingPatternConverterItem extends Item implements IMenuItem
{

    public ResonatingPatternConverterItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand)
    {
        ItemStack is = player.getItemInHand(hand);

        if (!player.level().isClientSide() && checkPreconditions(is))
        {
            MenuLocator locator = MenuLocators.forHand(player, hand);

            // 如果成功打开，我们返回成功
            if (MenuOpener.open(getMenuType(), player, locator))
            {
                return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()), is);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, is);
    }

    @Override
    public @Nullable ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult)
    {
        return new ResonatingPatternConverterMenuHost(this, player, locator);
    }

    public MenuType<?> getMenuType()
    {
        return AECSMenus.RESONATING_PATTERN_CONVERTER_MENU.get();
    }

    protected boolean checkPreconditions(ItemStack item)
    {
        return !item.isEmpty() && item.getItem() == this;
    }
}
