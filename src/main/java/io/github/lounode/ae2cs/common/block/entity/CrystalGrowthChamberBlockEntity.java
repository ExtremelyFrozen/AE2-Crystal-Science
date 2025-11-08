package io.github.lounode.ae2cs.common.block.entity;

import appeng.blockentity.AEBaseBlockEntity;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalGrowthChamberBlockEntity extends AEBaseBlockEntity
{

    protected NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);

    public CrystalGrowthChamberBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(), pos, blockState);
    }


    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        this.items = NonNullList.withSize(this.items.size(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(data, this.items, registries);
    }
}
