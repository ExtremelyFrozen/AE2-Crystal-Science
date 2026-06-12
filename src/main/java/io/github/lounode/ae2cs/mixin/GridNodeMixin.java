package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.api.CustomChannelProviderHost;

import appeng.me.GridNode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GridNode.class, remap = false)
public abstract class GridNodeMixin {

    @Shadow
    public abstract Object getOwner();

    @Inject(method = "getMaxChannels", at = @At("HEAD"), cancellable = true)
    public void ae2cs$getMaxChannels(CallbackInfoReturnable<Integer> cir) {
        if (getOwner() instanceof CustomChannelProviderHost host && host.isEnabledCustomChannel()) {
            cir.setReturnValue(host.getMaxChannels());
        }
    }
}
