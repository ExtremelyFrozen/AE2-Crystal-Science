package io.github.lounode.ae2cs.common.item;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.AEBasePart;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用于升级方块和接口的Item
 */
public class UpgradeItem extends Item
{
    private static final Map<Block, Block> BLOCK_REPLACE_INFO = new IdentityHashMap<>();
    private static final Map<IPartItem<?>, IPartItem<?>> PART_REPLACE_INFO = new IdentityHashMap<>();

    private final Set<ItemLike> allowedReplacement = new HashSet<>();

    public UpgradeItem(Properties properties)
    {
        super(properties);
    }

    protected void addAllowedReplacement(ItemLike item)
    {
        allowedReplacement.add(item);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context)
    {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockEntity originalBe = level.getBlockEntity(pos);
        if (originalBe != null)
        {
            BlockPlaceContext ctx = new BlockPlaceContext(context);
            if (BLOCK_REPLACE_INFO.containsKey(originalBe.getBlockState().getBlock()) &&
                    allowedReplacement.contains(originalBe.getBlockState().getBlock().asItem()))
            {
                BlockState originState = level.getBlockState(pos);
                Block newBlock = BLOCK_REPLACE_INFO.get(originState.getBlock());
                BlockState newState = newBlock.getStateForPlacement(ctx);
                if (newState == null)
                {
                    return InteractionResult.PASS;
                }

                CompoundTag originalData = originalBe.saveWithoutMetadata(level.registryAccess());
                if(originalBe instanceof AEBaseBlockEntity aeBaseBlockEntity)
                    aeBaseBlockEntity.clearContent();
                int flags = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;
                level.setBlock(pos, newState, flags);

                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe != null)
                {
                    newBe.loadWithComponents(originalData, level.registryAccess());
                    newBe.setChanged();
                    level.sendBlockUpdated(pos, newState, newState, Block.UPDATE_CLIENTS);
                }
                context.getItemInHand().shrink(1);
                return InteractionResult.CONSUME;
            }
            else if (originalBe instanceof CableBusBlockEntity cable)
            {
                Vec3 hitVec = context.getClickLocation();
                Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
                IPart part = cable.getCableBus().selectPartLocal(hitInBlock).part;
                if (part instanceof AEBasePart basePart &&
                        PART_REPLACE_INFO.containsKey(part.getPartItem()) &&
                        allowedReplacement.contains(part.getPartItem().asItem()))
                {
                    Direction side = basePart.getSide();
                    CompoundTag contents = new CompoundTag();
                    IPartItem<?> partItem = PART_REPLACE_INFO.get(part.getPartItem());
                    part.writeToNBT(contents, level.registryAccess());
                    IPart newPart = cable.replacePart(partItem, side, context.getPlayer(), null);
                    if (newPart != null)
                    {
                        newPart.readFromNBT(contents, level.registryAccess());
                        newPart.addToWorld();
                    }
                }
                else
                {
                    return InteractionResult.PASS;
                }
                context.getItemInHand().shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public static void registerReplaceInfo(Block from, Block to)
    {
        BLOCK_REPLACE_INFO.put(from, to);
    }

    public static void registerReplaceInfo(IPartItem<?> from, IPartItem<?> to)
    {
        PART_REPLACE_INFO.put(from, to);
    }

    public static void init()
    {
        registerReplaceInfo(AECSBlocks.ENDER_INTERFACE_BLOCK.get(), AECSBlocks.EX_ENDER_INTERFACE_BLOCK.get());
        registerReplaceInfo(AECSParts.ENDER_INTERFACE_PART.get(), AECSParts.EX_ENDER_INTERFACE_PART.get());

        // 将物品归一化，保证为同一实例
        for(DeferredItem<? extends Item> item : AECSItems.getALL())
        {
            if(item.asItem() instanceof UpgradeItem upgradeItem)
            {
                Set<ItemLike> clone = new HashSet<>(upgradeItem.allowedReplacement);
                upgradeItem.allowedReplacement.clear();
                for(ItemLike itemLike : clone)
                {
                    upgradeItem.allowedReplacement.add(itemLike.asItem());
                }
            }
        }
    }
}