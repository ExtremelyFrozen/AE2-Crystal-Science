package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import io.github.lounode.ae2cs.common.block.entity.IntegratedInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class IntegratedInterfaceBlock extends AEBaseEntityBlock<IntegratedInterfaceBlockEntity>
{

    public IntegratedInterfaceBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PatternProviderBlock.PUSH_DIRECTION, PushDirection.ALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(PatternProviderBlock.PUSH_DIRECTION);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        var be = this.getBlockEntity(level, pos);
        if (be != null)
        {
            // 通知更新红石信号缓存
            be.getLogic().updateRedstoneState();
        }
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (InteractionUtil.canWrenchRotate(heldItem))
        {
            setSide(level, pos, hit.getDirection());
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                        BlockHitResult hitResult)
    {
        var be = this.getBlockEntity(level, pos);
        if (be != null)
        {
            if (!level.isClientSide())
            {
                be.openMenu(player, MenuLocators.forBlockEntity(be));
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    public void setSide(Level level, BlockPos pos, Direction facing)
    {
        var currentState = level.getBlockState(pos);
        var pushSide = currentState.getValue(PatternProviderBlock.PUSH_DIRECTION).getDirection();

        PushDirection newPushDirection;
        if (pushSide == facing.getOpposite())
        {
            newPushDirection = PushDirection.fromDirection(facing);
        }
        else if (pushSide == facing)
        {
            newPushDirection = PushDirection.ALL;
        }
        else if (pushSide == null)
        {
            newPushDirection = PushDirection.fromDirection(facing.getOpposite());
        }
        else
        {
            newPushDirection = PushDirection.fromDirection(Platform.rotateAround(pushSide, facing));
        }

        level.setBlockAndUpdate(pos, currentState.setValue(PatternProviderBlock.PUSH_DIRECTION, newPushDirection));
    }
}
