package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;
import io.github.lounode.ae2cs.common.item.ResonatingMemoryCardHelper;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CableBusBlockEntity.class, remap = false)
public class CableBusBlockEntityMixin extends AEBaseBlockEntity {

    public CableBusBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Inject(method = "addPart", at = @At("RETURN"))
    public void ae2cs$addPart(IPartItem<? extends IPart> partItem, Direction side, @Nullable Player player,
                              CallbackInfoReturnable<? extends IPart> cir) {
        if (cir.getReturnValue() != null && level != null) {
            EnderEmitterBlockEntity.addPosToRecentEmitter(level, worldPosition);
            if (!level.isClientSide() && player != null) {
                ResonatingMemoryCardHelper.tryApplyToPart(player, cir.getReturnValue());
            }
        }
    }

    @Inject(method = "replacePart", at = @At("RETURN"))
    public void ae2cs$replacePart(IPartItem<? extends IPart> partItem, @Nullable Direction side, Player owner, InteractionHand hand,
                                  CallbackInfoReturnable<? extends IPart> cir) {
        if (cir.getReturnValue() != null && level != null) {
            EnderEmitterBlockEntity.addPosToRecentEmitter(level, worldPosition);
            if (!level.isClientSide()) {
                ResonatingMemoryCardHelper.tryApplyToPart(owner, cir.getReturnValue());
            }
        }
    }

    @Inject(method = "removePart", at = @At("RETURN"))
    public void ae2cs$removePart(IPart part, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && level != null) {
            // 不使用remove，而是使用add对其进行刷新
            EnderEmitterBlockEntity.addPosToRecentEmitter(level, worldPosition);
        }
    }
}
