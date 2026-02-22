package io.github.lounode.ae2cs.api.genericinv;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 将一个 {@link GenericInternalInventory} 适配成同时实现 {@link MEStorage} 与 {@link GenericInternalInventory} 的包装器。
 *
 * <p>GenericInternalInventory 负责“按槽”的插入/提取与过滤（isAllowedIn 等）。
 * <p>MEStorage 视角下的 insert/extract 会跨槽聚合调用 delegate 的 slot insert/extract。
 * <p>其目的是简化{@link CombinedGenericInternalInventory}的批量插入/取出
 */
public class GenericInvStorageAdapter implements MEStorage, GenericInternalInventory
{
    protected final GenericInternalInventory delegate;
    private Component description = Component.empty();

    public GenericInvStorageAdapter(GenericInternalInventory delegate)
    {
        this(delegate, Component.empty());
    }

    public GenericInvStorageAdapter(GenericInternalInventory delegate, Component description)
    {
        this.delegate = Preconditions.checkNotNull(delegate, "delegate");
        this.description = description == null ? Component.empty() : description;
    }

    // GenericInternalInventory：直接委托
    @Override
    public int size()
    {
        return delegate.size();
    }

    @Override
    public @Nullable GenericStack getStack(int slot)
    {
        return delegate.getStack(slot);
    }

    @Override
    public @Nullable AEKey getKey(int slot)
    {
        return delegate.getKey(slot);
    }

    @Override
    public long getAmount(int slot)
    {
        return delegate.getAmount(slot);
    }

    @Override
    public long getMaxAmount(AEKey key)
    {
        return delegate.getMaxAmount(key);
    }

    @Override
    public long getCapacity(AEKeyType keyType)
    {
        return delegate.getCapacity(keyType);
    }

    @Override
    public boolean canInsert()
    {
        return delegate.canInsert();
    }

    @Override
    public boolean canExtract()
    {
        return delegate.canExtract();
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack newStack)
    {
        delegate.setStack(slot, newStack);
    }

    @Override
    public boolean isAllowed(AEKey aeKey)
    {
        return delegate.isAllowed(aeKey);
    }

    @Override
    public long insert(int slot, AEKey what, long amount, Actionable mode)
    {
        return delegate.insert(slot, what, amount, mode);
    }

    @Override
    public long extract(int slot, AEKey what, long amount, Actionable mode)
    {
        return delegate.extract(slot, what, amount, mode);
    }

    @Override
    public void beginBatch()
    {
        delegate.beginBatch();
    }

    @Override
    public void endBatch()
    {
        delegate.endBatch();
    }

    @Override
    public void endBatchSuppressed()
    {
        delegate.endBatchSuppressed();
    }

    @Override
    public void onChange()
    {
        delegate.onChange();
    }

    // MEStorage:：跨槽聚合

    /**
     * 如果库存中已经存在该 key，则偏好此存储
     * ps：其实我觉得这个函数不太可能被调用
     */
    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source)
    {
        ObjectsCheck.checkPreconditions(what, 0, Actionable.SIMULATE, source);
        for (int i = 0; i < size(); i++)
        {
            AEKey key = getKey(i);
            if (key != null && key.equals(what))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source)
    {
        ObjectsCheck.checkPreconditions(what, amount, mode, source);

        if (amount == 0 || !canInsert() || !isAllowed(what))
        {
            return 0;
        }

        long inserted = 0;
        for (int slot = 0; slot < size() && inserted < amount; slot++)
        {
            long delta = insert(slot, what, amount - inserted, mode);
            if (delta > 0)
            {
                inserted += delta;
            }
        }

        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source)
    {
        ObjectsCheck.checkPreconditions(what, amount, mode, source);

        if (amount == 0 || !canExtract())
        {
            return 0;
        }

        long extracted = 0;
        for (int slot = 0; slot < size() && extracted < amount; slot++)
        {
            AEKey key = getKey(slot);
            if (key == null || !key.equals(what))
            {
                continue;
            }

            long delta = extract(slot, what, amount - extracted, mode);
            if (delta > 0)
            {
                extracted += delta;
            }
        }

        return extracted;
    }


    @Override
    public void getAvailableStacks(KeyCounter out)
    {
        Preconditions.checkNotNull(out, "out");
        for (int i = 0; i < size(); i++)
        {
            GenericStack stack = getStack(i);
            if (stack != null && stack.amount() > 0)
            {
                out.add(stack.what(), stack.amount());
            }
        }
    }

    @Override
    public Component getDescription()
    {
        return description;
    }

    public void setDescription(Component description)
    {
        this.description = description == null ? Component.empty() : description;
    }

    public GenericInternalInventory getDelegate()
    {
        return delegate;
    }

    /**
     * 小工具：复用 MEStorage 的前置条件检查风格，但不强依赖 AE 的静态方法（避免包依赖/可拷贝）。
     */
    private static final class ObjectsCheck
    {
        static void checkPreconditions(AEKey what, long amount, Actionable mode, IActionSource source)
        {
            Preconditions.checkNotNull(what, "what");
            Preconditions.checkNotNull(mode, "mode");
            Preconditions.checkNotNull(source, "source");
            Preconditions.checkArgument(amount >= 0, "amount >= 0");
        }
    }
}