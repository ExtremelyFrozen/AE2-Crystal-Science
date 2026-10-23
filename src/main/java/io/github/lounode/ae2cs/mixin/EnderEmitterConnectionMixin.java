package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.common.block.entity.EnderEmitterBlockEntity;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.GridConnection;
import appeng.me.GridNode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GridConnection.class, remap = false)
public abstract class EnderEmitterConnectionMixin {

    @Shadow
    public abstract GridNode a();

    @Shadow
    public abstract GridNode b();

    @Inject(method = "getMaxChannels", at = @At("HEAD"), cancellable = true)
    private void ae2cs$bandConnectionCapacity(CallbackInfoReturnable<Integer> cir) {
        if (a().getOwner() instanceof ControllerBlockEntity && b().getOwner() instanceof EnderEmitterBlockEntity emitter && emitter.isConnectedToBand()) {
            cir.setReturnValue(emitter.getMaxChannels());
        } else if (b().getOwner() instanceof ControllerBlockEntity && a().getOwner() instanceof EnderEmitterBlockEntity emitter && emitter.isConnectedToBand()) {
            cir.setReturnValue(emitter.getMaxChannels());
        }
    }
}
