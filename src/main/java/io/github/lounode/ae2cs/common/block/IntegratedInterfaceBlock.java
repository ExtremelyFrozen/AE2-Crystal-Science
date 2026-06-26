package io.github.lounode.ae2cs.common.block;

import io.github.lounode.ae2cs.common.block.entity.IntegratedInterfaceBlockEntity;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

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
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegratedInterfaceBlock extends AEBaseEntityBlock<IntegratedInterfaceBlockEntity> {

    public IntegratedInterfaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PatternProviderBlock.PUSH_DIRECTION, PushDirection.ALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PatternProviderBlock.PUSH_DIRECTION);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state,
                                @NotNull Level level,
                                @NotNull BlockPos pos,
                                @NotNull Block block,
                                @NotNull BlockPos fromPos,
                                boolean isMoving) {
        var be = this.getBlockEntity(level, pos);
        if (be != null) {
            // 通知更新红石信号缓存
            be.getLogic().updateRedstoneState();
        }
    }

    public InteractionResult onActivated(Level level, BlockPos pos, Player p, InteractionHand hand, @Nullable ItemStack heldItem, BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        } else if (heldItem != null && InteractionUtil.canWrenchRotate(heldItem)) {
            this.setSide(level, pos, hit.getDirection());
            return InteractionResult.sidedSuccess(level.isClientSide());
        } else {
            if (level.getBlockEntity(pos) instanceof IntegratedInterfaceBlockEntity be) {
                if (!level.isClientSide()) {
                    be.openMenu(p, MenuLocators.forBlockEntity(be));
                }

                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public void setSide(Level level, BlockPos pos, Direction facing) {
        var currentState = level.getBlockState(pos);
        var pushSide = currentState.getValue(PatternProviderBlock.PUSH_DIRECTION).getDirection();

        PushDirection newPushDirection;
        if (pushSide == facing.getOpposite()) {
            newPushDirection = PushDirection.fromDirection(facing);
        } else if (pushSide == facing) {
            newPushDirection = PushDirection.ALL;
        } else if (pushSide == null) {
            newPushDirection = PushDirection.fromDirection(facing.getOpposite());
        } else {
            newPushDirection = PushDirection.fromDirection(Platform.rotateAround(pushSide, facing));
        }

        level.setBlockAndUpdate(pos, currentState.setValue(PatternProviderBlock.PUSH_DIRECTION, newPushDirection));
    }
}
