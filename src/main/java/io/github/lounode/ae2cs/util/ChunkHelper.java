package io.github.lounode.ae2cs.util;

import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class ChunkHelper {

    private static final Map<ServerLevel, Set<Long>> LOADED_CHUNKS = new HashMap<>();

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

    /**
     * Collects block entities from every fully loaded chunk in a dimension.
     */
    public static List<BlockEntity> getLoadedBlockEntities(ServerLevel level) {
        List<BlockEntity> blockEntities = new ArrayList<>();
        Set<Long> loadedChunks = LOADED_CHUNKS.get(level);
        if (loadedChunks == null) {
            return blockEntities;
        }

        for (long packedPos : loadedChunks) {
            ChunkPos chunkPos = new ChunkPos(packedPos);
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                blockEntities.addAll(chunk.getBlockEntities().values());
            }
        }
        return blockEntities;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        LOADED_CHUNKS.computeIfAbsent(level, ignored -> new HashSet<>()).add(chunk.getPos().toLong());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Set<Long> loadedChunks = LOADED_CHUNKS.get(level);
        if (loadedChunks == null) {
            return;
        }

        loadedChunks.remove(event.getChunk().getPos().toLong());
        if (loadedChunks.isEmpty()) {
            LOADED_CHUNKS.remove(level);
        }
    }
}
