package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeteoritePatternProviderBlockEntity extends PatternProviderBlockEntity implements MeteoritePatternProviderHost
{
    private LazyOptional<GenericInternalInventory> genericInvOpt = LazyOptional.empty();


    public MeteoritePatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new MeteoritePatternProviderLogic(getMainNode(), this, 63);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side)
    {
        if (cap == Capabilities.GENERIC_INTERNAL_INV)
        {
            if (!genericInvOpt.isPresent())
            {
                genericInvOpt = LazyOptional.of(() -> getLogic().getReturnInv());
            }
            return genericInvOpt.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();

        if (genericInvOpt.isPresent()) genericInvOpt.invalidate();
        genericInvOpt = LazyOptional.empty();
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
