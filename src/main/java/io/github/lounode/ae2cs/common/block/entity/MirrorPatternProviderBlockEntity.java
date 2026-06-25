package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.locator.MenuLocator;
import appeng.util.SettingsFrom;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderLogic;
import io.github.lounode.ae2cs.common.me.logic.MirrorPatternProviderHost;
import io.github.lounode.ae2cs.common.me.logic.MirroredPatternProviderTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MirrorPatternProviderBlockEntity extends PatternProviderBlockEntity implements MirrorPatternProviderHost
{
    public MirrorPatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new MirrorPatternProviderLogic(getMainNode(), this);
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

        if (!player.level().isClientSide())
        {
            player.displayClientMessage(Component.translatable("ae2cs.msg.mirror_pattern_provider.target_missing"), true);
        }
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSBlocks.MIRROR_PATTERN_PROVIDER_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSBlocks.MIRROR_PATTERN_PROVIDER_BLOCK.get());
    }

    @Override
    public MirrorPatternProviderLogic getMirroringLogic()
    {
        return (MirrorPatternProviderLogic) getLogic();
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM || mode == SettingsFrom.MEMORY_CARD)
        {
            getMirroringLogic().readMirrorSettings(input);
            setChanged();
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output, @Nullable Player player)
    {
        super.exportSettings(mode, output, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM || mode == SettingsFrom.MEMORY_CARD)
        {
            getMirroringLogic().writeMirrorSettings(output);
        }
    }

    @Override
    public void markForLogicClientUpdate()
    {
        if (level != null && !level.isClientSide())
        {
            this.markForUpdate();
        }
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data)
    {
        super.writeToStream(data);
        var target = getMirroringLogic().getMirrorTarget();
        data.writeBoolean(target != null);
        if (target != null)
        {
            target.write(data);
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data)
    {
        getMirroringLogic().setMirrorTarget(data.readBoolean() ? MirroredPatternProviderTarget.read(data) : null);
        return true;
    }
}
