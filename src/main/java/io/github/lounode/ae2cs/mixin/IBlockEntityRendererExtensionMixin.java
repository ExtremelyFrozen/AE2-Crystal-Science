package io.github.lounode.ae2cs.mixin;

import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.CableBusTESR;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.lounode.ae2cs.api.render.ICustomRenderBounding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IBlockEntityRendererExtension.class, remap = false)
public interface IBlockEntityRendererExtensionMixin
{
    @ModifyReturnValue(
            method = "getRenderBoundingBox(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/world/phys/AABB;",
            at = @At("RETURN")
    )
    private AABB aecs$getRenderBoundingBox(AABB original, BlockEntity be)
    {
        if (!(this instanceof CableBusTESR)) {
            return original;
        }
        if (!(be instanceof CableBusBlockEntity te)) {
            return original;
        }

        BlockPos center = te.getBlockPos();
        AABB result = original;

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
