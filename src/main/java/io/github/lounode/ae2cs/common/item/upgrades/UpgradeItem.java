package io.github.lounode.ae2cs.common.item.upgrades;

import io.github.lounode.ae2cs.util.BlockStateAligner;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.AEBasePart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class UpgradeItem extends Item {

    private final List<ReplaceEntry<Block>> pendingBlockReplacements = new ArrayList<>();
    private final List<ReplaceEntry<IPartItem<?>>> pendingPartReplacements = new ArrayList<>();
    private final Map<Block, Block> blockReplaceInfo = new IdentityHashMap<>();
    private final Map<IPartItem<?>, IPartItem<?>> partReplaceInfo = new IdentityHashMap<>();
    private boolean initialized = false;

    public UpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        init();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockEntity originalBe = level.getBlockEntity(pos);
        if (originalBe != null) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            BlockPlaceContext ctx = new BlockPlaceContext(context);
            if (blockReplaceInfo.containsKey(originalBe.getBlockState().getBlock())) {
                BlockState originState = level.getBlockState(pos);
                Block newBlock = blockReplaceInfo.get(originState.getBlock());
                BlockState placed = newBlock.getStateForPlacement(ctx);
                if (placed == null) {
                    return InteractionResult.PASS;
                }
                BlockState newState = BlockStateAligner.align(originState, placed);

                CompoundTag originalData = originalBe.saveWithoutMetadata();
                level.removeBlockEntity(pos); // 先行移除掉对应的BE，防止物品掉落
                level.removeBlock(pos, false);
                level.setBlock(pos, newState, Block.UPDATE_ALL);
                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe != null) {
                    newBe.load(originalData);
                    newBe.setChanged();
                }
                context.getItemInHand().shrink(1);
                return InteractionResult.CONSUME;
            } else if (originalBe instanceof CableBusBlockEntity cable) {
                Vec3 hitVec = context.getClickLocation();
                Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
                IPart part = cable.getCableBus().selectPartLocal(hitInBlock).part;
                if (part instanceof AEBasePart basePart &&
                        partReplaceInfo.containsKey(part.getPartItem())) {
                    Direction side = basePart.getSide();
                    CompoundTag contents = new CompoundTag();
                    IPartItem<?> partItem = partReplaceInfo.get(part.getPartItem());
                    part.writeToNBT(contents);
                    IPart newPart = cable.replacePart(partItem, side, context.getPlayer(), null);
                    if (newPart != null) {
                        newPart.readFromNBT(contents);
                        newPart.addToWorld();
                    }
                    context.getItemInHand().shrink(1);
                } else {
                    return InteractionResult.PASS;
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    protected void registerBlockReplaceInfo(Supplier<? extends Block> from, Supplier<? extends Block> to) {
        pendingBlockReplacements.add(new ReplaceEntry<>(from, to));
    }

    protected void registerPartReplaceInfo(Supplier<? extends IPartItem<?>> from, Supplier<? extends IPartItem<?>> to) {
        pendingPartReplacements.add(new ReplaceEntry<>(from, to));
    }

    public void init() {
        if (initialized) {
            return;
        }
        for (ReplaceEntry<Block> entry : pendingBlockReplacements) {
            blockReplaceInfo.put(entry.from().get(), entry.to().get());
        }
        for (ReplaceEntry<IPartItem<?>> entry : pendingPartReplacements) {
            partReplaceInfo.put(entry.from().get(), entry.to().get());
        }
        initialized = true;
    }

    private record ReplaceEntry<T>(Supplier<? extends T> from, Supplier<? extends T> to) {}
}
