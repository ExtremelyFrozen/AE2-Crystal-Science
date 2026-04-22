package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 一个只有5个槽位的样板供应器
 */
public class SimplePatternProviderBlockEntity extends PatternProviderBlockEntity
{

    public SimplePatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new PatternProviderLogic(getMainNode(), this, 5);
    }

    @Override
    public void openMenu(Player player, MenuLocator locator)
    {
        MenuOpener.open(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSBlocks.SIMPLE_PATTERN_PROVIDER_BLOCK.get());
    }
}
