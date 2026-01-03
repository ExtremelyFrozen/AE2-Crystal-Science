package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ResonatingPatternProviderBlockEntity extends PatternProviderBlockEntity implements ResonatingPatternProviderHost
{
    public ResonatingPatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new ResonatingPatternProviderLogic(getMainNode(), this, 9);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.RESONATING_PATTERN_PROVIDER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.RESONATING_PATTERN_PROVIDER_BLOCK_ENTITY.get(),
                (be, direction) -> be.getLogic().getReturnInv()
        );
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator)
    {
        MenuOpener.open(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK);
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK.toStack();
    }

    @Override
    public ResonatingPatternProviderLogic getResonatingLogic()
    {
        return (ResonatingPatternProviderLogic) getLogic();
    }
}
