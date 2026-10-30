package io.github.lounode.ae2cs.common.me.logic.mirroring;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.util.AEColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 在节点创建前接入可选引擎；保留镜像自身的合成供应器服务，只替换实际工作的 ticker。
 * 两套逻辑均在构造阶段完成初始化，运行中不会再改变真实节点的服务或 flags。
 */
public final class MirrorProviderGridNode implements IManagedGridNode {

    private final IManagedGridNode delegate;
    private @Nullable RoutingTicker ticker;
    private @Nullable IStackWatcher craftingWatcher;
    private boolean craftingProviderRegistered;

    public MirrorProviderGridNode(IManagedGridNode delegate) {
        this.delegate = delegate;
    }

    public @Nullable IStackWatcher getCraftingWatcher() {
        return craftingWatcher;
    }

    @Override
    public <T extends IGridNodeService> IManagedGridNode addService(Class<T> serviceClass, T service) {
        if (serviceClass == IGridTickable.class) {
            if (ticker == null) {
                ticker = new RoutingTicker((IGridTickable) service);
                delegate.addService(IGridTickable.class, ticker);
            } else {
                ticker.delegate = (IGridTickable) service;
            }
        } else if (serviceClass == ICraftingProvider.class) {
            if (!craftingProviderRegistered) {
                delegate.addService(serviceClass, service);
                craftingProviderRegistered = true;
            }
        } else if (serviceClass == ICraftingWatcherNode.class) {
            ICraftingWatcherNode watcher = (ICraftingWatcherNode) service;
            delegate.addService(ICraftingWatcherNode.class, new ICraftingWatcherNode() {

                @Override
                public void updateWatcher(IStackWatcher newWatcher) {
                    craftingWatcher = newWatcher;
                    watcher.updateWatcher(newWatcher);
                }

                @Override
                public void onRequestChange(AEKey what) {
                    watcher.onRequestChange(what);
                }

                @Override
                public void onCraftableChange(AEKey what) {
                    watcher.onCraftableChange(what);
                }
            });
        } else {
            delegate.addService(serviceClass, service);
        }
        return this;
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public void create(Level level, @Nullable BlockPos blockPos) {
        delegate.create(level, blockPos);
    }

    @Override
    public void loadFromNBT(CompoundTag nodeData) {
        delegate.loadFromNBT(nodeData);
    }

    @Override
    public void saveToNBT(CompoundTag nodeData) {
        delegate.saveToNBT(nodeData);
    }

    @Override
    public IManagedGridNode setFlags(GridFlags... flags) {
        delegate.setFlags(flags);
        return this;
    }

    @Override
    public IManagedGridNode setExposedOnSides(Set<Direction> directions) {
        delegate.setExposedOnSides(directions);
        return this;
    }

    @Override
    public IManagedGridNode setIdlePowerUsage(double usagePerTick) {
        delegate.setIdlePowerUsage(usagePerTick);
        return this;
    }

    @Override
    public IManagedGridNode setVisualRepresentation(@Nullable AEItemKey visualRepresentation) {
        delegate.setVisualRepresentation(visualRepresentation);
        return this;
    }

    @Override
    public IManagedGridNode setInWorldNode(boolean accessible) {
        delegate.setInWorldNode(accessible);
        return this;
    }

    @Override
    public IManagedGridNode setTagName(String tagName) {
        delegate.setTagName(tagName);
        return this;
    }

    @Override
    public IManagedGridNode setGridColor(AEColor gridColor) {
        delegate.setGridColor(gridColor);
        return this;
    }

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

    @Override
    public boolean isOnline() {
        return delegate.isOnline();
    }

    @Override
    public boolean isPowered() {
        return delegate.isPowered();
    }

    @Override
    public boolean hasGridBooted() {
        return delegate.hasGridBooted();
    }

    @Override
    public void setOwningPlayerId(int ownerPlayerId) {
        delegate.setOwningPlayerId(ownerPlayerId);
    }

    @Override
    public void setOwningPlayer(Player ownerPlayer) {
        delegate.setOwningPlayer(ownerPlayer);
    }

    @Override
    public @Nullable IGridNode getNode() {
        return delegate.getNode();
    }

    private static final class RoutingTicker implements IGridTickable {

        private IGridTickable delegate;

        private RoutingTicker(IGridTickable delegate) {
            this.delegate = delegate;
        }

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return delegate.getTickingRequest(node);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            return delegate.tickingRequest(node, ticksSinceLastCall);
        }
    }
}
