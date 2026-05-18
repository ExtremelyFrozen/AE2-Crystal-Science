package io.github.lounode.ae2cs.api.genericinv;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 方便根据不同需求，对外暴露不同的能力，但始终维护同一套内部仓储
 * <p>
 * 代码来自 Applied Pneumatics
 */
public class GenericStackInvWrapper implements GenericInternalInventory
{
    protected final GenericInternalInventory delegate;

    public GenericStackInvWrapper(GenericInternalInventory delegate)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * 便于子类或外部取出原始对象
     */
    public GenericInternalInventory unwrap()
    {
        return delegate;
    }

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
    public boolean isSupportedType(AEKeyType type)
    {
        return delegate.isSupportedType(type);
    }

    @Override
    public boolean isSupportedType(AEKey what)
    {
        // 虽然接口有 default 实现，这里仍显式转发，确保若底层覆写则走其逻辑
        return delegate.isSupportedType(what);
    }

    @Override
    public boolean isAllowedIn(int slot, AEKey what)
    {
        return delegate.isAllowedIn(slot, what);
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

    @Override
    public void updateSnapshots(TransactionContext transaction) {

    }

    @Override
    public String toString()
    {
        return "GenericStackInvWrapper(" + delegate + ")";
    }
}