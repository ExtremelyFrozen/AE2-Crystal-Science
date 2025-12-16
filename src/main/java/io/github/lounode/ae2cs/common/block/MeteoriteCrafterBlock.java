package io.github.lounode.ae2cs.common.block;

import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import io.github.lounode.ae2cs.common.block.entity.MeteoriteCrafterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class MeteoriteCrafterBlock extends AEBaseEntityBlock<MeteoriteCrafterBlockEntity>
{

    public MeteoriteCrafterBlock(Properties properties)
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
    protected @NotNull ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (InteractionUtil.canWrenchRotate(heldItem))
        {
            setSide(level, pos, hit.getDirection());
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        super.useWithoutItem(state, level, pos, player, hitResult);
        if (!level.isClientSide() && !player.isShiftKeyDown())
        {
            //if (level.getBlockEntity(pos) instanceof MeteoriteCrafterBlockEntity be)
            //MenuOpener.open(AECSMenus..get(), player, MenuLocators.forBlockEntity(be));
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
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
