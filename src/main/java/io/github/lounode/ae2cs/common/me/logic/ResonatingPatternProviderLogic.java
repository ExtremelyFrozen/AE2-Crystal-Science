package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Setting;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigManager;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import io.github.lounode.ae2cs.api.settings.PullMode;
import io.github.lounode.ae2cs.api.util.GenericStackInvHelper;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.common.me.crafting.ResonatingPatternDetails;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 谐振样板供应器，对于谐振样板，尝试将标记了位置的原料发送到指定位置
 */
public class ResonatingPatternProviderLogic extends PatternProviderLogic implements IUpgradeableObject
{
    private final IActionSource actionSource;
    private final PatternProviderLogicHost host;
    private final IManagedGridNode mainNode;

    /**
     * 带目的地的待发送队列，若一次未能完全发送，则缓存在此，下次尝试
     */
    private final List<PendingSend> resonatingSendList = new ArrayList<>();

    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK, 1, this::onUpgradesChange);

    /**
     * 用于未标记目的地发送材料的round-robin
     */
    private int localRoundRobinIndex = 0;

    public ResonatingPatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host)
    {
        this(mainNode, host, 9);
    }

    public ResonatingPatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize)
    {
        super(mainNode, host, patternInventorySize);

        this.host = host;
        this.mainNode = mainNode.addService(IGridTickable.class, new ResonatingTicker());
        this.actionSource = new MachineSource(mainNode::getNode);

        if (getConfigManager() instanceof ConfigManager cm)
        {
            cm.registerSetting(AECSSettings.PULL_MODE, PullMode.PULL_OFF);
        }
    }

    public boolean isEnablePull()
    {
        return getConfigManager().getSetting(AECSSettings.PULL_MODE) == PullMode.PULL_ON;
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    @Override
    protected void configChanged(IConfigManager manager, Setting<?> setting)
    {
        super.configChanged(manager, setting);
        if (setting == AECSSettings.PULL_MODE)
        {
            host.saveChanges();
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    private void onUpgradesChange()
    {
        this.saveChanges();
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.writeToNBT(tag, registries);

        this.upgrades.writeToNBT(tag, "upgrades", registries);

        var list = new ListTag();
        for (var p : resonatingSendList)
        {
            var e = new CompoundTag();
            e.putString("dim", p.target().pos().dimension().location().toString());
            e.putLong("pos", p.target().pos().pos().asLong());
            e.putByte("face", (byte) p.target().face().get3DDataValue());
            e.put("stack", GenericStack.writeTag(registries, p.stack()));
            list.add(e);
        }
        tag.put("resonating_send_list", list);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.readFromNBT(tag, registries);

        this.upgrades.readFromNBT(tag, "upgrades", registries);

        resonatingSendList.clear();
        if (!tag.contains("resonating_send_list", Tag.TAG_LIST))
        {
            return;
        }

        var list = tag.getList("resonating_send_list", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            var e = list.getCompound(i);

            var dimStr = e.getString("dim");
            var posLong = e.getLong("pos");
            var face = Direction.from3DDataValue(e.getByte("face"));

            var stack = GenericStack.readTag(registries, e.getCompound("stack"));
            if (stack == null || stack.amount() <= 0)
            {
                continue;
            }

            try
            {
                var rl = Identifier.parse(dimStr);
                var dimKey = ResourceKey.create(Registries.DIMENSION, rl);
                var gp = GlobalPos.of(dimKey, BlockPos.of(posLong));
                resonatingSendList.add(new PendingSend(new EncodedResonatingPattern.Target(gp, face), stack));
            }
            catch (Exception ex)
            {
                AE2CrystalScience.LOGGER.warn("Failed to read pending resonating send entry from NBT: {}", e, ex);
            }
        }
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder)
    {
        // 非谐振走原版
        if (!(patternDetails instanceof ResonatingPatternDetails resonating))
        {
            return super.pushPattern(patternDetails, inputHolder);
        }

        // 类原版拒绝条件
        if (super.isBusy() || !resonatingSendList.isEmpty() || !this.mainNode.isActive()
                || !getAvailablePatterns().contains(patternDetails))
        {
            return false;
        }

        // 原版锁定逻辑
        if (getCraftingLockedReason() != LockCraftingMode.NONE)
        {
            return false;
        }

        var be = host.getBlockEntity();
        var level = be.getLevel();
        if (level == null)
        {
            return false;
        }

        // 复制用于unmarked fallback
        KeyCounter[] remaining = copyKeyCounters(inputHolder);

        // 收集marked，并从remaining扣掉对应数量
        record Marked(AEKey key, long amount, EncodedResonatingPattern.Target target)
        {
        }
        var marked = new ArrayList<Marked>();

        var sparseInputs = resonating.getSparseInputs();
        for (int sparseIndex = 0; sparseIndex < sparseInputs.size(); sparseIndex++)
        {
            var sparse = sparseInputs.get(sparseIndex);
            if (sparse == null) continue;

            var optTarget = resonating.getTargetForSparseInputIndex(sparseIndex);
            if (optTarget.isEmpty())
            {
                continue; // 无target：留给unmarked fallback
            }

            var target = optTarget.get();
            var key = sparse.what();
            var amount = sparse.amount();

            if (!removeFromRemaining(remaining, key, amount))
            {
                return false;
            }

            marked.add(new Marked(key, amount, target));
        }

        // 预先尝试marked材料是否能完整发配
        for (var m : marked)
        {
            var adapter = findTarget(m.target());
            if (adapter == null) return false;

            // 阻挡模式
            if (isBlockedByMode(adapter)) return false;

            long simulated = adapter.insert(m.key(), m.amount(), Actionable.SIMULATE);
            if (simulated < m.amount()) return false;
        }

        // remaining存在，则按照原版逻辑模拟发配
        Direction chosenDirection = null;
        PatternProviderTarget chosenAdapter = null;

        if (!isEmpty(remaining))
        {
            if (!patternDetails.supportsPushInputsToExternalInventory())
            {
                return false;
            }

            record PushTarget(Direction direction, PatternProviderTarget target)
            {
            }
            var possibleTargets = new ArrayList<PushTarget>();

            for (var dir : getActiveSidesFiltered())
            {
                var adjPos = be.getBlockPos().relative(dir);
                var adjSide = dir.getOpposite();

                var adapter = PatternProviderTarget.get(level, adjPos, null, adjSide, actionSource);
                if (adapter == null) continue;

                if (this.isBlocking() && adapter.containsPatternInput(this.patternInputs))
                {
                    continue;
                }

                possibleTargets.add(new PushTarget(dir, adapter));
            }

            rearrangeRoundRobin(possibleTargets);

            for (int i = 0; i < possibleTargets.size(); i++)
            {
                var p = possibleTargets.get(i);

                if (adapterAcceptsAllLocal(p.target(), remaining))
                {
                    chosenDirection = p.direction();
                    chosenAdapter = p.target();
                    localRoundRobinIndex += i + 1;
                    break;
                }
            }

            if (chosenAdapter == null)
            {
                return false;
            }
        }

        // 正式执行插入
        for (var m : marked)
        {
            var adapter = findTarget(m.target());
            if (adapter == null)
            {
                return false;
            }

            long inserted = adapter.insert(m.key(), m.amount(), Actionable.MODULATE);
            if (inserted < m.amount())
            {
                var left = m.amount() - inserted;
                resonatingSendList.add(new PendingSend(m.target(), new GenericStack(m.key(), left)));
                this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
            }
        }

        // 无目的地材料走原版
        if (chosenAdapter != null)
        {
            final var adapterFinal = chosenAdapter;

            patternDetails.pushInputsToExternalInventory(remaining, (what, amount) -> {
                var inserted = adapterFinal.insert(what, amount, Actionable.MODULATE);
                if (inserted < amount)
                {
                    addToSendList(what, amount - inserted);
                }
            });

            this.sendDirection = chosenDirection;
        }

        // 成功事件/锁定逻辑沿用原版
        onPushPatternSuccess(patternDetails);

        // 立刻尝试把缓存送出
        boolean didSomething = false;
        didSomething |= sendStacksOut();
        didSomething |= sendResonatingStacksOut();

        if (didSomething)
        {
            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }

        return true;
    }

    @Override
    public boolean isBusy()
    {
        return super.isBusy() || !resonatingSendList.isEmpty();
    }

    /**
     * 从周围面抽取资源进入返回仓
     */
    private boolean doPullWork()
    {
        if (!isEnablePull()) return false;

        var hostBe = host.getBlockEntity();
        var hostLevelRaw = hostBe.getLevel();
        if (!(hostLevelRaw instanceof ServerLevel hostLevel)) return false;

        if (!mainNode.isActive()) return false;

        var returnInv = getReturnInv();

        var sides = getActiveSidesFiltered();
        if (sides.isEmpty()) return false;

        // 扫描限流
        final int maxKeysPerTick = 32;
        int scanned = 0;

        for (var dir : sides)
        {
            var adjPos = hostBe.getBlockPos().relative(dir);
            var adjSide = dir.getOpposite();

            if (!hostLevel.hasChunkAt(adjPos)) continue;

            var ext = GenericStackInvHelper.getAdjacentMeStorage(hostLevel, adjPos, null, adjSide);
            if (ext == null) continue;

            for (var stack : ext.getAvailableStacks())
            {
                if (scanned++ >= maxKeysPerTick) return false;

                var key = stack.getKey();
                long available = stack.getLongValue();
                if (available <= 0) continue;

                long request = Math.min(available, 4000);

                long canBuffer = returnInv.insert(key, request, Actionable.SIMULATE, actionSource);
                if (canBuffer <= 0) continue;

                long extracted = ext.extract(key, canBuffer, Actionable.MODULATE, actionSource);
                if (extracted <= 0) continue;

                long buffered = returnInv.insert(key, extracted, Actionable.MODULATE, actionSource);

                long leftover = extracted - buffered;
                if (leftover > 0)
                {
                    ext.insert(key, leftover, Actionable.MODULATE, actionSource);
                }
                return true;
            }
        }
        return false;
    }


    // -------------------辅助方法-------------------------

    private boolean isBlockedByMode(PatternProviderTarget adapter)
    {
        return this.isBlocking() && adapter.containsPatternInput(this.patternInputs);
    }

    private static KeyCounter[] copyKeyCounters(KeyCounter[] inputHolder)
    {
        var out = new KeyCounter[inputHolder.length];
        for (int i = 0; i < inputHolder.length; i++)
        {
            out[i] = new KeyCounter();
            out[i].addAll(inputHolder[i]);
        }
        return out;
    }

    private static boolean removeFromRemaining(KeyCounter[] remaining, AEKey key, long amount)
    {
        long toRemove = amount;

        for (var counter : remaining)
        {
            long avail = counter.get(key);
            if (avail <= 0) continue;

            long take = Math.min(avail, toRemove);
            counter.remove(key, take);
            toRemove -= take;

            if (toRemove <= 0)
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isEmpty(KeyCounter[] holders)
    {
        for (var h : holders)
        {
            for (var entry : h)
            {
                if (entry.getLongValue() > 0)
                {
                    return false;
                }
            }
        }
        return true;
    }


    private static <T> void rearrangeRoundRobin(List<T> list, int roundRobinIndex)
    {
        if (list.isEmpty()) return;

        int idx = Math.floorMod(roundRobinIndex, list.size());
        if (idx == 0) return;

        var head = new ArrayList<>(list.subList(0, idx));
        list.subList(0, idx).clear();
        list.addAll(head);
    }

    private <T> void rearrangeRoundRobin(List<T> list)
    {
        rearrangeRoundRobin(list, localRoundRobinIndex);
    }

    private boolean adapterAcceptsAllLocal(PatternProviderTarget target, KeyCounter[] inputHolder)
    {
        for (var inputList : inputHolder)
        {
            for (var input : inputList)
            {
                var inserted = target.insert(input.getKey(), input.getLongValue(), Actionable.SIMULATE);
                if (inserted == 0)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 复刻父类getActiveSides的过滤逻辑
     */
    private Set<Direction> getActiveSidesFiltered()
    {
        var sides = EnumSet.copyOf(host.getTargets());

        var node = mainNode.getNode();
        if (node != null)
        {
            for (var entry : node.getInWorldConnections().entrySet())
            {
                var otherNode = entry.getValue().getOtherSide(node);
                if (otherNode.getOwner() instanceof PatternProviderLogicHost
                        || (otherNode.getOwner() instanceof InterfaceLogicHost
                        && otherNode.getGrid().equals(mainNode.getGrid())))
                {
                    sides.remove(entry.getKey());
                }
            }
        }

        return sides;
    }

    @Nullable
    private ServerLevel getTargetLevel(EncodedResonatingPattern.Target target)
    {
        var hostLevel = host.getBlockEntity().getLevel();
        if (!(hostLevel instanceof ServerLevel)) return null;

        var server = hostLevel.getServer();

        return server.getLevel(target.pos().dimension());
    }

    private boolean isTargetChunkLoaded(@NotNull ServerLevel level, BlockPos pos)
    {
        return level.hasChunkAt(pos);
    }

    @Nullable
    private PatternProviderTarget findTarget(EncodedResonatingPattern.Target target)
    {
        var targetLevel = getTargetLevel(target);
        if (targetLevel == null) return null;

        var pos = target.pos().pos();
        if (!isTargetChunkLoaded(targetLevel, pos)) return null;

        return PatternProviderTarget.get(targetLevel, pos, null, target.face(), actionSource);
    }

    private boolean hasResonatingWorkToDo()
    {
        return !resonatingSendList.isEmpty();
    }

    private boolean sendResonatingStacksOut()
    {
        if (resonatingSendList.isEmpty()) return false;

        boolean did = false;

        for (var it = resonatingSendList.listIterator(); it.hasNext(); )
        {
            var pending = it.next();

            var adapter = findTarget(pending.target());
            if (adapter == null)
            {
                continue; // 维度不存在/区块未加载/能力不存在
            }

            // 阻挡模式
            if (isBlockedByMode(adapter))
            {
                continue;
            }

            var what = pending.stack().what();
            var amount = pending.stack().amount();
            var inserted = adapter.insert(what, amount, Actionable.MODULATE);

            if (inserted >= amount)
            {
                it.remove();
                did = true;
            }
            else if (inserted > 0)
            {
                it.set(new PendingSend(pending.target(), new GenericStack(what, amount - inserted)));
                did = true;
            }
        }

        return did;
    }

    private boolean doResonatingWork()
    {
        return sendResonatingStacksOut();
    }

    @Override
    public void addDrops(List<ItemStack> drops)
    {
        super.addDrops(drops);
        for (ItemStack stack : upgrades)
        {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        upgrades.clear();
    }

    private class ResonatingTicker implements IGridTickable
    {
        @Override
        public TickingRequest getTickingRequest(IGridNode node)
        {
            boolean idle = !hasWorkToDo() && !hasResonatingWorkToDo() && !isEnablePull();
            return new TickingRequest(TickRates.Interface, idle);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall)
        {
            if (!mainNode.isActive())
            {
                return TickRateModulation.SLEEP;
            }

            boolean could = doWork() | doResonatingWork() | doPullWork();
            boolean has = hasWorkToDo() || hasResonatingWorkToDo() || isEnablePull();

            return has ? (could ? TickRateModulation.URGENT : TickRateModulation.SLOWER)
                    : TickRateModulation.SLEEP;
        }
    }

    private record PendingSend(EncodedResonatingPattern.Target target, GenericStack stack)
    {
    }
}