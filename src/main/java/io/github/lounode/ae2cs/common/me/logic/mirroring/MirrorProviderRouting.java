package io.github.lounode.ae2cs.common.me.logic.mirroring;

import appeng.api.config.LockCraftingMode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 镜像使用的可选发料引擎，隔离 AdvancedAE 类型与普通供应器的公共宿主契约。
 *
 * <p>
 * 引擎与镜像具有相同生命周期，只在世界线程调用，并始终使用镜像的位置和朝向。
 * 存在 AdvancedAE 时整台镜像使用同一个引擎，换源不会丢弃在途原料、返回库存或锁状态。
 * 不存储主源样板物品，也不向主源提交发料请求。
 * </p>
 */
public interface MirrorProviderRouting extends ICraftingProvider {

    /** 按当前可达主源刷新样板和输入/输出缓存；源不可达时仅清除可用样板。 */
    void updatePatterns();

    /** 返回引擎的设置视图；有源时读取源的已注册设置，否则读取本地存档设置。 */
    IConfigManager getConfigManager();

    /** 返回镜像独立持有的返回库存，不包含主源库存。 */
    PatternProviderReturnInventory getReturnInv();

    /** 读取镜像的红石或合成结果锁状态，不改变主源。 */
    LockCraftingMode getCraftingLockedReason();

    /** 等待合成结果时返回尚缺的结果；其他状态允许为空。 */
    @Nullable
    GenericStack getUnlockStack();

    /** 在显式解锁时清除镜像的等待状态，并按引擎契约保存变更。 */
    void resetCraftingLock();

    /** 节点状态变化后唤醒镜像自己的待处理工作，不注册替代合成供应器。 */
    void onMainNodeStateChanged();

    /** 邻居红石变化时刷新镜像本地红石锁。 */
    void updateRedstoneState();

    /** 将发料队列、逐原料方向、返回库存和锁写入宿主的供应器 NBT。 */
    void writeToNBT(CompoundTag tag, HolderLookup.Provider registries);

    /** 在节点创建前读取同一格式的运行状态；兼容普通供应器已有的公共 NBT 字段。 */
    void readFromNBT(CompoundTag tag, HolderLookup.Provider registries);

    /** 拆除时收集镜像实际持有的物品；不会掉落主源的样板。 */
    void addDrops(List<ItemStack> drops);

    /** 宿主明确清空时清除镜像实际库存及队列，不影响主源。 */
    void clearContent();
}
