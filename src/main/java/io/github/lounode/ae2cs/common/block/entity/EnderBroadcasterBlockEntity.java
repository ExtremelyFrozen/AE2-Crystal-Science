package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.networking.*;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.stacks.AEItemKey;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.util.SettingsFrom;
import io.github.lounode.ae2cs.api.CustomChannelProviderHost;
import io.github.lounode.ae2cs.api.linker.broadcast.*;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EnderBroadcasterBlockEntity extends AENetworkedComponentBlockEntity
        implements CustomChannelProviderHost, BroadcastSenderHost, BroadcastReceiverHost, IUpgradeableObject,
        CustomReturnableSubMenuHost
{
    private static final int VIRTUAL_SENDER_NODE_HARD_CAP = 768;

    /**
     * 作为接收者最多能分配到多少频道，用来限制期望频道量的设置
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

    private static final IGridNodeListener<EnderBroadcasterBlockEntity> VIRTUAL_NODE_LISTENER =
            new IGridNodeListener<>()
            {
                @Override
                public void onSaveChanges(EnderBroadcasterBlockEntity owner, IGridNode node)
                {
                    // 虚拟节点不持久化，不需要标脏 BE
                }

                @Override
                public void onStateChanged(EnderBroadcasterBlockEntity owner, IGridNode node, State reason)
                {
                    // 当节点本身的频道分配完成/变化时，自动让频段再统计一次
                    if (owner == null) return;
                    if (owner.connectionType != ConnectionType.AS_SENDER) return;
                    if (owner.bandId == null || owner.bandId.isEmpty()) return;

                    if (reason == State.CHANNEL || reason == State.GRID_BOOT || reason == State.POWER)
                    {
                        // 标脏，下一tick重新统计
                        // 由于发射端channel仅仅取决于此处连接状态，所以也仅需要在此处进行标脏即可
                        owner.markNeedRecountVirtualSenderNodes();
                    }
                }
            };

    private String bandId = "";
    private ConnectionType connectionType = ConnectionType.NO_CONNECTION;
    private int expectedChannels = 32;

    // CustomChannelProviderHost 数据（接收端用）
    private int customMaxChannels = 0;
    private boolean enabledCustomChannel = false;

    // 发送端：虚拟节点池（每个虚拟节点吃 1 个频道）
    private final List<IManagedGridNode> virtualSenderNodes = new ArrayList<>();
    private int virtualSenderNodeTarget = 0;
    private int succeedVirtualSenderNodes = 0; // 成功连接并分配得到频道的虚拟节点数
    private boolean needRecountVirtualSenderNodes = true;

    // 用于客户端渲染
    private boolean activeForClient = false;
    private boolean asSenderForClient = false;

    public EnderBroadcasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    public String getBandName()
    {
        return bandId;
    }

    public ConnectionType getConnectionType()
    {
        return connectionType;
    }

    public boolean isActiveForClient()
    {
        return activeForClient;
    }

    public boolean isAsSenderForClient()
    {
        return asSenderForClient;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();

        // 重新计数succeedVirtualSenderNodes
        if (needRecountVirtualSenderNodes)
        {
            this.needRecountVirtualSenderNodes = false;
            int succeedVirtualSenderNodes = countSucceedVirtualSenderNodes();
            if (this.succeedVirtualSenderNodes != succeedVirtualSenderNodes)
            {
                this.succeedVirtualSenderNodes = succeedVirtualSenderNodes;
                if (this.connectionType == ConnectionType.AS_SENDER)
                {
                    this.markBandRuntimeDirty(); // 可对外发送频段量变化，因此需要让频段重新分配
                }
            }
        }

        // 更新客户端渲染信息
        if (level == null || level.isClientSide()) return;
        boolean asSender = this.connectionType == ConnectionType.AS_SENDER;
        boolean asReceiver = this.connectionType == ConnectionType.AS_RECEIVER;
        boolean active = false;
        if (asSender)
        {
            active = getCouldSendChannels() > 0;
        }
        else if (asReceiver && getMainNode().getGrid() != null)
        {
            active = getMainNode().getGrid().getEnergyService().isNetworkPowered();
        }
        if (active != this.activeForClient)
        {
            this.activeForClient = active;
            markForClientUpdate();
        }
        if (asSender != this.asSenderForClient)
        {
            this.asSenderForClient = asSender;
            markForClientUpdate();
        }
    }

    // ---------------- BroadcastSenderHost ----------------

    /**
     * 发送端对外“可用频道数”：
     * 统计虚拟节点中满足频道要求（拿到频道）的数量。
     */
    @Override
    public int getCouldSendChannels()
    {
        if (level == null || level.isClientSide())
        {
            return 0;
        }

        if (connectionType != ConnectionType.AS_SENDER)
        {
            return 0;
        }

        return succeedVirtualSenderNodes;
    }

    @Override
    public int getExpectedChannels()
    {
        return this.expectedChannels;
    }

    public void setExpectedChannels(int expectedChannels)
    {
        int newExpectedChannels = Math.max(0, expectedChannels);
        newExpectedChannels = Math.min(newExpectedChannels, MAX_RECEIVER_CHANNELS.get());
        if (newExpectedChannels == this.expectedChannels) return;

        this.expectedChannels = newExpectedChannels;
        markBandRuntimeDirty(); // 期望频道数量改变，因此需要标脏
        setChanged();
    }

    // ---------------- CustomChannelProviderHost（接收端用） ----------------

    @Override
    public int getMaxChannels()
    {
        return Math.max(0, customMaxChannels);
    }

    @Override
    public void setMaxChannels(int maxChannels)
    {
        this.customMaxChannels = Math.max(0, maxChannels);
    }

    @Override
    public boolean isEnabledCustomChannel()
    {
        return enabledCustomChannel;
    }

    @Override
    public void setEnabledCustomChannel(boolean enabled)
    {
        this.enabledCustomChannel = enabled;
    }

    // ---------------- 发送端虚拟节点维护 ----------------

    /**
     * 获取我们当前应该创建的虚拟节点数，一般为32 * factor
     */
    private int computeSenderVirtualNodeTarget()
    {
        ChannelMode mode = AEConfig.instance().getChannelMode();
        if (mode == ChannelMode.INFINITE)
        {
            return 0;
        }

        int desired = Math.max(0, 32 * mode.getCableCapacityFactor());

        // 接收端紧贴控制器，则还要乘上控制器数量，以模拟接收多面频道功能
        if (level != null)
        {
            int controlSide = 0;
            for (Direction direction : Direction.values())
            {
                if (level.getBlockState(worldPosition.relative(direction)).getBlock() == AEBlocks.CONTROLLER.block())
                    controlSide++;
            }
            int mutil = Math.max(1, controlSide);
            desired = desired * mutil;
        }
        return Math.min(desired, VIRTUAL_SENDER_NODE_HARD_CAP);
    }

    /**
     * 确保BE链接到足够多的虚拟节点
     */
    private void ensureSenderVirtualNodes()
    {
        if (level == null || level.isClientSide())
        {
            return;
        }

        if (connectionType != ConnectionType.AS_SENDER)
        {
            destroySenderVirtualNodes();
            return;
        }

        int target = computeSenderVirtualNodeTarget();
        if (target <= 0)
        {
            destroySenderVirtualNodes();
            return;
        }

        // 目标数量没变且节点都 ready：不动
        if (target == virtualSenderNodeTarget && virtualSenderNodes.size() == target)
        {
            boolean allReady = true;
            for (var managedNode : virtualSenderNodes)
            {
                if (!managedNode.isReady())
                {
                    allReady = false;
                    break;
                }
            }
            if (allReady)
            {
                return;
            }
        }

        destroySenderVirtualNodes();
        virtualSenderNodeTarget = target;

        // 用本机的主节点作为锚点
        IGridNode anchor = getMainNode().getNode();
        if (anchor == null)
        {
            // 主节点还没准备好
            return;
        }

        // 在实际链接之前，打开自定义频道量并指定最大数量
        setEnabledCustomChannel(true);
        setMaxChannels(target);

        for (int i = 0; i < target; i++)
        {
            IManagedGridNode virtualManagedNode = GridHelper.createManagedNode(this, VIRTUAL_NODE_LISTENER)
                    .setInWorldNode(false)
                    .setIdlePowerUsage(10.0) // 这里可以消耗掉一些能量，模拟发送频道消耗能量
                    .setTagName("sender_virtual_" + i)
                    .setFlags(GridFlags.REQUIRE_CHANNEL);

            virtualManagedNode.create(level, worldPosition);

            IGridNode virtualNode = virtualManagedNode.getNode();
            if (virtualNode != null)
            {
                // 把所有虚拟节点链接到我们的主节点上
                GridHelper.createConnection(anchor, virtualNode);
            }

            virtualSenderNodes.add(virtualManagedNode);
        }
    }

    /**
     * 摧毁所有已建立的虚拟节点
     */
    private void destroySenderVirtualNodes()
    {
        if (!virtualSenderNodes.isEmpty())
        {
            for (var m : virtualSenderNodes)
            {
                try
                {
                    m.destroy();
                }
                catch (Throwable ignored)
                {
                }
            }
            virtualSenderNodes.clear();
        }
        virtualSenderNodeTarget = 0;
    }

    /**
     * 仅统计成功分配到频段的虚拟节点并更新到字段
     */
    private int countSucceedVirtualSenderNodes()
    {
        int count = 0;
        for (var managed : virtualSenderNodes)
        {
            var node = managed.getNode();
            if (node != null && node.isOnline()) // 同时检查能量和频道，模拟电量消耗
            {
                count++;
            }
        }
        return count;
    }

    /**
     * 标记需要重新统计虚拟节点成功分配到频道的数量
     */
    private void markNeedRecountVirtualSenderNodes()
    {
        this.needRecountVirtualSenderNodes = true;
    }

    // ---------------- 连接逻辑（纯数据 declared + 运行时 online/offline） ----------------

    /**
     * 对外接口，使其永久链接到某个频段
     */
    public void connectToBand(String newBandId, boolean asSender)
    {
        if (level == null || level.isClientSide()) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        BroadcastFrequencyBand newBand = FrequencyBandManager.getBand(newBandId);
        if (newBand == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), worldPosition);

        // 先清空旧频段信号
        if (!bandId.isEmpty())
        {
            BroadcastFrequencyBand oldBand = FrequencyBandManager.getBand(bandId);
            if (oldBand != null)
            {
                if (connectionType == ConnectionType.AS_RECEIVER)
                {
                    oldBand.onReceiverOffline(server, globalPos);
                }
                else if (connectionType == ConnectionType.AS_SENDER)
                {
                    oldBand.onSenderOffline(server, globalPos);
                }

                if (oldBand != newBand)
                {
                    oldBand.undeclareSender(globalPos);
                    oldBand.undeclareReceiver(globalPos);
                }
            }
        }

        // 根据不同端角色进行链接
        if (asSender)
        {
            newBand.declareSender(globalPos);
            newBand.undeclareReceiver(globalPos);

            this.connectionType = ConnectionType.AS_SENDER;
            ensureSenderVirtualNodes();

            IGridNode node = getMainNode().getNode();
            if (node != null)
            {
                newBand.onSenderOnline(server, globalPos, node);
            }
        }
        else
        {
            newBand.declareReceiver(globalPos);
            newBand.undeclareSender(globalPos);

            destroySenderVirtualNodes();
            setEnabledCustomChannel(true);
            setMaxChannels(0);

            this.connectionType = ConnectionType.AS_RECEIVER;

            IGridNode node = getMainNode().getNode();
            if (node != null)
            {
                newBand.onReceiverOnline(server, globalPos, node, this);
            }
        }

        this.bandId = newBand.getName();
        markForClientUpdate();
        setChanged();
    }

    /**
     * 对外接口，让be永久断开频段链接
     */
    @Override
    public void cleanConnectionPermanent()
    {
        if (level == null || level.isClientSide()) return;

        // 无论如何先清掉 sender 虚拟节点
        destroySenderVirtualNodes();

        MinecraftServer server = level.getServer();
        if (server == null) return;

        if (bandId.isEmpty() || connectionType == ConnectionType.NO_CONNECTION)
        {
            setEnabledCustomChannel(false);
            return;
        }

        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        GlobalPos globalPos = GlobalPos.of(level.dimension(), worldPosition);

        if (band != null)
        {
            if (connectionType == ConnectionType.AS_RECEIVER)
            {
                band.onReceiverOffline(server, globalPos);
            }
            else if (connectionType == ConnectionType.AS_SENDER)
            {
                band.onSenderOffline(server, globalPos);
            }

            band.undeclareSender(globalPos);
            band.undeclareReceiver(globalPos);
        }

        bandId = "";
        markForClientUpdate();
        connectionType = ConnectionType.NO_CONNECTION;
        setEnabledCustomChannel(false);
        setChanged();
    }

    /**
     * 将链接的频段标脏，让频段下一个tick重新计算频道分配，
     * 仅当发射端提供的频道/接收端需求的频道量变化时调用。
     */
    private void markBandRuntimeDirty()
    {
        if (level == null || level.isClientSide()) return;
        if (bandId.isEmpty()) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        FrequencyBandManager.markRuntimeDirty(server, bandId);
    }

    /**
     * 区块卸载时，进行运行时下线，并销毁sender虚拟节点释放频道
     */
    @Override
    public void onChunkUnloaded()
    {
        destroySenderVirtualNodes();

        if (level != null && !level.isClientSide() && !bandId.isEmpty() && connectionType != ConnectionType.NO_CONNECTION)
        {
            MinecraftServer server = level.getServer();
            if (server != null)
            {
                BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
                if (band != null)
                {
                    GlobalPos gp = GlobalPos.of(level.dimension(), worldPosition);
                    if (connectionType == ConnectionType.AS_RECEIVER)
                    {
                        band.onReceiverOffline(server, gp);
                    }
                    else if (connectionType == ConnectionType.AS_SENDER)
                    {
                        band.onSenderOffline(server, gp);
                    }
                }
            }
        }
        super.onChunkUnloaded();
    }

    /**
     * 网络初始化完成，恢复declared并上线
     */
    @Override
    public void onReady()
    {
        super.onReady();

        if (level == null || level.isClientSide()) return;

        if (bandId.isEmpty() || connectionType == ConnectionType.NO_CONNECTION)
        {
            setEnabledCustomChannel(false);
            destroySenderVirtualNodes();
            return;
        }

        MinecraftServer server = level.getServer();
        if (server == null) return;

        BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
        if (band == null)
        {
            setEnabledCustomChannel(false);
            destroySenderVirtualNodes();
            return;
        }

        GlobalPos gp = GlobalPos.of(level.dimension(), worldPosition);
        IGridNode node = getMainNode().getNode();
        if (node == null) return;

        if (connectionType == ConnectionType.AS_RECEIVER)
        {
            band.declareReceiver(gp);
            band.undeclareSender(gp);

            destroySenderVirtualNodes();
            setEnabledCustomChannel(true);

            band.onReceiverOnline(server, gp, node, this);
        }
        else if (connectionType == ConnectionType.AS_SENDER)
        {
            band.declareSender(gp);
            band.undeclareReceiver(gp);

            ensureSenderVirtualNodes();
            band.onSenderOnline(server, gp, node);
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason)
    {
        super.onMainNodeStateChanged(reason);
        if (level == null || level.isClientSide()) return;
        if (reason == IGridNodeListener.State.CHANNEL || reason == IGridNodeListener.State.GRID_BOOT)
        {
            // 内部在连接数相同时会阻止重连节点，不会重复造成网络变化
            ensureSenderVirtualNodes();
        }
    }

    // ---------------- 内存卡 ----------------

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD)
        {
            MemoryCardBandInfo targetLink = null;
            if (input.contains("memory_card_band_info"))
            {
                targetLink = MemoryCardBandInfo.readFromNBT(input.getCompound("memory_card_band_info"));
            }
            if (targetLink == null) return;

            BroadcastFrequencyBand band = FrequencyBandManager.getBand(targetLink.bandName());
            if (band == null || !band.isAllowedMemoryCardCopy()) return;

            this.cleanConnectionPermanent();
            this.connectToBand(targetLink.bandName(), targetLink.asSender());

            Integer expectChannelsPacked = null;
            if (input.contains("memory_card_broadcaster_receiver_expected_channels"))
            {
                expectChannelsPacked = input.getInt("memory_card_broadcaster_receiver_expected_channels");
            }
            if (expectChannelsPacked != null && !targetLink.asSender())
            {
                this.setExpectedChannels(expectChannelsPacked);
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag builder, @Nullable Player player)
    {
        super.exportSettings(mode, builder, player);

        if (mode == SettingsFrom.MEMORY_CARD)
        {
            if (bandId == null || bandId.isEmpty()) return;
            BroadcastFrequencyBand band = FrequencyBandManager.getBand(bandId);
            if (band == null || !band.isAllowedMemoryCardCopy()) return;

            boolean asSender = this.connectionType == ConnectionType.AS_SENDER;

            builder.put("memory_card_band_info", MemoryCardBandInfo.writeToNBT(new MemoryCardBandInfo(bandId, asSender)));
            if (!asSender)
            {
                builder.putInt("memory_card_broadcaster_receiver_expected_channels", this.expectedChannels);
            }
        }
    }

    // ---------------- NBT ----------------

    @Override
    public void saveAdditional(CompoundTag data)
    {
        super.saveAdditional(data);
        data.putString("band_id", bandId);
        data.putString("connection_type", connectionType.name());
        data.putBoolean("enabled_custom_channel", enabledCustomChannel);
        data.putInt("custom_max_channels", customMaxChannels);
        data.putInt("expected_channels", expectedChannels);
    }

    @Override
    public void loadTag(CompoundTag data)
    {
        super.loadTag(data);
        bandId = data.getString("band_id");

        String t = data.getString("connection_type");
        connectionType = t.isEmpty() ? ConnectionType.NO_CONNECTION : ConnectionType.valueOf(t);

        enabledCustomChannel = data.getBoolean("enabled_custom_channel");
        customMaxChannels = data.getInt("custom_max_channels");
        this.expectedChannels = data.getInt("expected_channels");
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data)
    {
        super.writeToStream(data);
        data.writeUtf(bandId);
        data.writeBoolean(this.activeForClient);
        data.writeBoolean(this.asSenderForClient);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data)
    {
        super.readFromStream(data);
        this.bandId = data.readUtf();
        this.activeForClient = data.readBoolean();
        this.asSenderForClient = data.readBoolean();
        return true;
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSBlocks.ENDER_BROADCASTER_BLOCK.get());
    }

    public enum ConnectionType
    {
        AS_SENDER,
        AS_RECEIVER,
        NO_CONNECTION
    }
}