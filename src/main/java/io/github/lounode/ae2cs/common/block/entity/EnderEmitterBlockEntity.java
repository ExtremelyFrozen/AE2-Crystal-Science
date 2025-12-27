package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.networking.*;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.parts.IPart;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.parts.CableBusContainer;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.util.GlobalChunkPos;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class EnderEmitterBlockEntity extends AENetworkedBlockEntity implements ServerTickingBlockEntity
{
    /**
     * 以全局区块坐标为索引的发信器位置表，用来快速寻找发信器，每个区块key下的set集合都对应周围3x3区块范围内所有发信器
     */
    public static Map<GlobalChunkPos, Set<BlockPos>> EMITTER_CHUNK_POSITIONS = new HashMap<>();

    public static MinecraftServer boundServer = null;

    // 最大可连接距离，半径，计算时使用直线距离
    private static int maxLinkDistance = 16;

    private Set<BlockPos> pendingLinkPositions = new HashSet<>();
    private Set<BlockPos> linkedPositions = new HashSet<>();
    /**
     * 到目标位置的连接，显式清除连接，方块破坏和区块卸载时只需清表，不需要手动摧毁连接，ae已经处理了这件事
     */
    private Map<BlockPos, List<IGridConnection>> linkedConnections = new HashMap<>();
    private int recentAddedPosCountdown = 2;

    public EnderEmitterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.ENDER_EMITTER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
    }

    @Override
    public void serverTick()
    {
        if (isRecentAddedPos()) return;
        if (level == null) return;
        IGridNode selfNode = getMainNode().getNode();
        if (selfNode == null) return;
        if (selfNode.getUsedChannels() >= getMaxLinkChannels()) return;

        if (!selfNode.isActive()) return;

        BlockPos connectedPos = null;
        for (BlockPos targetPos : pendingLinkPositions)
        {
            // 如果目标位置有链接，先摧毁
            List<IGridConnection> oldConnections = linkedConnections.remove(targetPos);
            if (oldConnections != null)
            {
                for (IGridConnection connection : oldConnections)
                {
                    connection.destroy();
                }
            }

            // 连接
            List<IGridNode> targetNodes = getConnectableNodes(level, targetPos);
            if (targetNodes.isEmpty()) continue;

            ArrayList<IGridConnection> newConnections = new ArrayList<>();
            for (IGridNode targetNode : targetNodes)
            {
                if (targetNode == null) continue;
                newConnections.add(GridHelper.createConnection(selfNode, targetNode));
            }
            // 这一轮有任何成功链接就结束，剩下的等待下一轮
            if (!newConnections.isEmpty())
            {
                linkedConnections.put(targetPos, newConnections);
                connectedPos = targetPos;
                break;
            }
        }
        if (connectedPos != null)
        {
            pendingLinkPositions.remove(connectedPos);
            linkedPositions.add(connectedPos);
        }
    }

    /**
     * 如果返回真，则说明最近添加了新pos，需要延后工作
     */
    private boolean isRecentAddedPos()
    {
        boolean result = recentAddedPosCountdown > 0;
        this.recentAddedPosCountdown--;
        return result;
    }

    private void addPosToPending(BlockPos pos)
    {
        this.pendingLinkPositions.add(pos);
        this.recentAddedPosCountdown = 2;
    }

    private void addListPosToPending(Collection<BlockPos> posList)
    {
        this.pendingLinkPositions.addAll(posList);
        this.recentAddedPosCountdown = 2;
    }

    private int getMaxLinkChannels()
    {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return 0;

        IPathingService pathingService = grid.getPathingService();
        ChannelMode mode = pathingService.getChannelMode();

        if (pathingService.getControllerState() == ControllerState.CONTROLLER_ONLINE)
        {
            return 32 * mode.getCableCapacityFactor();
        }
        else
        {
            return mode.getAdHocNetworkChannels();
        }
    }

    @Override
    public void onReady()
    {
        super.onReady();
        // 节点准备好之后加入到缓存表
        if (level != null && !level.isClientSide())
        {
            ChunkPos center = new ChunkPos(worldPosition);
            for (int offsetX = -1; offsetX <= 1; offsetX++)
            {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++)
                {
                    GlobalChunkPos chunkKey = new GlobalChunkPos(level.dimension(), center.x + offsetX, center.z + offsetZ);
                    EMITTER_CHUNK_POSITIONS
                            .computeIfAbsent(chunkKey, key -> new HashSet<>())
                            .add(worldPosition.immutable());
                }
            }
        }
        // 将linked转入pending，等待自动重连
        pendingLinkPositions.clear();
        addListPosToPending(linkedPositions);
        linkedPositions.clear();
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        // 区块卸载时从缓存表移除
        if (level != null && !level.isClientSide())
        {
            ChunkPos center = new ChunkPos(worldPosition);
            BlockPos posKey = worldPosition.immutable();

            for (int offsetX = -1; offsetX <= 1; offsetX++)
            {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++)
                {
                    GlobalChunkPos chunkKey = new GlobalChunkPos(level.dimension(), center.x + offsetX, center.z + offsetZ);

                    Set<BlockPos> set = EMITTER_CHUNK_POSITIONS.get(chunkKey);
                    if (set != null)
                    {
                        set.remove(posKey);
                        if (set.isEmpty())
                        {
                            EMITTER_CHUNK_POSITIONS.remove(chunkKey);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);

        ListTag linkPositions = new ListTag();
        for (BlockPos pos : linkedPositions)
        {
            try
            {
                Tag t = BlockPos.CODEC
                        .encodeStart(NbtOps.INSTANCE, pos)
                        .getOrThrow();
                linkPositions.add(t);
            }
            catch (Throwable e)
            {
                // 静默
            }
        }

        data.put("linked_positions", linkPositions);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);

        linkedPositions.clear();

        Tag root = data.get("linked_positions");
        if (!(root instanceof ListTag list)) return;

        for (Tag t : list)
        {
            try
            {
                BlockPos.CODEC
                        .parse(NbtOps.INSTANCE, t)
                        .resultOrPartial(msg -> {
                        })
                        .ifPresent(pos -> linkedPositions.add(pos.immutable()));
            }
            catch (Throwable e)
            {
                // 忽略
            }
        }
    }

    /**
     * 获取目标位置中最适合被无线连接的一个或一群节点，其内部元素无法判别是否为null，需要外部自行判断
     */
    public static List<IGridNode> getConnectableNodes(Level level, BlockPos pos)
    {
        ArrayList<IGridNode> nodes = new ArrayList<>();

        IInWorldGridNodeHost nodeHost = level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, pos);
        if (nodeHost == null) return nodes;

        // 无线连接拒绝控制器
        if (nodeHost instanceof ControllerBlockEntity) return nodes;
        else if (nodeHost instanceof CableBusBlockEntity cable)
        {
            // 线缆节点，优先获取中间节点，如不存在则将六个面的节点都加入
            CableBusContainer cableBus = cable.getCableBus();
            IPart center = cableBus.getPart(null);
            if (center != null)
            {
                nodes.add(center.getGridNode());
            }
            else
            {
                for (Direction direction : Direction.values())
                {
                    IPart part = cableBus.getPart(direction);
                    if (part != null)
                    {
                        nodes.add(part.getGridNode());
                    }
                }
            }
        }
        else
        {
            // 普通节点，任意一面取得即可
            for (Direction direction : Direction.values())
            {
                IGridNode node = nodeHost.getGridNode(direction);
                if (node != null)
                {
                    nodes.add(node);
                    break;
                }
            }
        }

        return nodes;
    }

    // 用于自动链接方块-------------------------------------------------------
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
    {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevelAccessor sla)
        {
            addPosToRecentEmitter(sla.getLevel(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        LevelAccessor targetLevelAccessor = event.getLevel();
        if (targetLevelAccessor instanceof ServerLevelAccessor sla)
        {
            removePosFromRecentEmitter(sla.getLevel(), event.getPos());
        }
    }

    /**
     * 将需要连接的位置添加到指定发信器
     */
    public static boolean addPosToEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos pos)
    {
        IGridNode emitterNode = emitter.getMainNode().getNode();
        if (emitterNode == null) return false;

        if (emitterNode.getUsedChannels() < emitter.getMaxLinkChannels())
        {
            emitter.addPosToPending(pos);
            return true;
        }
        return false;
    }

    /**
     * 从发信器中移除某个特定的位置
     */
    public static void removePosFromEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos targetPos)
    {
        emitter.pendingLinkPositions.remove(targetPos);
        emitter.linkedPositions.remove(targetPos);
        emitter.linkedConnections.remove(targetPos); // 连接本体会被ae自动摧毁，这里只需要丢掉引用
    }

    /**
     * 自动将当前位置添加到最近一个可用发信器
     */
    public static void addPosToRecentEmitter(@NotNull Level targetLevel, @NotNull BlockPos targetPos)
    {
        // 确认目标方块状态
        IInWorldGridNodeHost targetNodeHost = GridHelper.getNodeHost(targetLevel, targetPos);
        if (targetNodeHost == null) return;

        // 确认最佳发信器状态
        GlobalChunkPos targetChunkPos = new GlobalChunkPos(targetLevel.dimension(), targetPos);
        Set<BlockPos> linkPositions = EMITTER_CHUNK_POSITIONS.get(targetChunkPos);
        if (linkPositions == null || linkPositions.isEmpty()) return;
        double maxDistSq = (double) maxLinkDistance * (double) maxLinkDistance;
        List<BlockPos> availablePositions = new ArrayList<>(linkPositions.size());
        for (BlockPos linkPos : linkPositions)
        {
            if (linkPos.distSqr(targetPos) <= maxDistSq)
            {
                availablePositions.add(linkPos);
            }
        }
        if (availablePositions.isEmpty()) return;
        availablePositions.sort(Comparator.comparingDouble(p -> p.distSqr(targetPos)));

        // 迭代寻找可用发信器并与之连接
        for (BlockPos linkPos : availablePositions)
        {
            if (targetLevel.getBlockEntity(linkPos) instanceof EnderEmitterBlockEntity emitter)
            {
                if (addPosToEmitter(emitter, targetPos))
                    return;
            }
        }
    }

    /**
     * 将指定位置信息从附近的发信器索引中移除
     */
    public static void removePosFromRecentEmitter(@NotNull Level targetLevel, @NotNull BlockPos targetPos)
    {
        IInWorldGridNodeHost targetNodeHost = GridHelper.getNodeHost(targetLevel, targetPos);
        if (targetNodeHost == null) return;

        // 移除相关发信器中的连接状态
        GlobalChunkPos targetChunkPos = new GlobalChunkPos(targetLevel.dimension(), targetPos);
        Set<BlockPos> linkPositions = EMITTER_CHUNK_POSITIONS.get(targetChunkPos);
        if (linkPositions == null || linkPositions.isEmpty()) return;
        for (BlockPos linkPos : linkPositions)
        {
            if (targetLevel.getBlockEntity(linkPos) instanceof EnderEmitterBlockEntity emitter)
            {
                removePosFromEmitter(emitter, targetPos);
            }
        }
    }

    // 用来确定缓存表可用性-----------------------------------------------------
    public static void ensureBound(@Nullable MinecraftServer server)
    {
        if (server == null)
        {
            EMITTER_CHUNK_POSITIONS.clear();
            boundServer = null;
            return;
        }
        if (boundServer != server)
        {
            EMITTER_CHUNK_POSITIONS.clear();
            boundServer = server;
        }
    }

    @SubscribeEvent
    public static void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent e)
    {
        ensureBound(e.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e)
    {
        EMITTER_CHUNK_POSITIONS.clear();
        boundServer = null;
    }
}
