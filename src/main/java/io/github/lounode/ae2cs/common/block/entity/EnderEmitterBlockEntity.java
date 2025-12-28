package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.networking.*;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.parts.IPart;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.parts.CableBusContainer;
import io.github.lounode.ae2cs.Config;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.util.GlobalChunkPos;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.util.ChunkHelper;
import io.github.lounode.ae2cs.util.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class EnderEmitterBlockEntity extends AENetworkedBlockEntity implements ServerTickingBlockEntity,
        IUpgradeableObject
{
    /**
     * 以全局区块坐标为索引的发信器位置表，用来快速寻找发信器，每个区块key下的set集合都对应周围3x3区块范围内所有发信器
     */
    public static Map<GlobalChunkPos, Set<BlockPos>> EMITTER_CHUNK_POSITIONS = new HashMap<>();

    public static MinecraftServer boundServer = null;

    public static final int autoAreaFactor = Config.INSTANCE.startUpConfig.enderEmitterAutoAreaFactor.getAsInt();

    // 最大可连接距离，半径，计算时使用直线距离
    public static final int maxLinkDistance = 16 * autoAreaFactor;

    private boolean autoMode = true;
    private boolean allowAutoLinkCableLike = false;
    private int linkDistance = 8;
    private final Set<BlockPos> pendingLinkPositions = new HashSet<>();
    private final Set<BlockPos> linkedPositions = new HashSet<>();
    /**
     * 到目标位置的连接，显式清除连接，方块破坏和区块卸载时只需清表，不需要手动摧毁连接，ae已经处理了这件事
     */
    private final Map<BlockPos, List<IGridConnection>> linkedConnections = new HashMap<>();
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

    public boolean isAutoMode()
    {
        return autoMode;
    }

    public void setAutoMode(boolean autoMode)
    {
        this.autoMode = autoMode;
        setChanged();
    }

    public int getLinkDistance()
    {
        return linkDistance;
    }

    public void setLinkDistance(int linkDistance)
    {
        this.linkDistance = Math.max(0, Math.min(linkDistance, maxLinkDistance));
        setChanged();
    }

    public boolean allowAutoLinkCableLike()
    {
        return allowAutoLinkCableLike;
    }

    public void setAllowAutoLinkCableLike(boolean allowAutoLinkCableLike)
    {
        this.allowAutoLinkCableLike = allowAutoLinkCableLike;
        setChanged();
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

        for (Iterator<BlockPos> it = pendingLinkPositions.iterator(); it.hasNext(); )
        {
            BlockPos targetPos = it.next();

            // 不处理未加载区块的情况
            if (!level.isLoaded(targetPos)) continue;

            // 如果目标位置已有旧链接，先摧毁
            List<IGridConnection> oldConnections = linkedConnections.remove(targetPos);
            if (oldConnections != null)
            {
                for (IGridConnection connection : oldConnections)
                {
                    if (connection != null)
                    {
                        connection.destroy();
                    }
                }
            }

            // 尝试建立连接，这里targets会自动筛掉不符合条件的
            List<IGridNode> targetNodes = getConnectableNodes(level, targetPos);
            if (targetNodes.isEmpty())
            {
                // 无任何可连接节点
                it.remove();
                continue;
            }

            ArrayList<IGridConnection> newConnections = new ArrayList<>();
            for (IGridNode targetNode : targetNodes)
            {
                IGrid targetGrid = targetNode.getGrid();
                IGrid selfGrid = selfNode.getGrid();
                if (targetGrid != null && selfGrid != null)
                {
                    if (targetGrid.getPathingService().getControllerState() != ControllerState.NO_CONTROLLER)
                    {
                        if (targetGrid != selfGrid)
                            continue; // 不连接导致冲突的网络
                        else if (targetNode.meetsChannelRequirements())
                            continue; // 同一个网络内，若对方已经分配有频道，不连接
                    }
                }

                try
                {
                    newConnections.add(GridHelper.createConnection(selfNode, targetNode));
                }
                catch (IllegalStateException e)
                {
                    // 此错误说明两者之间已有连接，无需log记录
                }
            }

            if (newConnections.isEmpty())
            {
                // 本轮没有完成任何连接
                it.remove();
                continue;
            }

            // 本轮有任何成功链接就结束，剩下的等待下一轮
            linkedConnections.put(targetPos, newConnections);
            it.remove();
            linkedPositions.add(targetPos);
            setChanged();
            break;
        }
    }

    /**
     * 如果返回真，则说明最近添加了新pos，需要延后工作
     */
    private boolean isRecentAddedPos()
    {
        if (recentAddedPosCountdown > 0)
        {
            recentAddedPosCountdown--;
            return true;
        }
        return false;
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

        if (mode == ChannelMode.INFINITE) return Integer.MAX_VALUE;

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
            for (int offsetX = -autoAreaFactor; offsetX <= autoAreaFactor; offsetX++)
            {
                for (int offsetZ = -autoAreaFactor; offsetZ <= autoAreaFactor; offsetZ++)
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
    public void setRemoved()
    {
        super.setRemoved();
        // 方块被移除或区块卸载时从全局索引移除
        if (level != null && !level.isClientSide())
        {
            ChunkPos center = new ChunkPos(worldPosition);
            BlockPos posKey = worldPosition.immutable();

            for (int offsetX = -autoAreaFactor; offsetX <= autoAreaFactor; offsetX++)
            {
                for (int offsetZ = -autoAreaFactor; offsetZ <= autoAreaFactor; offsetZ++)
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

        data.putInt("link_distance", this.linkDistance);
        data.putBoolean("auto_mode", this.autoMode);
        data.putBoolean("allow_auto_link_cable_like", this.allowAutoLinkCableLike);

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

        this.linkDistance = data.getInt("link_distance");
        this.autoMode = data.getBoolean("auto_mode");
        this.allowAutoLinkCableLike = data.getBoolean("allow_auto_link_cable_like");

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
     * 获取目标位置中最适合被无线连接的一个或一群节点，其内部元素亦不为null
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

        nodes.removeIf(Objects::isNull);
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
    public static boolean addPosToEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos pos, boolean byManual, boolean forceAuto)
    {
        // 如果是手动连接，仅检查手动方法
        boolean valid = byManual && VecHelper.closerThanChebyshev(emitter.worldPosition, pos, maxLinkDistance);

        // 自动连接额外检查
        if (!valid)
        {
            if (emitter.level == null) return false;
            IGridNode emitterNode = emitter.getMainNode().getNode();
            if (emitterNode == null) return false;
            IInWorldGridNodeHost targetNodeHost = emitter.level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, pos);
            valid = (forceAuto || emitter.isAutoMode())
                    && emitterNode.getUsedChannels() < emitter.getMaxLinkChannels()
                    && (!(targetNodeHost instanceof CableBusBlockEntity) || emitter.allowAutoLinkCableLike())
                    && VecHelper.closerThanChebyshev(emitter.worldPosition, pos, emitter.linkDistance);
        }

        if (valid)
        {
            emitter.addPosToPending(pos);
            emitter.setChanged();
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
        emitter.setChanged();
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

        List<BlockPos> availablePositions = new ArrayList<>(linkPositions.size());
        for (BlockPos linkPos : linkPositions)
        {
            if (VecHelper.closerThanChebyshev(linkPos, targetPos, maxLinkDistance))
            {
                availablePositions.add(linkPos);
            }
        }
        if (availablePositions.isEmpty()) return;

        // 按欧式距离排序优先级
        availablePositions.sort(Comparator.comparingDouble(pos -> pos.distSqr(targetPos)));

        // 迭代寻找可用发信器并与之连接
        for (BlockPos linkPos : availablePositions)
        {
            if (targetLevel.getBlockEntity(linkPos) instanceof EnderEmitterBlockEntity emitter)
            {
                // 打开自动模式、检查emitter的独特距离，最后再尝试加入到emitter
                if (addPosToEmitter(emitter, targetPos, false, false))
                {
                    return;
                }
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

    /**
     * 用来主动将附近区块的be全部添加进表
     */
    public static void addAllRecentBEtoEmitter(@NotNull EnderEmitterBlockEntity emitter)
    {
        Level targetLevel = emitter.level;
        if (targetLevel instanceof ServerLevel serverLevel)
        {
            ChunkPos centerChunk = new ChunkPos(emitter.worldPosition);
            List<BlockEntity> blockEntities = ChunkHelper.getBlockEntitiesInChunks(serverLevel, centerChunk, autoAreaFactor);
            for (BlockEntity be : blockEntities)
            {
                addPosToEmitter(emitter, be.getBlockPos(), false, true);
            }
        }
    }

    /**
     * 手动显式清除所有连接
     */
    public static void removeAllLinkedFromEmitter(@NotNull EnderEmitterBlockEntity emitter)
    {
        for (Collection<IGridConnection> connections : emitter.linkedConnections.values())
        {
            for (IGridConnection connection : connections)
            {
                connection.destroy();
            }
        }
        emitter.linkedConnections.clear();
        emitter.linkedPositions.clear();
        emitter.pendingLinkPositions.clear();
        emitter.setChanged();
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
