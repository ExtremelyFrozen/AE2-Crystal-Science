package io.github.lounode.ae2cs.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * 用于 Gson 序列化的 GlobalPos 包装：dimensionId + x/y/z
 */
public record GlobalPosJson(String dimensionId, int x, int y, int z) {

    public static GlobalPosJson from(GlobalPos pos) {
        Objects.requireNonNull(pos, "pos");
        String dim = pos.dimension().location().toString();
        BlockPos bp = pos.pos();
        return new GlobalPosJson(dim, bp.getX(), bp.getY(), bp.getZ());
    }

    public GlobalPos toGlobalPos() {
        if (dimensionId == null || dimensionId.isBlank()) {
            throw new IllegalStateException("dimensionId is blank");
        }

        ResourceLocation id = ResourceLocation.tryParse(dimensionId);
        if (id == null) {
            throw new IllegalStateException("Invalid dimensionId: " + dimensionId);
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, id);
        return GlobalPos.of(dimKey, new BlockPos(x, y, z));
    }
}
