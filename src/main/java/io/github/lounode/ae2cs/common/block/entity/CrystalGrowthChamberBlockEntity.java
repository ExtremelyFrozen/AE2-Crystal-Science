package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.inventory.CrystalGrowthChamberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalGrowthChamberBlockEntity extends BaseContainerBlockEntity {

    protected NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);

    public CrystalGrowthChamberBlockEntity(BlockPos pos, BlockState blockState) {
        super(AE2CrystalSeedsBlockEntities.CRYSTAL_GROWTH_CHAMBER, pos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.ae2_crystal_seeds.crystal_growth_chamber");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CrystalGrowthChamberMenu(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
}
