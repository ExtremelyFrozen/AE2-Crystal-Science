package io.github.lounode.ae2cs.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkHelper {

    /**
     * 在指定的已加载区块范围内收集所有的BE并进列表
     */
    public static List<BlockEntity> getBlockEntitiesInChunks(ServerLevel level, ChunkPos centerChunk, int offset) {
        List<BlockEntity> blockEntitiesInChunks = new ArrayList<BlockEntity>();
        for (int offsetX = -offset; offsetX <= offset; offsetX++) {
            for (int offsetZ = -offset; offsetZ <= offset; offsetZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(centerChunk.x + offsetX, centerChunk.z + offsetZ);
                if (chunk == null) continue;

                blockEntitiesInChunks.addAll(chunk.getBlockEntities().values());
            }
        }
        return blockEntitiesInChunks;
    }
}
