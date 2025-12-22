package io.github.lounode.ae2cs.api.linker.broadcast;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ControllerState;
import appeng.me.Grid;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import com.mojang.serialization.DataResult;
import io.github.lounode.ae2cs.AE2CrystalScience;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

/**
 * 用于末影广播装置的频段本体，其记录了以下信息
 * - 频段本身名称（作为唯一id）
 * - 频段的设置（例如：密码、白名单、是否为私人频段、是否允许内存卡复制链接）
 * - 其所链接到的所有广播装置，按发射端和接收端分隔（以带维度的pos记录）
 */
public class BroadcastFrequencyBand implements INBTSerializable<CompoundTag>
{
    /** 每个接收者最多能分配到多少频段 */
    private static final int MAX_RECEIVER_CHANNELS = 32;

    // 持久化数据

    /** 名称（唯一id） */
    @NotNull
    private String name;

    /** 密码 */
    @NotNull
    private String password;

    /** 是否公开 */
    private boolean isPublic;

    /** 是否允许内存卡复制使用了此频段的链接 */
    private boolean allowedMemoryCardCopy;

    /** 白名单（可见私人频道或无需密码链接） */
    @NotNull
    private final Set<UUID> whiteList = new HashSet<>();

    /** 所有发送者的坐标 */
    @NotNull
    private final Set<GlobalPos> senderList = new LinkedHashSet<>();

    /** 所有接收者的坐标 */
    @NotNull
    private final Set<GlobalPos> receiverList = new HashSet<>();

    // 运行时数据

    /** 给接收端发送的网络 */
    @Nullable
    private IGrid bindGrid;

    /** 可对外发送的频道总量 */
    private int usableChannel = 0;

    /** 已使用的频道总量 */
    private int usedChannel = 0;

    private final Object2IntOpenHashMap<GlobalPos> sender2UsableChannelMap = new Object2IntOpenHashMap<>();

    private final Object2IntOpenHashMap<GlobalPos> receiver2UsedChannelMap = new Object2IntOpenHashMap<>();


    public BroadcastFrequencyBand(@NotNull String name, @NotNull String password, boolean isPublic, boolean allowedMemoryCardCopy)
    {
        this.name = name;
        this.password = password;
        this.isPublic = isPublic;
        this.allowedMemoryCardCopy = allowedMemoryCardCopy;

        sender2UsableChannelMap.defaultReturnValue(0);
    }

    /**
     * 更新当前发送者状态，如果此前不存在于频道，则加入此发送者
     * <p>
     * 连接到频段的发射端必须与所有其他发射端位于同一个有控制器的网络
     * @return 返回当前设备是否成功连接，如果没有，则尽可能返回原因
     */
    public LinkState updateSender(Level level, BlockPos pos)
    {
        IGridNode iGridNode = GridHelper.getExposedNode(level, pos, Direction.NORTH);
        if(iGridNode instanceof GridNode gridNode)
        {
            IGrid grid = gridNode.getGrid();
            if(grid == null) return LinkState.NO_SUCCESS;

            if(grid.getPathingService().getControllerState() != ControllerState.CONTROLLER_ONLINE) return LinkState.NO_CONTROL;

            if(bindGrid == null)
            {
                bindGrid = grid;
            }
            else if(bindGrid != grid)
            {
                return LinkState.GRID_CONFLICT;
            }

            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
            int newUsableChannel = gridNode.subtreeMaxChannels - gridNode.getUsedChannels();

            // 重置状态并新增
            usableChannel -= sender2UsableChannelMap.removeInt(globalPos);
            usableChannel += newUsableChannel;
            sender2UsableChannelMap.put(globalPos, usableChannel);
            senderList.add(globalPos);
            return LinkState.SUCCESS;
        }
        return LinkState.NO_SUCCESS;
    }

    /**
     * 将发送者从频段移除
     */
    public void removeSender(Level level, BlockPos pos)
    {
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        usableChannel -= sender2UsableChannelMap.removeInt(globalPos);
        senderList.remove(globalPos);
        if(senderList.isEmpty())
            bindGrid = null;
    }

    /**
     * 更新接收者状态，如果此前不存在于频道，则加入此接收者
     */
    public LinkState updateReceiver(Level level, BlockPos pos)
    {
        IGridNode receiverNode = GridHelper.getExposedNode(level, pos, Direction.NORTH);
        if(receiverNode == null) return LinkState.NO_SUCCESS;
        IGrid grid = receiverNode.getGrid();
        if(grid == null) return LinkState.NO_SUCCESS;

        // 如果接收者处于一个有控制网络，则必须与此网络为同一个，否则冲突
        if(grid.getPathingService().getControllerState() == ControllerState.CONTROLLER_ONLINE
                && grid != bindGrid)
        {
            return LinkState.GRID_CONFLICT;
        }

        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);

        usableChannel -= receiver2UsedChannelMap.removeInt(globalPos);
        int receiverAcceptChannel = Math.min(MAX_RECEIVER_CHANNELS, usableChannel - usedChannel);

        usedChannel += receiverAcceptChannel;
        receiver2UsedChannelMap.put(globalPos, receiverAcceptChannel);
        receiverList.add(globalPos);
        // TODO 在建立连接之前，给接收者一个信号，使其能重设最大可用频道
        GridHelper.createConnection(grid.getPivot(), receiverNode);
        return LinkState.SUCCESS;
    }

    /**
     * 移除接收者
     */
    public void removeReceiver(Level level, BlockPos pos)
    {
        IGridNode receiverNode = GridHelper.getExposedNode(level, pos, Direction.NORTH);
        if(receiverNode == null) return;
        IGrid grid = receiverNode.getGrid();
        if(grid == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);

        // 清除使用频段并断开全部连接以使其重新计算
        usableChannel -= receiver2UsedChannelMap.removeInt(globalPos);
        receiverList.remove(globalPos);
        receiverNode.getConnections().forEach(IGridConnection::destroy);
    }

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
            if (id != null)
                whiteListTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("white_list", whiteListTag);
        ListTag senderListTag = new ListTag();
        for (GlobalPos sender : senderList)
        {
            if (sender == null) continue;

            DataResult<Tag> encoded = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, sender);
            encoded.result().ifPresent(senderListTag::add);
        }
        tag.put("sender_list", senderListTag);
        ListTag receiverListTag = new ListTag();
        for (GlobalPos receiver : receiverList)
        {
            if (receiver == null) continue;

            DataResult<Tag> encoded = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, receiver);
            encoded.result().ifPresent(receiverListTag::add);
        }
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
        this.senderList.clear();
        this.receiverList.clear();

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
                    this.senderList::add,
                    () -> AE2CrystalScience.LOGGER.error("Failed to parse GlobalPos from sender_list entry: {}", t)
            );
        }

        ListTag receiverListTag = compoundTag.getList("receiver_list", 10);
        for (Tag t : receiverListTag)
        {
            DataResult<GlobalPos> parsed = GlobalPos.CODEC.parse(NbtOps.INSTANCE, t);
            parsed.result().ifPresentOrElse(
                    this.receiverList::add,
                    () -> AE2CrystalScience.LOGGER.error("Failed to parse GlobalPos from receiver_list entry: {}", t)
            );
        }
    }

    public static enum LinkState
    {
        NO_CONTROL, // 发射器网络中无控制器
        GRID_CONFLICT, // 发射器来自不同的网络
        SUCCESS, // 成功连接
        NO_SUCCESS // 未知错误，连接失败
    }
}
