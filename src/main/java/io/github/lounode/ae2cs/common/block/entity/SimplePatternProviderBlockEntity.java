package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.SettingsFrom;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.MirroredSimplePatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredSimplePatternProviderLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 一个只有5个槽位的样板供应器
 */
public class SimplePatternProviderBlockEntity extends PatternProviderBlockEntity implements MirroredSimplePatternProviderHost
{

    public SimplePatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new MirroredSimplePatternProviderLogic(getMainNode(), this, 5);
    }

    @Override
    public void openMenu(Player player, MenuLocator locator)
    {
        var targetRef = getMirroringLogic().getMirrorTarget();
        if (targetRef != null)
        {
            MenuLocator targetLocator = targetRef.toMenuLocator(level);
            PatternProviderLogicHost mirrorTarget = getMirroringLogic().resolveMirrorTargetHost();
            if (mirrorTarget != null && targetLocator != null)
            {
                mirrorTarget.openMenu(player, targetLocator);
                return;
            }
        }

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

    @Override
    public MirroredSimplePatternProviderLogic getMirroringLogic()
    {
        return (MirroredSimplePatternProviderLogic) getLogic();
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM)
        {
            getMirroringLogic().readMirrorSettings(input);
            setChanged();
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output, @Nullable Player player)
    {
        super.exportSettings(mode, output, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM)
        {
            getMirroringLogic().writeMirrorSettings(output);
        }
    }
}
