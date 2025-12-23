package io.github.lounode.ae2cs.api.linker.broadcast;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.core.AEConfig;
import com.mojang.serialization.DataResult;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import io.github.lounode.ae2cs.api.util.AECSGridHelper;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 频段：持久化数据（纯数据） + 运行时在线端/连接/分配（下一 tick 重算）
 * - declaredSenders/declaredReceivers：纯数据（不受 chunk unload/load 影响）
 * - online*：运行时（由端上报在线/离线）
 * - recomputeRuntime：由 FrequencyBandManager 统一触发
 */
public class BroadcastFrequencyBand implements INBTSerializable<CompoundTag>
{
    /**
     * 当AE使用无线频段时，我们统计可用频段总数时直接返回此值
     */
    private static final long INFINITE_USABLE_CHANNELS = 1L << 60;

    /**
     * 每个接收者最多能分配到多少频道
     */
    private static final Supplier<Integer> MAX_RECEIVER_CHANNELS = () ->
    {
        ChannelMode mode = AEConfig.instance().getChannelMode();
        if (mode == ChannelMode.INFINITE)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return 32 * mode.getCableCapacityFactor();
        }
    };

    // ----------------- 持久化（纯数据） -----------------

    /**
     * 频段名称
     */
    @NotNull
    private String name;

    /**
     * 频段密码
     */
    @NotNull
    private String password;

    /**
     * 是否公共可见
     */
    private boolean isPublic;

    /**
     * 内存卡是否能从已连接机器上复制其连接状态
     */
    private boolean allowedMemoryCardCopy;

    /**
     * 白名单：对于公共频段，无需密码；对于私人频段，允许白名单上的人可见可连
     */
    @NotNull
    private final Set<UUID> whiteList = new HashSet<>();

    /**
     * 持久化声明：这个位置应该有sender
     */
    @NotNull
    private final Set<GlobalPos> declaredSenders = new LinkedHashSet<>();

    /**
     * 持久化声明：这个位置应该有receiver
     */
    @NotNull
    private final Set<GlobalPos> declaredReceivers = new LinkedHashSet<>();

    // ----------------- 运行时（不持久化） -----------------

    /**
     * 在线 sender：pos -> node（用于 grid/controller 绑定与冲突判断）
     */
    private final transient Object2ObjectOpenHashMap<GlobalPos, IGridNode> onlineSenderNodes = new Object2ObjectOpenHashMap<>();

    /**
     * 在线 receiver：pos -> node
     */
    private final transient Object2ObjectOpenHashMap<GlobalPos, IGridNode> onlineReceiverNodes = new Object2ObjectOpenHashMap<>();

    /**
     * 在线 receiver：pos -> host（用于设置 maxChannels/enabled）
     */
    private final transient Object2ObjectOpenHashMap<GlobalPos, CustomChannelProviderHost> onlineReceiverHosts = new Object2ObjectOpenHashMap<>();

    /**
     * 当前分配给 receiver 的频道
     */
    private final transient Object2IntOpenHashMap<GlobalPos> receiverAllocated = new Object2IntOpenHashMap<>();

    /**
     * receiver -> connection
     */
    private final transient Object2ObjectOpenHashMap<GlobalPos, IGridConnection> receiverConnections = new Object2ObjectOpenHashMap<>();

    /**
     * 本轮绑定的 grid/controller
     */
    private transient @Nullable IGrid bindGrid;

    /**
     * 给接收端分配频道时，应该让接收段连接到的控制器节点
     */
    private transient @Nullable IGridNode controllerNode;

    public BroadcastFrequencyBand(@NotNull String name, @NotNull String password, boolean isPublic, boolean allowedMemoryCardCopy)
    {
        this.name = name;
        this.password = password;
        this.isPublic = isPublic;
        this.allowedMemoryCardCopy = allowedMemoryCardCopy;

        receiverAllocated.defaultReturnValue(0);
    }

    public @NotNull String getName()
    {
        return name;
    }

    // ----------------- 纯数据 API（只改 declared，不查节点/不动连接） -----------------

    public boolean declareSender(GlobalPos pos)
    {
        boolean changed = declaredSenders.add(pos);
        if (changed) FrequencyBandManager.markDirty();
        return changed;
    }

    public boolean undeclareSender(GlobalPos pos)
    {
        boolean changed = declaredSenders.remove(pos);
        if (changed) FrequencyBandManager.markDirty();
        return changed;
    }

    public boolean declareReceiver(GlobalPos pos)
    {
        boolean changed = declaredReceivers.add(pos);
        if (changed) FrequencyBandManager.markDirty();
        return changed;
    }

    public boolean undeclareReceiver(GlobalPos pos)
    {
        boolean changed = declaredReceivers.remove(pos);
        if (changed) FrequencyBandManager.markDirty();
        return changed;
    }

    // ----------------- 运行时 API（在线/离线，只登记 + 标记下一 tick 重算） -----------------

    public void onSenderOnline(MinecraftServer server, GlobalPos pos, IGridNode node)
    {
        if (!declaredSenders.contains(pos)) return;
        onlineSenderNodes.put(pos, node);
        FrequencyBandManager.markRuntimeDirty(server, name);
    }

    public void onSenderOffline(MinecraftServer server, GlobalPos pos)
    {
        onlineSenderNodes.remove(pos);
        FrequencyBandManager.markRuntimeDirty(server, name);
    }

    public void onReceiverOnline(MinecraftServer server, GlobalPos pos, IGridNode node, CustomChannelProviderHost host)
    {
        if (!declaredReceivers.contains(pos)) return;
        onlineReceiverNodes.put(pos, node);
        onlineReceiverHosts.put(pos, host);
        FrequencyBandManager.markRuntimeDirty(server, name);
    }

    public void onReceiverOffline(MinecraftServer server, GlobalPos pos)
    {
        onlineReceiverNodes.remove(pos);

        CustomChannelProviderHost host = onlineReceiverHosts.remove(pos);
        if (host != null)
        {
            // 接收端打算离线时，将其最大频道承载量还原至原版情况
            host.setEnabledCustomChannel(false);
        }

        receiverAllocated.removeInt(pos);

        IGridConnection conn = receiverConnections.remove(pos);
        if (conn != null) conn.destroy();

        FrequencyBandManager.markRuntimeDirty(server, name);
    }

    /**
     * 重新统计全部发送端频道并将链接分配到接收端，由FrequencyBandManager统一管理调用
     * <p>
     *
     * @see FrequencyBandManager#tick(ServerTickEvent.Post)
     */
    void recomputeRuntime()
    {
        // 1) 选择 bindGrid：sender所在网络必须有控制器，且所有在线sender必须同一grid，否则视为冲突，所有接收端断链
        IGrid newBindGrid = null;
        boolean conflict = false;

        for (var entry : onlineSenderNodes.object2ObjectEntrySet())
        {
            IGridNode senderNode = entry.getValue();
            if (senderNode == null)
            {
                conflict = true;
                break;
            }

            IGrid grid = senderNode.getGrid();
            if (grid == null)
            {
                conflict = true;
                break;
            }

            if (grid.getPathingService().getControllerState() != ControllerState.CONTROLLER_ONLINE)
            {
                conflict = true;
                break;
            }

            if (newBindGrid == null)
            {
                newBindGrid = grid;
            }
            else if (newBindGrid != grid)
            {
                conflict = true;
                break;
            }
        }

        if (conflict || newBindGrid == null)
        {
            bindGrid = null;
            controllerNode = null;
            dropAllReceiversToVanillaAndDisconnect();
            return;
        }

        // 找控制器节点，用于让接收端建立直连
        IGridNode newController = AECSGridHelper.getControlNode(newBindGrid);
        if (newController == null)
        {
            bindGrid = null;
            controllerNode = null;
            dropAllReceiversToVanillaAndDisconnect();
            return;
        }

        boolean controllerChanged = (controllerNode != newController);
        bindGrid = newBindGrid;
        controllerNode = newController;

        // controller 变更：断开旧连接
        if (controllerChanged)
        {
            for (IGridConnection conn : receiverConnections.values())
            {
                if (conn != null) conn.destroy();
            }
            receiverConnections.clear();
        }

        // 2) 统计 sender 提供的可用频道总量（long 防溢出）
        long totalUsable = computeTotalUsableChannels();

        // 3) 分配
        int capPerReceiver = MAX_RECEIVER_CHANNELS.get();
        allocateFillFirst(totalUsable, capPerReceiver);
    }

    /**
     * 统计 sender 提供的总可用频道量
     */
    private long computeTotalUsableChannels()
    {
        boolean infinite = (AEConfig.instance().getChannelMode() == ChannelMode.INFINITE);
        if (infinite)
        {
            return INFINITE_USABLE_CHANNELS;
        }

        long totalUsable = 0;

        long maxNeed = INFINITE_USABLE_CHANNELS;

        for (var entry : onlineSenderNodes.object2ObjectEntrySet())
        {
            IGridNode senderNode = entry.getValue();
            if (senderNode == null) continue;

            int usable = 0;
            if (senderNode.getOwner() instanceof BroadcastSenderHost senderHost)
            {
                usable = senderHost.getCouldSendChannels();
            }

            if (usable < 0) usable = 0;

            totalUsable += usable;
            if (totalUsable >= maxNeed)
            {
                totalUsable = maxNeed;
                break;
            }
        }

        return totalUsable;
    }

    /**
     * 无 sender/冲突时：所有在线 receiver 恢复原版并断开连接
     */
    private void dropAllReceiversToVanillaAndDisconnect()
    {
        // 恢复原版逻辑
        for (var entry : onlineReceiverHosts.object2ObjectEntrySet())
        {
            CustomChannelProviderHost host = entry.getValue();
            if (host != null) host.setEnabledCustomChannel(false);
        }

        receiverAllocated.clear();

        for (IGridConnection conn : receiverConnections.values())
        {
            if (conn != null) conn.destroy();
        }
        receiverConnections.clear();
    }

    /**
     * 满额优先：按 declaredReceivers 的稳定顺序尽可能喂满
     */
    private void allocateFillFirst(long totalUsable, int capPerReceiver)
    {
        long remaining = totalUsable;

        for (GlobalPos rp : declaredReceivers)
        {
            IGridNode rNode = onlineReceiverNodes.get(rp);
            CustomChannelProviderHost host = onlineReceiverHosts.get(rp);
            if (rNode == null || host == null) continue;

            int alloc = computeAllocFillFirst(remaining, capPerReceiver);
            if (capPerReceiver != Integer.MAX_VALUE)
            {
                remaining -= alloc;
            }

            applyReceiver(rp, rNode, host, alloc);
        }

        // 在线但未声明的 receiver，一律恢复原版并断开
        for (var entry : onlineReceiverHosts.object2ObjectEntrySet())
        {
            GlobalPos rp = entry.getKey();
            if (!declaredReceivers.contains(rp))
            {
                CustomChannelProviderHost host = entry.getValue();
                if (host != null) host.setEnabledCustomChannel(false);

                receiverAllocated.removeInt(rp);
                IGridConnection conn = receiverConnections.remove(rp);
                if (conn != null) conn.destroy();
            }
        }
    }

    private int computeAllocFillFirst(long remaining, int capPerReceiver)
    {
        if (remaining <= 0) return 0;
        if (capPerReceiver == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        long give = Math.min((long) capPerReceiver, remaining);
        return (int) give;
    }

    /**
     * 应用分配到单个 receiver：
     * - alloc==0：禁用 custom（恢复原版）并断开连接
     * - alloc>0：启用 custom，并确保连接到 controller
     *
     * @param receiverPos  接收者位置
     * @param receiverNode 接收者节点
     * @param host         接收者的频段承载对象
     * @param newAlloc     分配到此节点的频道数
     */
    private void applyReceiver(GlobalPos receiverPos, IGridNode receiverNode, CustomChannelProviderHost host, int newAlloc)
    {
        int oldAlloc = receiverAllocated.getInt(receiverPos);
        boolean allocChanged = (oldAlloc != newAlloc);

        if (newAlloc <= 0)
        {
            receiverAllocated.removeInt(receiverPos);
            host.setEnabledCustomChannel(false);

            IGridConnection oldConn = receiverConnections.remove(receiverPos);
            if (oldConn != null) oldConn.destroy();
            return;
        }

        if (!host.isEnabledCustomChannel())
        {
            host.setEnabledCustomChannel(true);
            allocChanged = true;
        }

        if (allocChanged)
        {
            receiverAllocated.put(receiverPos, newAlloc);
            host.setMaxChannels(newAlloc);
        }

        if (controllerNode == null)
        {
            host.setEnabledCustomChannel(false);
            IGridConnection oldConn = receiverConnections.remove(receiverPos);
            if (oldConn != null) oldConn.destroy();
            return;
        }

        IGridConnection conn = receiverConnections.get(receiverPos);

        // 这里的目的是尽量减少重连次数，但是如果后续发现频道传递不及时/不稳定，最好把这里改成true
        boolean needRebuild = conn == null || allocChanged;

        if (needRebuild)
        {
            if (conn != null) conn.destroy();
            receiverConnections.put(receiverPos, GridHelper.createConnection(controllerNode, receiverNode));
        }
    }

    // ----------------- 持久化 -----------------
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("password", password);
        tag.putBoolean("is_public", isPublic);
        tag.putBoolean("allowed_memory_card_copy", allowedMemoryCardCopy);

        ListTag whiteListTag = new ListTag();
        for (UUID id : whiteList)
        {
            if (id != null) whiteListTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("white_list", whiteListTag);

        ListTag senderListTag = new ListTag();
        for (GlobalPos sender : declaredSenders)
        {
            if (sender == null) continue;
            DataResult<Tag> encoded = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, sender);
            encoded.result().ifPresent(senderListTag::add);
        }
        tag.put("sender_list", senderListTag);

        ListTag receiverListTag = new ListTag();
        for (GlobalPos receiver : declaredReceivers)
        {
            if (receiver == null) continue;
            DataResult<Tag> encoded = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, receiver);
            encoded.result().ifPresent(receiverListTag::add);
        }
        tag.put("receiver_list", receiverListTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag compoundTag)
    {
        this.name = compoundTag.getString("name");
        this.password = compoundTag.getString("password");
        this.isPublic = compoundTag.getBoolean("is_public");
        this.allowedMemoryCardCopy = compoundTag.getBoolean("allowed_memory_card_copy");

        this.whiteList.clear();
        this.declaredSenders.clear();
        this.declaredReceivers.clear();

        this.onlineSenderNodes.clear();
        this.onlineReceiverNodes.clear();
        this.onlineReceiverHosts.clear();
        this.receiverAllocated.clear();
        for (IGridConnection conn : receiverConnections.values())
        {
            if (conn != null) conn.destroy();
        }
        this.receiverConnections.clear();
        this.bindGrid = null;
        this.controllerNode = null;

        ListTag whiteListTag = compoundTag.getList("white_list", 8);
        for (Tag t : whiteListTag)
        {
            if (t instanceof StringTag stringTag)
            {
                try
                {
                    UUID id = UUID.fromString(stringTag.getAsString());
                    this.whiteList.add(id);
                }
                catch (Throwable e)
                {
                    AE2CrystalScience.LOGGER.error("Failed to convert String to UUID: " + stringTag.getAsString(), e);
                }
            }
        }

        ListTag senderListTag = compoundTag.getList("sender_list", 10);
        for (Tag t : senderListTag)
        {
            DataResult<GlobalPos> parsed = GlobalPos.CODEC.parse(NbtOps.INSTANCE, t);
            parsed.result().ifPresentOrElse(
                    this.declaredSenders::add,
                    () -> AE2CrystalScience.LOGGER.error("Failed to parse GlobalPos from sender_list entry: {}", t)
            );
        }

        ListTag receiverListTag = compoundTag.getList("receiver_list", 10);
        for (Tag t : receiverListTag)
        {
            DataResult<GlobalPos> parsed = GlobalPos.CODEC.parse(NbtOps.INSTANCE, t);
            parsed.result().ifPresentOrElse(
                    this.declaredReceivers::add,
                    () -> AE2CrystalScience.LOGGER.error("Failed to parse GlobalPos from receiver_list entry: {}", t)
            );
        }
    }
}
