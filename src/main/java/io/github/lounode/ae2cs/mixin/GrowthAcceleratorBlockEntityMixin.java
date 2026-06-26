package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.common.item.CrystalSeedItem;

import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GrowthAcceleratorBlockEntity.class, remap = false)
public abstract class GrowthAcceleratorBlockEntityMixin extends AENetworkBlockEntity {

    public GrowthAcceleratorBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Inject(method = "onTick(I)V", at = @At("TAIL"))
    private void ae2cs$onTick(int ticksSinceLastCall, CallbackInfo ci) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        double range = 2.0;
        AABB box = new AABB(this.worldPosition).inflate(range);
        List<ItemEntity> items = this.level.getEntitiesOfClass(
                ItemEntity.class,
                box,
                e -> (!e.getItem().isEmpty() && e.getItem().getItem() instanceof CrystalSeedItem && e.isInWater()));

        for (ItemEntity item : items) {
            item.setItem(CrystalSeedItem.grow(item.getItem(), 1 * ticksSinceLastCall));
        }
    }
}
