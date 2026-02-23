package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class MeteoritePatternProviderBlockEntity extends PatternProviderBlockEntity implements MeteoritePatternProviderHost
{

    public MeteoritePatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new MeteoritePatternProviderLogic(getMainNode(), this, 63);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.METEORITE_PATTERN_PROVIDER_BLOCK_ENTITY.get(),
                (be, direction) -> be.getLogic().getReturnInv()
        );
    }

    @Override
    public void openMenu(Player player, MenuLocator locator)
    {
        MenuOpener.open(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.METEORITE_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK.get());
    }
}
