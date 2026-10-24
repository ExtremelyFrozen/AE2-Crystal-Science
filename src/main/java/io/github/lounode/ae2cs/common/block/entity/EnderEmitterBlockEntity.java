package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastFrequencyBand;
import io.github.lounode.ae2cs.api.linker.broadcast.BroadcastReceiverHost;
import io.github.lounode.ae2cs.api.linker.broadcast.FrequencyBandManager;
import io.github.lounode.ae2cs.api.render.ICustomRenderBounding;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.AutoLinkCableMode;
import io.github.lounode.ae2cs.api.settings.AutoLinkMode;
import io.github.lounode.ae2cs.api.settings.ShowRangeMode;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.api.util.AECSGridHelper;
import io.github.lounode.ae2cs.api.util.GlobalChunkPos;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.inventory.EnderEmitterUpgradeInventory;
import io.github.lounode.ae2cs.util.ChunkHelper;

import appeng.api.AECapabilities;
import appeng.api.networking.*;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPart;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.parts.CableBusContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EventBusSubscriber(modid = AECSConstants.MODID)
public class EnderEmitterBlockEntity extends AENetworkedBlockEntity implements ServerTickingBlockEntity,
                                     IUpgradeableObject, ClientTickingBlockEntity, IConfigurableObject, ICustomRenderBounding,
                                     CustomChannelProviderHost, BroadcastReceiverHost, CustomReturnableSubMenuHost {

    /** Index every chunk in an emitter's maximum upgraded range, without loading chunks. */
    public static final Map<GlobalChunkPos, Set<BlockPos>> EMITTER_CHUNK_POSITIONS = new HashMap<>();
    public static MinecraftServer boundServer;

    private static final IGridNodeListener<EnderEmitterBlockEntity> INPUT_LISTENER = new IGridNodeListener<>() {

        @Override
        public void onSaveChanges(EnderEmitterBlockEntity host, IGridNode node) {
            host.saveChanges();
        }

        @Override
        public void onStateChanged(EnderEmitterBlockEntity host, IGridNode node, State state) {
            host.markForClientUpdate();
        }
    };

    private final IConfigManager configManager;
    private final IUpgradeInventory upgrades = new EnderEmitterUpgradeInventory(this::onUpgradesChanged);
    /**
     * Each base face has its own input route. Joining these inputs to a single node would
     * make AE2 select only one controller route, limiting the whole emitter to one cable.
     */
    private final Map<Direction, IManagedGridNode> sideInputs = new EnumMap<>(Direction.class);
    private final Set<BlockPos> pendingLinkPositions = new LinkedHashSet<>();
    private final Set<BlockPos> linkedPositions = new LinkedHashSet<>();
    private final Set<BlockPos> manualLinkPositions = new HashSet<>();
    private final Map<BlockPos, List<IGridConnection>> linkedConnections = new HashMap<>();

    // Distances are chunk radii: 0/1/2/3 correspond to 1x1/3x3/5x5/7x7.
    private int linkDistance = 1;
    private int maxLinkDistanceForClient = 1;
    private int indexedChunkRadius = -1;
    private int recentAddedPosCountdown;
    private int retryCountdown;
    private boolean needsScan;
    private boolean active;
    private boolean autoMode = true;
    private boolean allowAutoLinkCableLike = true;
    private boolean showLinkStatus;
    private String bandId = "";
    private int expectedChannels = 32;
    private int customMaxChannels;
    private boolean enabledCustomChannel;
    private int bandUsedChannelsForClient;
    private int bandTotalChannelsForClient;

    public EnderEmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            sideInputs.put(side, GridHelper.createManagedNode(this, INPUT_LISTENER)
                    .setFlags(GridFlags.DENSE_CAPACITY)
                    .setInWorldNode(true)
                    .setExposedOnSides(EnumSet.of(side))
                    .setIdlePowerUsage(0)
                    .setVisualRepresentation(AECSBlocks.ENDER_EMITTER_BLOCK)
                    .setTagName("input_" + side.getSerializedName()));
        }
        configManager = IConfigManager.builder(this::onConfigChanged)
                .registerSetting(AECSSettings.AUTO_LINK_MODE, AutoLinkMode.ENABLE)
                .registerSetting(AECSSettings.AUTO_LINK_CABLE_MODE, AutoLinkCableMode.ENABLE)
                .registerSetting(AECSSettings.SHOW_RANGE_MODE, ShowRangeMode.HIDE_RANGE)
                .build();
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

    public int getMaxLinkDistance() {
        return 1 + upgrades.getInstalledUpgrades(AEItems.WIRELESS_BOOSTER) / 32;
    }

    public int getMaxLinkDistanceForClient() {
        return level != null && level.isClientSide() ? maxLinkDistanceForClient : getMaxLinkDistance();
    }

    public boolean allowAutoLinkCableLike() {
        return allowAutoLinkCableLike;
    }

    public boolean isShowLinkStatus() {
        return showLinkStatus;
    }

    public boolean isConnectedToBand() {
        return bandId != null && !bandId.isEmpty();
    }

    public String getBandName() {
        return bandId == null ? "" : bandId;
    }

    public int getBandUsedChannelsForClient() {
        return bandUsedChannelsForClient;
    }

    public int getBandTotalChannelsForClient() {
        return bandTotalChannelsForClient;
    }

    public int getUsedChannelsForClient() {
        return getUsedLinkChannels();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void setOwner(Player player) {
        getMainNode().setOwningPlayer(player);
        sideInputs.values().forEach(input -> input.setOwningPlayer(player));
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return isConnectedToBand() ? EnumSet.noneOf(Direction.class) : EnumSet.of(Direction.DOWN);
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction side) {
        if (isConnectedToBand() || side == Direction.UP) return null;
        IManagedGridNode input = side == Direction.DOWN ? getMainNode() : sideInputs.get(side);
        return input == null ? null : input.getNode();
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        for (IGridNode node : getInputNodes()) {
            if (node.isActive()) return node;
        }
        return getMainNode().getNode();
    }

    private void updateInputSides() {
        onGridConnectableSidesChanged();
        sideInputs.forEach((side, input) -> input.setExposedOnSides(
                isConnectedToBand() ? EnumSet.noneOf(Direction.class) : EnumSet.of(side)));
    }

    private List<IGridNode> getInputNodes() {
        List<IGridNode> nodes = new ArrayList<>(5);
        IGridNode main = getMainNode().getNode();
        if (main != null && (isConnectedToBand() || hasInputConnection(main))) nodes.add(main);
        if (!isConnectedToBand()) {
            for (IManagedGridNode input : sideInputs.values()) {
                IGridNode node = input.getNode();
                if (node != null && hasInputConnection(node)) nodes.add(node);
            }
        }
        return nodes;
    }

    private boolean hasInputConnection(IGridNode node) {
        for (IGridConnection connection : node.getConnections()) {
            if (connection.isInWorld() || linkedConnections.values().stream().noneMatch(links -> links.contains(connection))) return true;
        }
        return false;
    }

    private int getInputCapacity(IGridNode node) {
        if (isConnectedToBand()) return getMaxChannels();
        if (!node.hasGridBooted()) return Math.max(0, node.getUsedChannels());
        if (node.getGrid().getPathingService().getControllerState() == ControllerState.NO_CONTROLLER) {
            ChannelMode mode = node.getGrid().getPathingService().getChannelMode();
            return mode == ChannelMode.INFINITE ? Integer.MAX_VALUE : 8 * mode.getCableCapacityFactor();
        }
        return clampChannelCount((long) node.getUsedChannels() + AECSGridHelper.getDistributableChannelsOnControllerPath(node));
    }

    public int getMaxLinkChannels() {
        if (isConnectedToBand()) return getMaxChannels();
        long channels = 0;
        for (IGridNode node : getInputNodes()) channels += getInputCapacity(node);
        return clampChannelCount(channels);
    }

    public int getDisplayedMaxLinkChannels() {
        return getMaxLinkChannels();
    }

    public int getUsedLinkChannels() {
        long used = 0;
        for (IGridNode node : getInputNodes()) used += node.getUsedChannels();
        return clampChannelCount(used);
    }

    @Override
    public int getMaxChannels() {
        return Math.max(0, customMaxChannels);
    }

    @Override
    public void setMaxChannels(int channels) {
        customMaxChannels = Math.max(0, channels);
    }

    @Override
    public boolean isEnabledCustomChannel() {
        // A disconnected or exhausted band must stay at zero instead of falling back to 32.
        return isConnectedToBand() || enabledCustomChannel;
    }

    @Override
    public void setEnabledCustomChannel(boolean enabled) {
        enabledCustomChannel = enabled;
        if (!enabled) customMaxChannels = 0;
    }

    @Override
    public int getExpectedChannels() {
        return expectedChannels;
    }

    public void changeExpectedChannels(int delta) {
        expectedChannels = clampChannelCount((long) expectedChannels + delta);
        markBandRuntimeDirty();
        setChanged();
    }

    private static int clampChannelCount(long channels) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, channels));
    }

    public void connectToBand(String name) {
        if (level == null || level.isClientSide() || level.getServer() == null) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(name);
        if (band == null || name.equals(bandId)) return;
        disconnectBand(false);
        queueExistingConnections();
        bandId = band.getName();
        updateInputSides();
        setEnabledCustomChannel(true);
        setMaxChannels(0);
        GlobalPos pos = GlobalPos.of(level.dimension(), worldPosition);
        band.declareReceiver(pos);
        IGridNode node = getMainNode().getNode();
        if (node != null) band.onReceiverOnline(level.getServer(), pos, node, this);
        setChanged();
        markForClientUpdate();
    }

    private void disconnectBand(boolean keepDeclaration) {
        if (level == null || level.isClientSide() || bandId.isEmpty() || level.getServer() == null) return;
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        if (band != null) {
            GlobalPos pos = GlobalPos.of(level.dimension(), worldPosition);
            band.onReceiverOffline(level.getServer(), pos);
            if (!keepDeclaration) band.undeclareReceiver(pos);
        }
    }

    @Override
    public void cleanConnectionPermanent() {
        if (level == null || level.isClientSide()) return;
        disconnectBand(false);
        queueExistingConnections();
        bandId = "";
        setEnabledCustomChannel(false);
        updateInputSides();
        setChanged();
        markForClientUpdate();
    }

    private void queueExistingConnections() {
        pendingLinkPositions.addAll(linkedPositions);
        for (List<IGridConnection> connections : linkedConnections.values()) {
            connections.forEach(IGridConnection::destroy);
        }
        linkedConnections.clear();
        linkedPositions.clear();
    }

    public void setLinkDistance(int radius) {
        int next = Math.max(0, Math.min(radius, getMaxLinkDistance()));
        if (next == linkDistance) return;
        linkDistance = next;
        pruneOutOfRangeConnections();
        needsScan = true;
        setChanged();
        markForClientUpdate();
    }

    private void onUpgradesChanged() {
        if (level == null || level.isClientSide()) return;
        linkDistance = Math.min(linkDistance, getMaxLinkDistance());
        refreshChunkIndex();
        pruneOutOfRangeConnections();
        needsScan = true;
        setChanged();
        markForClientUpdate();
    }

    private boolean isInRange(BlockPos pos, int radius) {
        ChunkPos center = new ChunkPos(worldPosition);
        ChunkPos target = new ChunkPos(pos);
        return Math.abs((long) target.x - center.x) <= radius && Math.abs((long) target.z - center.z) <= radius;
    }

    public AABB getChunkRangeBounds(int radius) {
        ChunkPos center = new ChunkPos(worldPosition);
        int minY = level == null ? worldPosition.getY() - 64 : level.getMinBuildHeight();
        int maxY = level == null ? worldPosition.getY() + 64 : level.getMaxBuildHeight();
        return new AABB((center.x - radius) * 16, minY, (center.z - radius) * 16,
                (center.x + radius + 1) * 16, maxY, (center.z + radius + 1) * 16);
    }

    private void pruneOutOfRangeConnections() {
        Set<BlockPos> positions = new HashSet<>(linkedPositions);
        positions.addAll(pendingLinkPositions);
        for (BlockPos pos : positions) {
            int radius = manualLinkPositions.contains(pos) ? getMaxLinkDistance() : linkDistance;
            if (!isInRange(pos, radius) || (!manualLinkPositions.contains(pos) && level != null && level.isLoaded(pos) && isAutoLinkExcluded(level, pos))) removePosFromEmitter(this, pos);
        }
    }

    protected void onConfigChanged() {
        boolean wasAutoMode = autoMode;
        autoMode = configManager.getSetting(AECSSettings.AUTO_LINK_MODE) == AutoLinkMode.ENABLE;
        allowAutoLinkCableLike = configManager.getSetting(AECSSettings.AUTO_LINK_CABLE_MODE) == AutoLinkCableMode.ENABLE;
        showLinkStatus = configManager.getSetting(AECSSettings.SHOW_RANGE_MODE) == ShowRangeMode.SHOW_RANGE;
        if (autoMode && !wasAutoMode) needsScan = true;
        saveChanges();
        markForClientUpdate();
    }

    @Override
    public void serverTick() {
        if (level == null) return;
        if (needsScan && autoMode) {
            needsScan = false;
            addAllRecentBEtoEmitter(this);
        }
        refreshClientDisplayState();
        List<IGridNode> sources = getInputNodes();
        boolean lostInput = false;
        for (BlockPos pos : List.copyOf(linkedPositions)) {
            List<IGridConnection> links = linkedConnections.get(pos);
            if (links == null) continue;
            boolean missingSource = links.stream().anyMatch(connection -> {
                IGridNode source = connection.a().getOwner() == this ? connection.a() : connection.b();
                return !sources.contains(source);
            });
            if (missingSource) {
                linkedConnections.remove(pos);
                links.forEach(IGridConnection::destroy);
                linkedPositions.remove(pos);
                pendingLinkPositions.add(pos);
                lostInput = true;
            }
        }
        if (lostInput) {
            setChanged();
            markForClientUpdate();
        }
        boolean nextActive = sources.stream().anyMatch(IGridNode::isActive) && (!isConnectedToBand() || getMaxChannels() > 0);
        if (active != nextActive) {
            active = nextActive;
            markForClientUpdate();
        }
        if (recentAddedPosCountdown > 0) {
            recentAddedPosCountdown--;
            return;
        }
        if (!active) return;
        sources.removeIf(node -> !node.isActive() || getInputCapacity(node) <= node.getUsedChannels());
        sources.sort(Comparator.comparingInt((IGridNode node) -> getInputCapacity(node) - node.getUsedChannels()).reversed());
        if (sources.isEmpty()) return;

        // A removed input can leave an existing output unserved. Retry it through the remaining faces.
        if (--retryCountdown <= 0) {
            retryCountdown = 20;
            for (BlockPos pos : List.copyOf(linkedPositions)) {
                if (!level.isLoaded(pos)) continue;
                boolean unserved = getConnectableNodes(level, pos).stream()
                        .anyMatch(node -> node.hasFlag(GridFlags.REQUIRE_CHANNEL) && node.hasGridBooted() && !node.meetsChannelRequirements());
                if (unserved) {
                    List<IGridConnection> old = linkedConnections.remove(pos);
                    if (old != null) old.forEach(IGridConnection::destroy);
                    linkedPositions.remove(pos);
                    pendingLinkPositions.add(pos);
                    break;
                }
            }
        }

        int pendingSize = pendingLinkPositions.size();
        for (Iterator<BlockPos> iterator = pendingLinkPositions.iterator(); iterator.hasNext();) {
            BlockPos targetPos = iterator.next();
            if (!level.isLoaded(targetPos)) continue;
            List<IGridNode> targets = getConnectableNodes(level, targetPos);
            if (targets.isEmpty()) {
                iterator.remove();
                manualLinkPositions.remove(targetPos);
                continue;
            }
            List<IGridConnection> connections = new ArrayList<>();
            for (IGridNode target : targets) {
                int previousSize = connections.size();
                for (IGridNode source : sources) {
                    IGrid targetGrid = target.getGrid();
                    IGrid sourceGrid = source.getGrid();
                    if (targetGrid.getPathingService().getControllerState() != ControllerState.NO_CONTROLLER) {
                        if (targetGrid != sourceGrid || target.meetsChannelRequirements()) continue;
                    }
                    if (source == target) continue;
                    try {
                        connections.add(GridHelper.createConnection(source, target));
                    } catch (IllegalStateException ignored) {
                        // An adjacent physical connection or another part already provides this route.
                    }
                    if (connections.size() > previousSize) break;
                }
            }
            iterator.remove();
            if (!connections.isEmpty()) {
                linkedConnections.put(targetPos, connections);
                linkedPositions.add(targetPos);
                break; // Let AE2 finish routing this target before adding another one.
            }
        }
        if (pendingSize != pendingLinkPositions.size()) {
            markBandRuntimeDirty();
            setChanged();
            markForClientUpdate();
        }
    }

    @Override
    public void clientTick() {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    private void refreshClientDisplayState() {
        BroadcastFrequencyBand band = bandId.isEmpty() ? null : FrequencyBandManager.getBand(bandId);
        int used = band == null ? 0 : clampChannelCount(band.getUsedChannels());
        int total = band == null ? 0 : clampChannelCount(band.getUsableChannels());
        if (used != bandUsedChannelsForClient || total != bandTotalChannelsForClient) {
            bandUsedChannelsForClient = used;
            bandTotalChannelsForClient = total;
            markForClientUpdate();
        }
    }

    private void markBandRuntimeDirty() {
        if (level != null && !level.isClientSide() && !bandId.isEmpty() && level.getServer() != null) {
            FrequencyBandManager.markRuntimeDirty(level.getServer(), bandId);
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        markForClientUpdate();
    }

    @Override
    public void onReady() {
        updateInputSides();
        super.onReady();
        if (level == null || level.isClientSide()) return;
        for (IManagedGridNode input : sideInputs.values()) {
            input.setOwningPlayerId(getMainNode().getNode().getOwningPlayerId());
            input.create(level, worldPosition);
        }
        refreshChunkIndex();
        pendingLinkPositions.addAll(linkedPositions);
        linkedPositions.clear();
        linkedConnections.clear();
        pruneOutOfRangeConnections();
        needsScan = false;
        if (bandId.isEmpty() || level.getServer() == null) return;
        setMaxChannels(0); // Always request a fresh allocation after loading.
        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        if (band != null) {
            GlobalPos pos = GlobalPos.of(level.dimension(), worldPosition);
            band.declareReceiver(pos);
            band.onReceiverOnline(level.getServer(), pos, getMainNode().getNode(), this);
        }
    }

    private void refreshChunkIndex() {
        if (level == null || level.isClientSide() || !getMainNode().isReady()) return;
        int radius = getMaxLinkDistance();
        if (radius == indexedChunkRadius) return;
        removeChunkIndex();
        ChunkPos center = new ChunkPos(worldPosition);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                GlobalChunkPos key = new GlobalChunkPos(level.dimension(), center.x + x, center.z + z);
                EMITTER_CHUNK_POSITIONS.computeIfAbsent(key, ignored -> new HashSet<>()).add(worldPosition.immutable());
            }
        }
        indexedChunkRadius = radius;
    }

    private void removeChunkIndex() {
        if (level == null || indexedChunkRadius < 0) return;
        ChunkPos center = new ChunkPos(worldPosition);
        for (int x = -indexedChunkRadius; x <= indexedChunkRadius; x++) {
            for (int z = -indexedChunkRadius; z <= indexedChunkRadius; z++) {
                GlobalChunkPos key = new GlobalChunkPos(level.dimension(), center.x + x, center.z + z);
                Set<BlockPos> positions = EMITTER_CHUNK_POSITIONS.get(key);
                if (positions != null) {
                    positions.remove(worldPosition);
                    if (positions.isEmpty()) EMITTER_CHUNK_POSITIONS.remove(key);
                }
            }
        }
        indexedChunkRadius = -1;
    }

    private void unloadInputs() {
        disconnectBand(true);
        removeChunkIndex();
        sideInputs.values().forEach(IManagedGridNode::destroy);
    }

    @Override
    public void onChunkUnloaded() {
        unloadInputs();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        unloadInputs();
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        sideInputs.values().forEach(input -> input.saveToNBT(data));
        configManager.writeToNBT(data, registries);
        upgrades.writeToNBT(data, "emitter_upgrades", registries);
        data.putInt("chunk_radius", linkDistance);
        data.putString("band_id", bandId);
        data.putInt("expected_channels", expectedChannels);
        Set<BlockPos> positions = new LinkedHashSet<>(linkedPositions);
        positions.addAll(pendingLinkPositions);
        data.put("linked_positions", writePositions(positions));
        data.put("manual_link_positions", writePositions(manualLinkPositions));
    }

    private static ListTag writePositions(Collection<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().ifPresent(list::add);
        return list;
    }

    private static void readPositions(CompoundTag data, String key, Set<BlockPos> positions) {
        positions.clear();
        for (Tag tag : data.getList(key, Tag.TAG_INT_ARRAY)) {
            BlockPos.CODEC.parse(NbtOps.INSTANCE, tag).result().ifPresent(pos -> positions.add(pos.immutable()));
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        sideInputs.values().forEach(input -> input.loadFromNBT(data));
        configManager.readFromNBT(data, registries);
        upgrades.readFromNBT(data, "emitter_upgrades", registries);
        linkDistance = data.contains("chunk_radius") ? Math.max(0, Math.min(getMaxLinkDistance(), data.getInt("chunk_radius"))) : 1;
        bandId = data.getString("band_id");
        expectedChannels = data.contains("expected_channels") ? Math.max(0, data.getInt("expected_channels")) : 32;
        customMaxChannels = 0;
        enabledCustomChannel = false;
        readPositions(data, "linked_positions", linkedPositions);
        readPositions(data, "manual_link_positions", manualLinkPositions);
        autoMode = configManager.getSetting(AECSSettings.AUTO_LINK_MODE) == AutoLinkMode.ENABLE;
        allowAutoLinkCableLike = configManager.getSetting(AECSSettings.AUTO_LINK_CABLE_MODE) == AutoLinkCableMode.ENABLE;
        showLinkStatus = configManager.getSetting(AECSSettings.SHOW_RANGE_MODE) == ShowRangeMode.SHOW_RANGE;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : upgrades) if (!stack.isEmpty()) drops.add(stack.copy());
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(autoMode);
        data.writeBoolean(active);
        data.writeBoolean(showLinkStatus);
        data.writeUtf(bandId);
        data.writeInt(bandUsedChannelsForClient);
        data.writeInt(bandTotalChannelsForClient);
        data.writeInt(getMaxLinkDistance());
        data.writeInt(linkDistance);
        data.writeInt(pendingLinkPositions.size());
        for (BlockPos pos : pendingLinkPositions) BlockPos.STREAM_CODEC.encode(data, pos);
        data.writeInt(linkedPositions.size());
        for (BlockPos pos : linkedPositions) BlockPos.STREAM_CODEC.encode(data, pos);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        super.readFromStream(data);
        autoMode = data.readBoolean();
        active = data.readBoolean();
        showLinkStatus = data.readBoolean();
        bandId = data.readUtf();
        bandUsedChannelsForClient = data.readInt();
        bandTotalChannelsForClient = data.readInt();
        maxLinkDistanceForClient = data.readInt();
        linkDistance = data.readInt();
        pendingLinkPositions.clear();
        for (int count = data.readInt(); count > 0; count--) pendingLinkPositions.add(BlockPos.STREAM_CODEC.decode(data));
        linkedPositions.clear();
        for (int count = data.readInt(); count > 0; count--) linkedPositions.add(BlockPos.STREAM_CODEC.decode(data));
        return true;
    }

    public List<BlockPos> getPendingRenderPositionsSnapshot() {
        return List.copyOf(pendingLinkPositions);
    }

    public List<BlockPos> getLinkedRenderPositionsSnapshot() {
        return List.copyOf(linkedPositions);
    }

    @Override
    public boolean enableCustomRenderBounding() {
        return showLinkStatus;
    }

    @Override
    public int getRange() {
        return (getMaxLinkDistanceForClient() + 1) * 16;
    }

    @Override
    public AABB getCustomBoundingBox(BlockPos centerPos) {
        return showLinkStatus ? getChunkRangeBounds(getMaxLinkDistanceForClient()) : new AABB(centerPos).inflate(0, 1, 0);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AECSBlocks.ENDER_EMITTER_BLOCK.toStack();
    }

    public static List<IGridNode> getConnectableNodes(Level level, BlockPos pos) {
        List<IGridNode> nodes = new ArrayList<>();
        IInWorldGridNodeHost host = level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, pos);
        if (host == null || host instanceof ControllerBlockEntity) return nodes;
        if (host instanceof CableBusBlockEntity cable) {
            CableBusContainer bus = cable.getCableBus();
            IPart center = bus.getPart(null);
            if (center != null) {
                nodes.add(center.getGridNode());
            } else {
                for (Direction side : Direction.values()) {
                    IPart part = bus.getPart(side);
                    if (part != null) nodes.add(part.getGridNode());
                }
            }
        } else {
            for (Direction side : Direction.values()) {
                IGridNode node = host.getGridNode(side);
                if (node != null) {
                    nodes.add(node);
                    break;
                }
            }
        }
        nodes.removeIf(Objects::isNull);
        return nodes;
    }

    private static boolean isAutoLinkExcluded(Level level, BlockPos pos) {
        BlockEntity target = level.getBlockEntity(pos);
        if (target instanceof ControllerBlockEntity || target instanceof EnderEmitterBlockEntity || target instanceof EnderBroadcasterBlockEntity) return true;
        String path = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
        return path.equals("controller") || path.endsWith("_controller") || path.endsWith("wireless_receiver") || path.endsWith("wireless_transceiver") || path.endsWith("wireless_connect") || path.endsWith("wireless_connector") || path.endsWith("wireless_hub");
    }

    public static boolean addPosToEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos pos,
                                          boolean byManual, boolean forceAuto) {
        if (emitter.level == null || emitter.level.isClientSide() || emitter.worldPosition.equals(pos)) return false;
        if (emitter.linkedPositions.contains(pos)) {
            if (byManual && emitter.manualLinkPositions.add(pos.immutable())) emitter.setChanged();
            return true;
        }
        if (byManual) {
            if (!emitter.isInRange(pos, emitter.getMaxLinkDistance())) return false;
        } else {
            if ((!forceAuto && !emitter.autoMode) || !emitter.isInRange(pos, emitter.linkDistance) || isAutoLinkExcluded(emitter.level, pos)) return false;
            IInWorldGridNodeHost host = emitter.level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, pos);
            if (host == null || (host instanceof CableBusBlockEntity && !emitter.allowAutoLinkCableLike)) return false;
        }
        if (getConnectableNodes(emitter.level, pos).isEmpty()) return false;
        if (byManual) emitter.manualLinkPositions.add(pos.immutable());
        if (emitter.pendingLinkPositions.add(pos.immutable())) {
            emitter.recentAddedPosCountdown = 2;
            emitter.setChanged();
            emitter.markForClientUpdate();
        }
        return true;
    }

    public static void removePosFromEmitter(@NotNull EnderEmitterBlockEntity emitter, @NotNull BlockPos pos) {
        emitter.pendingLinkPositions.remove(pos);
        emitter.linkedPositions.remove(pos);
        emitter.manualLinkPositions.remove(pos);
        List<IGridConnection> connections = emitter.linkedConnections.remove(pos);
        if (connections != null) connections.forEach(IGridConnection::destroy);
        emitter.setChanged();
        emitter.markForClientUpdate();
    }

    public static void addPosToRecentEmitter(@NotNull Level level, @NotNull BlockPos pos) {
        Set<BlockPos> positions = EMITTER_CHUNK_POSITIONS.get(new GlobalChunkPos(level.dimension(), pos));
        if (positions == null) return;
        List<BlockPos> sorted = new ArrayList<>(positions);
        sorted.sort(Comparator.comparingInt((BlockPos candidate) -> level.isLoaded(candidate) && level.getBlockEntity(candidate) instanceof EnderEmitterBlockEntity emitter && emitter.isActive() ? 0 : 1)
                .thenComparingDouble(candidate -> candidate.distSqr(pos)));
        for (BlockPos candidate : sorted) {
            if (!level.isLoaded(candidate)) continue;
            if (level.getBlockEntity(candidate) instanceof EnderEmitterBlockEntity emitter && addPosToEmitter(emitter, pos, false, false)) return;
        }
    }

    public static void removePosFromRecentEmitter(@NotNull Level level, @NotNull BlockPos pos) {
        Set<BlockPos> positions = EMITTER_CHUNK_POSITIONS.get(new GlobalChunkPos(level.dimension(), pos));
        if (positions == null) return;
        for (BlockPos candidate : Set.copyOf(positions)) {
            if (level.isLoaded(candidate) && level.getBlockEntity(candidate) instanceof EnderEmitterBlockEntity emitter) {
                removePosFromEmitter(emitter, pos);
            }
        }
    }

    public static void addAllRecentBEtoEmitter(@NotNull EnderEmitterBlockEntity emitter) {
        if (!(emitter.level instanceof ServerLevel serverLevel)) return;
        List<BlockEntity> targets = ChunkHelper.getBlockEntitiesInChunks(serverLevel, new ChunkPos(emitter.worldPosition), emitter.linkDistance);
        targets.sort(Comparator.comparingDouble(target -> target.getBlockPos().distSqr(emitter.worldPosition)));
        for (BlockEntity target : targets) addPosToEmitter(emitter, target.getBlockPos(), false, true);
    }

    public static void removeAllLinkedFromEmitter(@NotNull EnderEmitterBlockEntity emitter) {
        for (List<IGridConnection> connections : emitter.linkedConnections.values()) {
            connections.forEach(IGridConnection::destroy);
        }
        emitter.linkedConnections.clear();
        emitter.linkedPositions.clear();
        emitter.pendingLinkPositions.clear();
        emitter.manualLinkPositions.clear();
        emitter.needsScan = false;
        emitter.setChanged();
        emitter.markForClientUpdate();
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevelAccessor accessor) addPosToRecentEmitter(accessor.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevelAccessor accessor) removePosFromRecentEmitter(accessor.getLevel(), event.getPos());
    }

    public static void ensureBound(@Nullable MinecraftServer server) {
        if (server == null || boundServer != server) EMITTER_CHUNK_POSITIONS.clear();
        boundServer = server;
    }

    @SubscribeEvent
    public static void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        ensureBound(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        ensureBound(null);
    }
}
