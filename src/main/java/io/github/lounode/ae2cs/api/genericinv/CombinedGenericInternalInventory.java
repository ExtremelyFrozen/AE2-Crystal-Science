package io.github.lounode.ae2cs.api.genericinv;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

/**
 * 多个 GenericInternalInventory 实例的组合视图。
 * 槽位寻址被扁平化： [0..size0) 映射到 inv0， [size0..size0+size1) 映射到 inv1，依此类推。
 * <p>
 * getCapacity(type) 返回所有委托库存容量的饱和求和。
 * getMaxAmount(key) 返回所有委托库存中 getMaxAmount(key) 的最大值。
 * 批处理与变更通知会传播到所有委托库存。
 * <p>
 * 代码来自 Applied Pneumatics
 */
public class CombinedGenericInternalInventory implements GenericInternalInventory
{

    protected final GenericInternalInventory[] delegates; // 底层库存
    protected final int[] baseIndex; // 各库存尺寸的前缀和（用于槽位映射）
    protected final int slotCount;   // 总槽位数量

    public CombinedGenericInternalInventory(GenericInternalInventory... delegates)
    {
        this.delegates = delegates == null ? new GenericInternalInventory[0] : delegates.clone();
        this.baseIndex = new int[this.delegates.length];
        int sum = 0;
        for (int i = 0; i < this.delegates.length; i++)
        {
            GenericInternalInventory inv = this.delegates[i];
            int sz = inv == null ? 0 : inv.size();
            sum += sz;
            baseIndex[i] = sum;
        }
        this.slotCount = sum;
    }

    // === 槽位映射辅助（思路同 CombinedInvWrapper） ===

    // 根据扁平槽位返回对应的委托库存索引；非法返回 -1。
    protected int getIndexForSlot(int slot)
    {
        if (slot < 0) return -1;
        for (int i = 0; i < baseIndex.length; i++)
        {
            if (slot - baseIndex[i] < 0)
            {
                return i;
            }
        }
        return -1;
    }

    // 通过索引返回委托库存；越界时返回空实现。
    protected GenericInternalInventory getInventoryFromIndex(int index)
    {
        if (index < 0 || index >= delegates.length)
        {
            return EmptyInventory.INSTANCE;
        }
        GenericInternalInventory inv = delegates[index];
        return inv != null ? inv : EmptyInventory.INSTANCE;
    }

    // 将扁平槽位映射为该索引对应委托库存中的本地槽位
    protected int getLocalSlot(int slot, int index)
    {
        if (index <= 0 || index >= baseIndex.length)
        {
            return slot;
        }
        return slot - baseIndex[index - 1];
    }

    // === GenericInternalInventory 接口实现 ===

    @Override
    public int size()
    {
        return slotCount;
    }

    @Override
    public @Nullable GenericStack getStack(int slot)
    {
        int idx = getIndexForSlot(slot);
        GenericInternalInventory inv = getInventoryFromIndex(idx);
        int local = getLocalSlot(slot, idx);
        return inv.getStack(local);
    }

    @Override
    public @Nullable AEKey getKey(int slot)
    {
        int idx = getIndexForSlot(slot);
        GenericInternalInventory inv = getInventoryFromIndex(idx);
        int local = getLocalSlot(slot, idx);
        return inv.getKey(local);
    }

    @Override
    public long getAmount(int slot)
    {
        int idx = getIndexForSlot(slot);
        GenericInternalInventory inv = getInventoryFromIndex(idx);
        int local = getLocalSlot(slot, idx);
        return inv.getAmount(local);
    }

    /**
     * 对给定 key 返回“每槽位最大可容纳量”的上界（跨所有委托取最大）。
     * （该方法不包含具体槽位的上下文；这里取任一底层槽位可能支持的最大能力值。）
     */
    @Override
    public long getMaxAmount(AEKey key)
    {
        if (key == null) return 0L;
        long max = 0L;
        for (GenericInternalInventory inv : delegates)
        {
            if (inv == null) continue;
            long v = inv.getMaxAmount(key);
            if (v > max) max = v;
            if (max == Long.MAX_VALUE) break;
        }
        return Math.max(0L, max);
    }

    /**
     * 对给定类型返回所有委托库存容量的饱和求和。
     */
    @Override
    public long getCapacity(AEKeyType keyType)
    {
        if (keyType == null) return 0L;
        long sum = 0L;
        for (GenericInternalInventory inv : delegates)
        {
            if (inv == null) continue;
            long c = inv.getCapacity(keyType);
            if (c < 0) c = 0; // 防御性处理
            sum = saturatingAdd(sum, c);
            if (sum == Long.MAX_VALUE) break;
        }
        return sum;
    }

    @Override
    public boolean canInsert()
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null && inv.canInsert()) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract()
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null && inv.canExtract()) return true;
        }
        return false;
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack newStack)
    {
        int idx = getIndexForSlot(slot);
        GenericInternalInventory inv = getInventoryFromIndex(idx);
        int local = getLocalSlot(slot, idx);
        inv.setStack(local, newStack);
    }

    @Override
    public boolean isAllowed(AEKey aeKey)
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null && inv.isAllowed(aeKey)) return true;
        }
        return false;
    }

    @Override
    public long insert(int slot, AEKey what, long amount, Actionable mode)
    {
        int idx = getIndexForSlot(slot);
        GenericInternalInventory inv = getInventoryFromIndex(idx);
        int local = getLocalSlot(slot, idx);
        return inv.insert(local, what, amount, mode);
    }

    @Override
    public long extract(int slot, AEKey what, long amount, Actionable mode)
    {
        int idx = getIndexForSlot(slot);
        GenericInternalInventory inv = getInventoryFromIndex(idx);
        int local = getLocalSlot(slot, idx);
        return inv.extract(local, what, amount, mode);
    }

    @Override
    public void beginBatch()
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null) inv.beginBatch();
        }
    }

    @Override
    public void endBatch()
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null) inv.endBatch();
        }
    }

    @Override
    public void endBatchSuppressed()
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null) inv.endBatchSuppressed();
        }
    }

    @Override
    public void onChange()
    {
        for (GenericInternalInventory inv : delegates)
        {
            if (inv != null) inv.onChange();
        }
    }

    // === 辅助方法 ===

    private static long saturatingAdd(long a, long b)
    {
        // 两者预期为非负；发生溢出时饱和到 Long.MAX_VALUE
        long limit = Long.MAX_VALUE - a;
        if (b > limit) return Long.MAX_VALUE;
        long r = a + b;
        return r < 0 ? Long.MAX_VALUE : r;
    }

    // 当槽位/委托索引非法时使用的最小空库存，确保调用安全。
    private static final class EmptyInventory implements GenericInternalInventory
    {
        static final EmptyInventory INSTANCE = new EmptyInventory();

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public @Nullable GenericStack getStack(int slot)
        {
            return null;
        }

        @Override
        public @Nullable AEKey getKey(int slot)
        {
            return null;
        }

        @Override
        public long getAmount(int slot)
        {
            return 0;
        }

        @Override
        public long getMaxAmount(AEKey key)
        {
            return 0;
        }

        @Override
        public long getCapacity(AEKeyType keyType)
        {
            return 0;
        }

        @Override
        public boolean canInsert()
        {
            return false;
        }

        @Override
        public boolean canExtract()
        {
            return false;
        }

        @Override
        public void setStack(int slot, @Nullable GenericStack newStack)
        {
        }

        @Override
        public boolean isAllowed(AEKey aeKey)
        {
            return false;
        }

        @Override
        public long insert(int slot, AEKey what, long amount, Actionable mode)
        {
            return 0;
        }

        @Override
        public long extract(int slot, AEKey what, long amount, Actionable mode)
        {
            return 0;
        }

        @Override
        public void beginBatch()
        {
        }

        @Override
        public void endBatch()
        {
        }

        @Override
        public void endBatchSuppressed()
        {
        }

        @Override
        public void onChange()
        {
        }
    }
}