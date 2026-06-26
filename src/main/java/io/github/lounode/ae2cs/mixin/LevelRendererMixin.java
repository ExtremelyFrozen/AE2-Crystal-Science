package io.github.lounode.ae2cs.mixin;

import io.github.lounode.ae2cs.api.render.ICustomRenderBounding;

import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LevelRenderer.class, priority = 800)
public abstract class LevelRendererMixin {

    @WrapOperation(
                   method = "renderLevel",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getRenderBoundingBox()Lnet/minecraft/world/phys/AABB;",
                            remap = false),
                   require = 0)
    private AABB aecs$wrapCableBusGetRenderBoundingBox(BlockEntity be, Operation<AABB> original) {
        final AABB base;
        try {
            base = original.call(be);
        } catch (Throwable t) {
            return new AABB(be.getBlockPos());
        }

        if (!(be instanceof CableBusBlockEntity te)) {
            return base;
        }

        AABB result = base;
        final BlockPos center = te.getBlockPos();

        for (Direction dir : Direction.values()) {
            IPart part = te.getPart(dir);
            if (!(part instanceof ICustomRenderBounding custom)) {
                continue;
            }

            AABB customBox = custom.getCustomBoundingBox(center);
            result = result.minmax(customBox);
        }

        return result;
    }
}
