package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.linker.broadcast.BandLinkHost;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastReceiverHost;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.linker.broadcast.MemoryCardBandInfo;
import io.github.lounode.ae2cs.api.render.ICustomRenderBounding;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.api.util.GlobalChunkPos;
import io.github.lounode.ae2cs.client.render.EnderEmitterClientRenderHelper;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.item.ResonatingMemoryCardHelper;
import io.github.lounode.ae2cs.util.ChunkHelper;
import io.github.lounode.ae2cs.util.VecHelper;

import appeng.api.networking.*;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.parts.IPart;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.parts.CableBusContainer;
import appeng.util.ConfigManager;
import appeng.util.SettingsFrom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = AECSConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnderEmitterBlockEntity extends AENetworkBlockEntity implements ServerTickingBlockEntity,
                                     IUpgradeableObject, ClientTickingBlockEntity, IConfigurableObject, ICustomRenderBounding,
                                     CustomChannelProviderHost, BroadcastReceiverHost, BandLinkHost, CustomReturnableSubMenuHost {

    /**
     * 以全局区块坐标为索引的发信器位置表，用来快速寻找发信器，每个区块key下的set集合都对应周围3x3区块范围内所有发信器
     */
    public static Map<GlobalChunkPos, Set<BlockPos>> EMITTER_CHUNK_POSITIONS = new HashMap<>();

    public static MinecraftServer boundServer = null;

    public static int autoAreaFactor = 1;

    // 最大可连接距离，半径，计算时使用直线距离
    public static final Supplier<Integer> maxLinkDistance = () -> 16 * autoAreaFactor;

    // 客户端 + 服务端字段
    private final IConfigManager configManager;
    private int linkDistance = 8;
    private final Set<BlockPos> pendingLinkPositions = new HashSet<>();
    private final Set<BlockPos> linkedPositions = new HashSet<>();
    /**
     * 到目标位置的连接，用于显式清除连接
     */
    private final Map<BlockPos, List<IGridConnection>> linkedConnections = new HashMap<>();
    private int recentAddedPosCountdown = 2;

    // 服务端字段

    // 客户端字段
    private boolean active = false;
    private boolean autoMode = true;
    private boolean allowAutoLinkCableLike = false;
    private int maxLinkDistanceForClient = 16;
    private boolean showLinkStatus = false;
    private int usedChannelsForClient = 0;
    private int totalChannelsForClient = 0;
    private long bandUsedChannelsForClient = 0;
    private long bandTotalChannelsForClient = 0;
    private String bandId = "";
    private int customMaxChannels = 0;
    private boolean enabledCustomChannel = false;
    private int lastBandReceiverUsedChannels = 0;

    public EnderEmitterBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
        configManager = new ConfigManager(this::onConfigChanged);
        configManager.registerSetting(AECSSettings.AUTO_LINK_MODE, AutoLinkMode.ENABLE);
        configManager.registerSetting(AECSSettings.AUTO_LINK_CABLE_MODE, AutoLinkCableMode.ENABLE);
        configManager.registerSetting(AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public int getLinkDistance() {
        return linkDistance;
    }

    public String getBandName() {
        return bandId;
    }

    public boolean isConnectedToBand() {
        return bandId != null && !bandId.isEmpty();
    }

    @Override
    public Set<Direction> getGridConnectableSides(appeng.api.orientation.BlockOrientation orientation) {
        if (isConnectedToBand()) {
            return EnumSet.noneOf(Direction.class);
        }
        return EnumSet.allOf(Direction.class);
    }

    public long getBandUsedChannelsForClient() {
        return bandUsedChannelsForClient;
    }

    public long getBandTotalChannelsForClient() {
        return bandTotalChannelsForClient;
    }

    public int getUsedChannelsForClient() {
        return usedChannelsForClient;
    }

    public int getTotalChannelsForClient() {
        return totalChannelsForClient;
    }

    public boolean allowAutoLinkCableLike() {
        return allowAutoLinkCableLike;
    }

    public boolean isShowLinkStatus() {
        return showLinkStatus;
    }

    public void setLinkDistance(int newLinkDistance) {
        newLinkDistance = Math.max(0, Math.min(newLinkDistance, maxLinkDistance.get()));
        if (newLinkDistance != this.linkDistance) {
            this.linkDistance = newLinkDistance;
            markForClientUpdate();
            setChanged();
        }
    }

    /**
     * 来自高版本移植
     */
    public void markForClientUpdate() {
        this.requestModelDataUpdate();

        if (this.level != null && !this.isRemoved() && !notLoaded()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public int getMaxLinkDistanceForClient() {
        return maxLinkDistanceForClient;
    }

    public List<BlockPos> getPendingRenderPositionsSnapshot() {
        return this.pendingLinkPositions.isEmpty() ? List.of() : List.copyOf(this.pendingLinkPositions);
    }

    public List<BlockPos> getLinkedRenderPositionsSnapshot() {
        return this.linkedPositions.isEmpty() ? List.of() : List.copyOf(this.linkedPositions);
    }

    public boolean shouldRenderLinkStatusForClient() {
        if (isShowLinkStatus()) {
            return true;
        }

        Boolean isHoldingLinker = DistExecutor.safeCallWhenOn(Dist.CLIENT,
                () -> EnderEmitterClientRenderHelper::shouldRenderLinkStatus);
        return Boolean.TRUE.equals(isHoldingLinker);
    }

    @Override
    public boolean enableCustomRenderBounding() {
        return shouldRenderLinkStatusForClient();
    }

    @Override
    public int getRange() {
        return this.maxLinkDistanceForClient * 2;
    }

    @Override
    public AABB getCustomBoundingBox(BlockPos centerPos) {
        if (enableCustomRenderBounding())
            return ICustomRenderBounding.super.getCustomBoundingBox(centerPos);
        else
            return new AABB(centerPos).inflate(0, 1, 0);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return this.getCustomBoundingBox(this.getBlockPos());
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    protected void onConfigChanged() {
        this.autoMode = configManager.getSetting(AECSSettings.AUTO_LINK_MODE) == AutoLinkMode.ENABLE;
        this.allowAutoLinkCableLike = configManager.getSetting(AECSSettings.AUTO_LINK_CABLE_MODE) == AutoLinkCableMode.ENABLE;
        this.showLinkStatus = configManager.getSetting(AECSSettings.SHOW_RANGE_MODE) == ShowRangeMode.SHOW_RANGE;
        this.saveChanges();
        this.markForClientUpdate();
    }

    @Override
    public void serverTick() {
        if (isRecentAddedPos()) return;
        if (level == null) return;
        IGridNode selfNode = getMainNode().getNode();
        if (selfNode == null) return;
        refreshClientDisplayState(selfNode);
        if (active != selfNode.isActive()) {
            active = selfNode.isActive();
            markForClientUpdate();
        }
        if (!active) return;
        if (!bandId.isEmpty()) {
            BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
            if (band != null && band.getErrorState() == BroadcastFrequencyBand.BandError.CHANNEL_OVERFLOW) {
                suspendEmitterLinksForBandOverflow();
                return;
            }
        }

        if (!bandId.isEmpty()) {
            int usedChannels = Math.max(0, selfNode.getUsedChannels());
            if (usedChannels != this.lastBandReceiverUsedChannels) {
                this.lastBandReceiverUsedChannels = usedChannels;
                BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
                if (band != null && band.getErrorState() != BroadcastFrequencyBand.BandError.CHANNEL_OVERFLOW) {
                    FrequencyBandManager.markRuntimeDirty(level.getServer(), bandId);
                }
            }
        } else {
            this.lastBandReceiverUsedChannels = 0;
        }

        if (selfNode.getUsedChannels() >= getMaxLinkChannels()) {
            // 当无可用频道时清除pending
            if (!pendingLinkPositions.isEmpty()) {
                pendingLinkPositions.clear();
                setChanged();
                markForClientUpdate();
            }
            return;
        }

        int pendingSize = pendingLinkPositions.size();
        for (Iterator<BlockPos> it = pendingLinkPositions.iterator(); it.hasNext();) {
            BlockPos targetPos = it.next();

            // 不处理未加载区块的情况
            if (!level.isLoaded(targetPos)) continue;

            // 如果目标位置已有旧链接，先摧毁
            List<IGridConnection> oldConnections = linkedConnections.remove(targetPos);
            if (oldConnections != null) {
                for (IGridConnection connection : oldConnections) {
                    if (connection != null) {
                        connection.destroy();
                    }
                }
            }

            // 尝试建立连接，这里targets会自动筛掉不符合条件的
            List<IGridNode> targetNodes = getConnectableNodes(level, targetPos);
            if (targetNodes.isEmpty()) {
                // 无任何可连接节点
                it.remove();
                continue;
            }

            ArrayList<IGridConnection> newConnections = new ArrayList<>();
            for (IGridNode targetNode : targetNodes) {
                IGrid targetGrid = targetNode.getGrid();
                IGrid selfGrid = selfNode.getGrid();
                if (targetGrid != null && selfGrid != null) {
                    if (targetGrid.getPathingService().getControllerState() != ControllerState.NO_CONTROLLER) {
                        if (targetGrid != selfGrid)
                            continue; // 不连接导致冲突的网络
                        else if (targetNode.meetsChannelRequirements())
                            continue; // 同一个网络内，若对方已经分配有频道，不连接
                    }
                }

                try {
                    newConnections.add(GridHelper.createConnection(selfNode, targetNode));
                } catch (IllegalStateException e) {
                    // 此错误说明两者之间已有连接，无需log记录
                }
            }

            if (newConnections.isEmpty()) {
                // 本轮没有完成任何连接
                it.remove();
                continue;
            }

            // 本轮有任何成功链接就结束，剩下的等待下一轮
            linkedConnections.put(targetPos, newConnections);
            it.remove();
            linkedPositions.add(targetPos);
            break;
        }

        if (pendingSize != pendingLinkPositions.size()) {
            setChanged();
            markForClientUpdate();
        }
    }

    private void refreshClientDisplayState(IGridNode selfNode) {
        int newUsedChannels = Math.max(0, selfNode.getUsedChannels());
        int newTotalChannels = Math.max(0, selfNode.getMaxChannels());
        long newBandUsedChannels = 0;
        long newBandTotalChannels = 0;

        if (!bandId.isEmpty()) {
            BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
            if (band != null) {
                newBandUsedChannels = band.getUsedChannels();
                newBandTotalChannels = band.getUsableChannels();
            }
        }

        if (this.usedChannelsForClient != newUsedChannels || this.totalChannelsForClient != newTotalChannels || this.bandUsedChannelsForClient != newBandUsedChannels || this.bandTotalChannelsForClient != newBandTotalChannels) {
            this.usedChannelsForClient = newUsedChannels;
            this.totalChannelsForClient = newTotalChannels;
            this.bandUsedChannelsForClient = newBandUsedChannels;
            this.bandTotalChannelsForClient = newBandTotalChannels;
            markForClientUpdate();
        }
    }

    private void suspendEmitterLinksForBandOverflow() {
        if (linkedConnections.isEmpty() && linkedPositions.isEmpty()) {
            return;
        }

        pendingLinkPositions.addAll(linkedPositions);
        for (Collection<IGridConnection> connections : linkedConnections.values()) {
            for (IGridConnection connection : connections) {
                if (connection != null) {
                    connection.destroy();
                }
            }
        }

        linkedConnections.clear();
        linkedPositions.clear();
        setChanged();
        markForClientUpdate();
    }

    @Override
    public void clientTick() {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    /**
     * 如果返回真，则说明最近添加了新pos，需要延后工作
     */
    private boolean isRecentAddedPos() {
        if (recentAddedPosCountdown > 0) {
            recentAddedPosCountdown--;
            return true;
        }
        return false;
    }

    private void addPosToPending(BlockPos pos) {
        this.pendingLinkPositions.add(pos);
        this.recentAddedPosCountdown = 2;
    }

    private void addListPosToPending(Collection<BlockPos> posList) {
        this.pendingLinkPositions.addAll(posList);
        this.recentAddedPosCountdown = 2;
    }

    public int getMaxLinkChannels() {
        IGridNode node = getMainNode().getNode();
        return node == null ? 0 : node.getMaxChannels();
    }

    public int getUsedLinkChannels() {
        IGridNode node = getMainNode().getNode();
        return node == null ? 0 : node.getUsedChannels();
    }

    @Override
    public int getMaxChannels() {
        return Math.max(0, customMaxChannels);
    }

    @Override
    public void setMaxChannels(int maxChannels) {
        this.customMaxChannels = Math.max(0, maxChannels);
    }

    @Override
    public boolean isEnabledCustomChannel() {
        return enabledCustomChannel;
    }

    @Override
    public void setEnabledCustomChannel(boolean enabled) {
        this.enabledCustomChannel = enabled;
    }

    public void connectToBand(String newBandId) {
        if (level == null || level.isClientSide()) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        BroadcastFrequencyBand newBand = FrequencyBandManager.getBand(newBandId);
        if (newBand == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), worldPosition);
        if (!bandId.isEmpty()) {
            BroadcastFrequencyBand oldBand = FrequencyBandManager.getBand(bandId);
            if (oldBand != null) {
                oldBand.onReceiverOffline(server, globalPos);
                if (oldBand != newBand) {
                    oldBand.undeclareReceiver(globalPos);
                }
            }
        }

        newBand.declareReceiver(globalPos);
        setEnabledCustomChannel(true);
        setMaxChannels(0);

        IGridNode node = getMainNode().getNode();
        if (node != null) {
            newBand.onReceiverOnline(server, globalPos, node, this);
        }

        this.bandId = newBand.getName();
        onGridConnectableSidesChanged();
        markForClientUpdate();
        setChanged();
    }

    @Override
    public void cleanConnectionPermanent() {
        if (level == null || level.isClientSide()) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        if (!bandId.isEmpty()) {
            BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
            if (band != null) {
                GlobalPos globalPos = GlobalPos.of(level.dimension(), worldPosition);
                band.onReceiverOffline(server, globalPos);
                band.undeclareReceiver(globalPos);
            }
        }

        bandId = "";
        setEnabledCustomChannel(false);
        setMaxChannels(0);
        lastBandReceiverUsedChannels = 0;
        onGridConnectableSidesChanged();
        markForClientUpdate();
        setChanged();
    }

    private void markBandRuntimeDirty() {
        if (level == null || level.isClientSide()) return;
        if (bandId.isEmpty()) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        FrequencyBandManager.clearOverflowState(bandId);
        FrequencyBandManager.markRuntimeDirty(server, bandId);
    }

    @Override
    public void onReady() {
        super.onReady();

        if (level != null && !level.isClientSide() && !bandId.isEmpty()) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
                IGridNode node = getMainNode().getNode();
                if (band != null && node != null) {
                    GlobalPos gp = GlobalPos.of(level.dimension(), worldPosition);
                    band.declareReceiver(gp);
                    setEnabledCustomChannel(true);
                    band.onReceiverOnline(server, gp, node, this);
                } else {
                    setEnabledCustomChannel(false);
                }
            }
        }

        // 节点准备好之后加入到缓存表
        if (level != null && !level.isClientSide()) {
            ChunkPos center = new ChunkPos(worldPosition);
            for (int offsetX = -autoAreaFactor; offsetX <= autoAreaFactor; offsetX++) {
                for (int offsetZ = -autoAreaFactor; offsetZ <= autoAreaFactor; offsetZ++) {
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
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide() && !bandId.isEmpty()) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
                if (band != null) {
                    band.onReceiverOffline(server, GlobalPos.of(level.dimension(), worldPosition));
                }
            }
        }
        super.onChunkUnloaded();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (reason == IGridNodeListener.State.CHANNEL || reason == IGridNodeListener.State.GRID_BOOT) {
            markBandRuntimeDirty();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // 方块被移除或区块卸载时从全局索引移除
        if (level != null && !level.isClientSide()) {
            ChunkPos center = new ChunkPos(worldPosition);
            BlockPos posKey = worldPosition.immutable();

            for (int offsetX = -autoAreaFactor; offsetX <= autoAreaFactor; offsetX++) {
                for (int offsetZ = -autoAreaFactor; offsetZ <= autoAreaFactor; offsetZ++) {
                    GlobalChunkPos chunkKey = new GlobalChunkPos(level.dimension(), center.x + offsetX, center.z + offsetZ);

                    Set<BlockPos> set = EMITTER_CHUNK_POSITIONS.get(chunkKey);
                    if (set != null) {
                        set.remove(posKey);
                        if (set.isEmpty()) {
                            EMITTER_CHUNK_POSITIONS.remove(chunkKey);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putString("band_id", bandId);
        data.putBoolean("enabled_custom_channel", enabledCustomChannel);
        data.putInt("custom_max_channels", customMaxChannels);
        configManager.writeToNBT(data);
        data.putInt("link_distance", this.linkDistance);

        ListTag linkPositions = new ListTag();
        for (BlockPos pos : linkedPositions) {
            try {
                Tag t = NbtUtils.writeBlockPos(pos);
                linkPositions.add(t);
            } catch (Throwable e) {
                // 静默
            }
        }

        data.put("linked_positions", linkPositions);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.bandId = data.getString("band_id");
        this.enabledCustomChannel = data.getBoolean("enabled_custom_channel");
        this.customMaxChannels = data.getInt("custom_max_channels");
        this.configManager.readFromNBT(data);
        this.linkDistance = data.getInt("link_distance");

        linkedPositions.clear();

        Tag root = data.get("linked_positions");
        if (!(root instanceof ListTag list)) return;

        for (Tag t : list) {
            try {
                BlockPos pos = NbtUtils.readBlockPos((CompoundTag) t);
                linkedPositions.add(pos);
            } catch (Throwable e) {
                // 忽略
            }
        }

        this.autoMode = configManager.getSetting(AECSSettings.AUTO_LINK_MODE) == AutoLinkMode.ENABLE;
        this.allowAutoLinkCableLike = configManager.getSetting(AECSSettings.AUTO_LINK_CABLE_MODE) == AutoLinkCableMode.ENABLE;
        this.showLinkStatus = configManager.getSetting(AECSSettings.SHOW_RANGE_MODE) == ShowRangeMode.SHOW_RANGE;
        onGridConnectableSidesChanged();
        this.markForClientUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onConfigChanged(); // 确保字段与配置始终同步
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable net.minecraft.world.entity.player.Player player) {
        super.importSettings(mode, input, player);
        if (mode == SettingsFrom.MEMORY_CARD) {
            if (input.contains("memory_card_link_distance", Tag.TAG_INT)) {
                setLinkDistance(input.getInt("memory_card_link_distance"));
            }

            if (input.contains("memory_card_band_info", Tag.TAG_COMPOUND)) {
                MemoryCardBandInfo targetLink = MemoryCardBandInfo.readFromNBT(input.getCompound("memory_card_band_info"));
                if (targetLink == null || targetLink.asSender()) return;

                BroadcastFrequencyBand band = FrequencyBandManager.getBand(targetLink.bandName());
                if (band == null || !band.isAllowedMemoryCardCopy()) return;

                cleanConnectionPermanent();
                connectToBand(targetLink.bandName());
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag builder, @Nullable net.minecraft.world.entity.player.Player player) {
        super.exportSettings(mode, builder, player);
        if (mode == SettingsFrom.MEMORY_CARD) {
            builder.putInt("memory_card_link_distance", this.linkDistance);

            if (!bandId.isEmpty()) {
                BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
                if (band == null || !band.isAllowedMemoryCardCopy()) return;

                builder.put("memory_card_band_info", MemoryCardBandInfo.writeToNBT(new MemoryCardBandInfo(bandId, false)));
            }
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(AECSBlocks.ENDER_EMITTER_BLOCK.get());
    }

    // 把重要信息和pengding和linked写表，用于客户端显示
    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        BroadcastFrequencyBand band = bandId.isEmpty() ? null : FrequencyBandManager.getBand(bandId);
        data.writeBoolean(this.autoMode);
        data.writeBoolean(this.active);
        data.writeBoolean(this.showLinkStatus);
        data.writeInt(this.maxLinkDistanceForClient);
        data.writeInt(this.linkDistance);
        data.writeUtf(this.bandId);
        data.writeInt(getUsedLinkChannels());
        data.writeInt(getMaxLinkChannels());
        data.writeLong(band == null ? 0 : band.getUsedChannels());
        data.writeLong(band == null ? 0 : band.getUsableChannels());
        data.writeInt(this.pendingLinkPositions.size());
        for (BlockPos pos : this.pendingLinkPositions) {
            data.writeBlockPos(pos);
        }
        data.writeInt(this.linkedPositions.size());
        for (BlockPos pos : this.linkedPositions) {
            data.writeBlockPos(pos);
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        super.readFromStream(data);
        this.autoMode = data.readBoolean();
        this.active = data.readBoolean();
        this.showLinkStatus = data.readBoolean();
        this.maxLinkDistanceForClient = data.readInt();
        this.linkDistance = data.readInt();
        this.bandId = data.readUtf();
        this.usedChannelsForClient = data.readInt();
        this.totalChannelsForClient = data.readInt();
        this.bandUsedChannelsForClient = data.readLong();
        this.bandTotalChannelsForClient = data.readLong();
        this.pendingLinkPositions.clear();
        int pendingSize = data.readInt();
        for (int i = 0; i < pendingSize; i++) {
            BlockPos pos = data.readBlockPos();
            this.pendingLinkPositions.add(pos);
        }
        this.linkedPositions.clear();
        int linkSize = data.readInt();
        for (int i = 0; i < linkSize; i++) {
            BlockPos pos = data.readBlockPos();
            this.linkedPositions.add(pos);
        }
        return true;
    }

    /**
     * 获取目标位置中最适合被无线连接的一个或一群节点，其内部元素亦不为null
     */
    public static List<IGridNode> getConnectableNodes(Level level, BlockPos pos) {
        ArrayList<IGridNode> nodes = new ArrayList<>();

        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(level, pos);
        if (nodeHost == null) return nodes;

        // 无线连接拒绝控制器
        if (nodeHost instanceof ControllerBlockEntity) return nodes;
        else if (nodeHost instanceof CableBusBlockEntity cable) {
            // 线缆节点，优先获取中间节点，如不存在则将六个面的节点都加入
            CableBusContainer cableBus = cable.getCableBus();
            IPart center = cableBus.getPart(null);
            if (center != null) {
                nodes.add(center.getGridNode());
            } else {
                for (Direction direction : Direction.values()) {
                    IPart part = cableBus.getPart(direction);
                    if (part != null) {
                        nodes.add(part.getGridNode());
                    }
                }
            }
        } else {
            // 普通节点，任意一面取得即可
            for (Direction direction : Direction.values()) {
                IGridNode node = nodeHost.getGridNode(direction);
                if (node != null) {
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
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevelAccessor sla) {
            addPosToRecentEmitter(sla.getLevel(), event.getPos());
        }

        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
            if (be != null) {
                ResonatingMemoryCardHelper.tryApplyToBlockEntity(player, be);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor targetLevelAccessor = event.getLevel();
        if (targetLevelAccessor instanceof ServerLevelAccessor sla) {
            removePosFromRecentEmitter(sla.getLevel(), event.getPos());
        }
    }

    /**
     * 将需要连接的位置添加到指定发信器
     */
    public static boolean addPosToEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos pos, boolean byManual, boolean forceAuto) {
        if (emitter.getBlockPos().equals(pos)) return false;

        // 如果是手动连接，仅检查手动方法
        boolean valid = byManual && VecHelper.closerThanChebyshev(emitter.worldPosition, pos, maxLinkDistance.get());

        // 自动连接额外检查
        if (!valid) {
            if (emitter.level == null) return false;
            IGridNode emitterNode = emitter.getMainNode().getNode();
            if (emitterNode == null || !emitterNode.isActive()) return false;
            IInWorldGridNodeHost targetNodeHost = GridHelper.getNodeHost(emitter.level, pos);
            valid = (forceAuto || emitter.isAutoMode()) && emitterNode.getUsedChannels() < emitter.getMaxLinkChannels() && (!(targetNodeHost instanceof CableBusBlockEntity) || emitter.allowAutoLinkCableLike()) && VecHelper.closerThanChebyshev(emitter.worldPosition, pos, emitter.linkDistance);
        }

        if (valid) {
            emitter.addPosToPending(pos);
            emitter.markForClientUpdate();
            emitter.setChanged();
            return true;
        }
        return false;
    }

    /**
     * 从发信器中移除某个特定的位置
     */
    public static void removePosFromEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos targetPos) {
        emitter.pendingLinkPositions.remove(targetPos);
        emitter.linkedPositions.remove(targetPos);
        var connections = emitter.linkedConnections.remove(targetPos);
        if (connections != null && !connections.isEmpty()) {
            for (IGridConnection connection : connections) {
                if (connection != null)
                    connection.destroy();
            }
        }
        emitter.setChanged();
        emitter.markForClientUpdate();
    }

    /**
     * 自动将当前位置添加到最近一个可用发信器
     */
    public static void addPosToRecentEmitter(@NotNull Level targetLevel, @NotNull BlockPos targetPos) {
        // 确认目标方块状态
        IInWorldGridNodeHost targetNodeHost = GridHelper.getNodeHost(targetLevel, targetPos);
        if (targetNodeHost == null) return;

        // 确认最佳发信器状态
        GlobalChunkPos targetChunkPos = new GlobalChunkPos(targetLevel.dimension(), targetPos);
        Set<BlockPos> linkPositions = EMITTER_CHUNK_POSITIONS.get(targetChunkPos);
        if (linkPositions == null || linkPositions.isEmpty()) return;

        List<BlockPos> availablePositions = new ArrayList<>(linkPositions.size());
        for (BlockPos linkPos : linkPositions) {
            if (VecHelper.closerThanChebyshev(linkPos, targetPos, maxLinkDistance.get())) {
                availablePositions.add(linkPos);
            }
        }
        if (availablePositions.isEmpty()) return;

        // 按欧式距离排序优先级
        availablePositions.sort(Comparator.comparingDouble(pos -> pos.distSqr(targetPos)));

        // 迭代寻找可用发信器并与之连接
        for (BlockPos linkPos : availablePositions) {
            if (targetLevel.getBlockEntity(linkPos) instanceof EnderEmitterBlockEntity emitter) {
                // 打开自动模式、检查emitter的独特距离，最后再尝试加入到emitter
                if (addPosToEmitter(emitter, targetPos, false, false)) {
                    return;
                }
            }
        }
    }

    /**
     * 将指定位置信息从附近的发信器索引中移除
     */
    public static void removePosFromRecentEmitter(@NotNull Level targetLevel, @NotNull BlockPos targetPos) {
        IInWorldGridNodeHost targetNodeHost = GridHelper.getNodeHost(targetLevel, targetPos);
        if (targetNodeHost == null) return;

        // 移除相关发信器中的连接状态
        GlobalChunkPos targetChunkPos = new GlobalChunkPos(targetLevel.dimension(), targetPos);
        Set<BlockPos> linkPositions = EMITTER_CHUNK_POSITIONS.get(targetChunkPos);
        if (linkPositions == null || linkPositions.isEmpty()) return;
        for (BlockPos linkPos : linkPositions) {
            if (targetLevel.getBlockEntity(linkPos) instanceof EnderEmitterBlockEntity emitter) {
                removePosFromEmitter(emitter, targetPos);
            }
        }
    }

    /**
     * 用来主动将附近区块的be全部添加进表
     */
    public static void addAllRecentBEtoEmitter(@NotNull EnderEmitterBlockEntity emitter) {
        Level targetLevel = emitter.level;
        if (targetLevel instanceof ServerLevel serverLevel) {
            ChunkPos centerChunk = new ChunkPos(emitter.worldPosition);
            List<BlockEntity> blockEntities = ChunkHelper.getBlockEntitiesInChunks(serverLevel, centerChunk, autoAreaFactor);
            for (BlockEntity be : blockEntities) {
                addPosToEmitter(emitter, be.getBlockPos(), false, true);
            }
        }
    }

    /**
     * 手动显式清除所有连接
     */
    public static void removeAllLinkedFromEmitter(@NotNull EnderEmitterBlockEntity emitter) {
        for (Collection<IGridConnection> connections : emitter.linkedConnections.values()) {
            for (IGridConnection connection : connections) {
                connection.destroy();
            }
        }
        emitter.linkedConnections.clear();
        emitter.linkedPositions.clear();
        emitter.pendingLinkPositions.clear();
        emitter.setChanged();
        emitter.markForClientUpdate();
    }

    // 用来确定缓存表可用性-----------------------------------------------------
    public static void ensureBound(@Nullable MinecraftServer server) {
        if (server == null) {
            EMITTER_CHUNK_POSITIONS.clear();
            boundServer = null;
            return;
        }
        if (boundServer != server) {
            EMITTER_CHUNK_POSITIONS.clear();
            boundServer = server;
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent e) {
        ensureBound(e.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent e) {
        EMITTER_CHUNK_POSITIONS.clear();
        boundServer = null;
    }
}
