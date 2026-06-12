package io.github.lounode.ae2cs.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record GlobalChunkPos(ResourceKey<Level> dimension, ChunkPos chunkPos) {

    public GlobalChunkPos(GlobalPos pos) {
        this(pos.dimension(), new ChunkPos(pos.pos()));
    }

    public GlobalChunkPos(ResourceKey<Level> dimension, BlockPos pos) {
        this(dimension, new ChunkPos(pos));
    }

    public GlobalChunkPos(ResourceKey<Level> dimension, int x, int z) {
        this(dimension, new ChunkPos(x, z));
    }

    public GlobalChunkPos(ResourceKey<Level> dimension, long packedPos) {
        this(dimension, new ChunkPos(packedPos));
    }
}
