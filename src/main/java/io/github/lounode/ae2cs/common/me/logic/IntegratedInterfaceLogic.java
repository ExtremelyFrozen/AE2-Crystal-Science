package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.common.init.AECSBlocks;

import appeng.api.config.*;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.settings.TickRates;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.helpers.patternprovider.UnlockCraftingEvent;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.PlayerInternalInventory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IntegratedInterfaceLogic implements IConfigurableObject, IUpgradeableObject,
                                      ICraftingProvider, ICraftingRequester {

    /**
     * 实际载体，可能是特定be，也可能是ae的线缆部件
     */
    private final IntegratedInterfaceHost host;

    /**
     * 受管节点
     */
    private final IManagedGridNode mainNode;

    /**
     * 标记仓
     */
    private final ConfigInventory configInv;

    /**
     * 返回兼存储仓
     */
    private final ConfigInventory storage;

    /**
     * 样板槽 - 只允许UI存取
     */
    private final AppEngInternalInventory patternInventory;

    /**
     * 解析后的样板信息
     */
    private final List<IPatternDetails> patterns = new ArrayList<>();

    /**
     * 接口配置
     */
    private final IConfigManager configManager;

    /**
     * 升级槽 - 允许UI以及右键机器
     */
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.INTEGRATED_INTERFACE_BLOCK, 1, this::onUpgradesChanged);

    /**
     * 包含了所有样板输入项的集合，用来给阻塞模式快速判断
     */
    private final Set<AEKey> patternInputs = new HashSet<>();

    /**
     * 等待发送的资源列表
     */
    private final List<GenericStack> sendList = new ArrayList<>();

    /**
     * 下一次发生会尝试向哪个方向？
     */
    private Direction sendDirection;

    /**
     * 用于样板推送时的轮询索引（round-robin），每次成功向某个目标推送后递增，用来决定下次从哪个目标开始尝试，避免总是优先同一个目标。
     */
    private int roundRobinIndex = 0;

    /**
     * 缓存当前的红石状态：YES = 有红石信号、NO = 无红石信号、UNDECIDED = 未检测 / 需要重新检测（懒加载用）
     * <p>
     * 主要用于 LOCK_WHILE_LOW / LOCK_WHILE_HIGH / LOCK_UNTIL_PULSE 这几种锁定模式。
     */
    private YesNo redstoneState = YesNo.UNDECIDED;

    /**
     * 当前正在等待的解锁事件类型：
     * <p>
     * - null：没有处于“等待解锁”的状态；
     * <p>
     * - REDSTONE_POWER / REDSTONE_PULSE：用于 LOCK_UNTIL_PULSE；
     * <p>
     * - RESULT：用于 LOCK_UNTIL_RESULT，等待特定合成结果回到网络中。
     */
    @Nullable
    private UnlockCraftingEvent unlockEvent;

    /**
     * 在 LOCK_UNTIL_RESULT 模式下，记录需要“回到 ME 网络”的目标输出及其剩余数量。数量为0或以下时视为“结果已返回”，解除锁定。
     */
    @Nullable
    private GenericStack unlockStack;

    /**
     * 每个方块面的 PatternProviderTarget 缓存：
     * <p>
     * 索引 = side.get3DDataValue()（0~5，对应 6 个方向）。
     * <p>
     * 用于快速找到某一面的“外部目标”（如机器 / 容器），
     * <p>
     * 避免每次 pushPattern 都重新扫描世界方块。
     */
    private final PatternProviderTargetCache[] targetCaches = new PatternProviderTargetCache[6];

    /**
     * 此方块实体在 AE 网络中的动作来源
     */
    private final IActionSource actionSource;

    /**
     * 用于跟踪通过“合成卡”发起的自动合成任务：
     * <p>
     * - 为每个槽位维护一个 ICraftingLink；
     * <p>
     * - 负责在任务完成时把产物插回 storage；
     * <p>
     * - 也支持取消所有挂起任务（移除合成卡时）。
     */
    private final MultiCraftingTracker craftingTracker;

    /**
     * 计划要执行的工作（和 InterfaceLogic 一样：>0 拉货，<0 退货）
     */
    private final GenericStack[] plannedWork;

    /**
     * 是否配置了备货内容（有 config 的话对外暴露本地库存，没有的话暴露网络）
     */
    private boolean hasConfig = false;

    /**
     * 本地库存的 MEStorage 视图，用于给存储总线看
     */
    @Nullable
    private GenericStackInv localStorageView;

    private final int storageSize;

    private final int patternSize;

    public IntegratedInterfaceLogic(IManagedGridNode mainNode, IntegratedInterfaceHost host, int storageSize, int patternSize) {
        this.host = host;

        this.mainNode = mainNode.setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
                .addService(ICraftingProvider.class, this) // 可以提供样板（样板供应器）
                .addService(ICraftingRequester.class, this) // 可以请求合成（合成卡请求）
                .addService(IGridTickable.class, new Ticker()); // tick行为

        configManager = IConfigManager.builder(this::onConfigChanged)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
                .registerSetting(Settings.BLOCKING_MODE, YesNo.NO)
                .registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES)
                .registerSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE)
                .build();

        this.storageSize = storageSize;
        this.patternSize = patternSize;

        this.configInv = ConfigInventory.configStacks(storageSize)
                .changeListener(this::onConfigRowChanged)
                .build();
        this.storage = ConfigInventory.storage(storageSize)
                .slotFilter(this::isAllowedInStorageSlot)
                .changeListener(this::onStorageChanged)
                .build();
        this.plannedWork = new GenericStack[storageSize];

        this.patternInventory = new AppEngInternalInventory(null, patternSize, 1) {

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return super.isItemValid(slot, stack) && stack.getItem() instanceof EncodedPatternItem;
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                updatePatterns();
            }
        };

        this.actionSource = new MachineSource(mainNode::getNode);
        this.craftingTracker = new MultiCraftingTracker(this, storageSize);

        // 为GenericStackInv的单槽位容量使用AE默认注册容量
        this.configInv.useRegisteredCapacities();
        this.storage.useRegisteredCapacities();
    }

    public @Nullable GenericStack getUnlockStack() {
        return unlockStack;
    }

    // IConfigurableObject
    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    // IUpgradeableObject
    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    // ICraftingProvider
    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    @Override
    public int getPatternPriority() {
        return this.host.getPriority();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!sendList.isEmpty() || !this.mainNode.isActive() || !this.patterns.contains(patternDetails)) {
            return false;
        }

        BlockEntity blockEntity = host.getBlockEntity();
        Level level = blockEntity.getLevel();

        if (level == null) return false;

        if (getCraftingLockedReason() != LockCraftingMode.NONE) return false;

        record PushTarget(Direction direction, PatternProviderTarget target) {}
        ArrayList<PushTarget> possibleTargets = new ArrayList<>();

        // 首先尝试给ICraftingMachine推送合成
        // 如果没有，则再找可能的目标，并加入possibleTargets
        for (Direction direction : getActiveSides()) {
            BlockPos adjPos = blockEntity.getBlockPos().relative(direction);
            Direction adjBeSide = direction.getOpposite();

            ICraftingMachine craftingMachine = ICraftingMachine.of(level, adjPos, adjBeSide);
            if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                if (craftingMachine.pushPattern(patternDetails, inputHolder, adjBeSide)) {
                    onPushPatternSuccess(patternDetails);
                    return true;
                }
                continue;
            }

            PatternProviderTarget adapter = findAdapter(direction);
            if (adapter == null)
                continue;

            possibleTargets.add(new PushTarget(direction, adapter));
        }

        // 如果没有合适的ICraftingMachine，且此配方并不支持外部仓库运行，则到此停止
        if (!patternDetails.supportsPushInputsToExternalInventory()) {
            return false;
        }

        // 再安排轮询顺序
        rearrangeRoundRobin(possibleTargets);

        // 向其他类型的方块推送样板
        for (int i = 0; i < possibleTargets.size(); ++i) {
            PushTarget target = possibleTargets.get(i);
            Direction direction = target.direction();
            PatternProviderTarget adapter = target.target();

            // 如果开启了阻塞模式，且对方包含我们的某个样板输入
            if (this.isBlocking() && adapter.containsPatternInput(this.patternInputs)) {
                continue;
            }

            // 只要目标能接收样板中所有种类的输入，我们就推送
            // 无论本次是否真的完全推送了所有资源，都视为完成，所有未完成资源添加到sendList，等待其他逻辑重新推送
            if (this.adapterAcceptsAll(adapter, inputHolder)) {
                patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                    long inserted = adapter.insert(what, amount, Actionable.MODULATE);
                    if (inserted < amount) {
                        this.addToSendList(what, amount - inserted);
                    }
                });
                onPushPatternSuccess(patternDetails);
                this.sendDirection = direction;
                this.sendStacksOut(); // 在这里立刻对外发送一次sendList中的东西
                roundRobinIndex += i + 1;
                return true;
            }
        }

        return false;
    }

    /**
     * 重新解析样板信息，并通知ae网络获取
     */
    public void updatePatterns() {
        patterns.clear();
        patternInputs.clear();

        BlockEntity blockEntity = host.getBlockEntity();

        for (ItemStack stack : this.patternInventory) {
            IPatternDetails details = PatternDetailsHelper.decodePattern(stack, blockEntity.getLevel());

            if (details != null) {
                patterns.add(details);

                for (IPatternDetails.IInput iinput : details.getInputs()) {
                    for (GenericStack inputCandidate : iinput.getPossibleInputs()) {
                        patternInputs.add(inputCandidate.what().dropSecondary());
                    }
                }
            }
        }

        ICraftingProvider.requestUpdate(mainNode);
    }

    /**
     * 获取当前合成供应被锁定的原因
     *
     * @return 如果未被锁定则返回null
     */
    public LockCraftingMode getCraftingLockedReason() {
        var lockMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        if (lockMode == LockCraftingMode.LOCK_WHILE_LOW && !getRedstoneState()) {
            // Crafting locked by redstone signal
            return LockCraftingMode.LOCK_WHILE_LOW;
        } else if (lockMode == LockCraftingMode.LOCK_WHILE_HIGH && getRedstoneState()) {
            return LockCraftingMode.LOCK_WHILE_HIGH;
        } else if (unlockEvent != null) {
            // Crafting locked by waiting for unlock event
            switch (unlockEvent) {
                case REDSTONE_POWER, REDSTONE_PULSE -> {
                    return LockCraftingMode.LOCK_UNTIL_PULSE;
                }
                case RESULT -> {
                    return LockCraftingMode.LOCK_UNTIL_RESULT;
                }
            }
        }
        return LockCraftingMode.NONE;
    }

    /**
     * 当样板推送完成，刷新一次锁定状态
     */
    private void onPushPatternSuccess(IPatternDetails pattern) {
        resetCraftingLock();

        LockCraftingMode lockMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        switch (lockMode) {
            case LOCK_UNTIL_PULSE -> {
                if (getRedstoneState()) {
                    // 已经有信号，等待无信号时切换到 REDSTONE_POWER
                    unlockEvent = UnlockCraftingEvent.REDSTONE_PULSE;
                } else {
                    // 当前无信号，等待信号
                    unlockEvent = UnlockCraftingEvent.REDSTONE_POWER;
                }
                redstoneState = YesNo.UNDECIDED; // 在下次更新时检查红石状态
                saveChanges();
            }
            case LOCK_UNTIL_RESULT -> {
                unlockEvent = UnlockCraftingEvent.RESULT;
                unlockStack = pattern.getPrimaryOutput();
                saveChanges();
            }
        }
    }

    /**
     * 将目标方向的MEStorage或者其他合适能力包装成PatternProviderTarget
     */
    @Nullable
    private PatternProviderTarget findAdapter(Direction side) {
        if (targetCaches[side.get3DDataValue()] == null) {
            BlockEntity thisBe = host.getBlockEntity();
            targetCaches[side.get3DDataValue()] = new PatternProviderTargetCache(
                    (ServerLevel) thisBe.getLevel(),
                    thisBe.getBlockPos().relative(side),
                    side.getOpposite(),
                    actionSource);
        }

        return targetCaches[side.get3DDataValue()].find();
    }

    /**
     * 对列表应用轮询
     */
    private <T> void rearrangeRoundRobin(List<T> list) {
        if (list.isEmpty()) {
            return;
        }

        roundRobinIndex %= list.size();
        for (int i = 0; i < roundRobinIndex; ++i) {
            list.add(list.get(i));
        }
        list.subList(0, roundRobinIndex).clear();
    }

    public boolean isBlocking() {
        return this.configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES;
    }

    /**
     * 过滤掉同网络直连的 AE 机器目标面，保持与原版样板供应器一致。
     */
    private Set<Direction> getActiveSides() {
        EnumSet<Direction> sides = EnumSet.copyOf(host.getTargets());

        IGridNode node = mainNode.getNode();
        if (node != null) {
            for (var entry : node.getInWorldConnections().entrySet()) {
                IGridNode otherNode = entry.getValue().getOtherSide(node);
                Object otherOwner = otherNode.getOwner();
                if (otherOwner instanceof PatternProviderLogicHost || otherOwner instanceof IntegratedInterfaceHost || (otherOwner instanceof InterfaceLogicHost && Objects.equals(otherNode.getGrid(), mainNode.getGrid()))) {
                    sides.remove(entry.getKey());
                }
            }
        }

        return sides;
    }

    /**
     * 检查目标是否能接收此样板所有输入种类（不包含具体的数量验证）
     */
    private boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
        for (KeyCounter inputList : inputHolder) {
            for (Object2LongMap.Entry<AEKey> input : inputList) {
                long inserted = target.insert(input.getKey(), input.getLongValue(), Actionable.SIMULATE);
                if (inserted == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将物品加入待发送列表，然后加速接口运行
     */
    private void addToSendList(AEKey what, long amount) {
        if (amount > 0) {
            this.sendList.add(new GenericStack(what, amount));

            this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    /**
     * 尝试将sendList中的东西向外发送
     */
    private boolean sendStacksOut() {
        if (sendDirection == null) {
            if (!sendList.isEmpty()) {
                throw new IllegalStateException("Invalid pattern provider state, this is a bug.");
            }
            return false;
        }

        PatternProviderTarget adapter = findAdapter(sendDirection);
        if (adapter == null) {
            return false;
        }

        boolean didSomething = false;

        for (ListIterator<GenericStack> it = sendList.listIterator(); it.hasNext();) {
            GenericStack stack = it.next();
            AEKey what = stack.what();
            long amount = stack.amount();

            long inserted = adapter.insert(what, amount, Actionable.MODULATE);
            if (inserted >= amount) {
                it.remove();
                didSomething = true;
            } else if (inserted > 0) {
                it.set(new GenericStack(what, amount - inserted));
                didSomething = true;
            }
        }

        if (sendList.isEmpty()) {
            sendDirection = null;
        }

        return didSomething;
    }

    private boolean getRedstoneState() {
        if (redstoneState == YesNo.UNDECIDED) {
            BlockEntity be = this.host.getBlockEntity();
            Level level = be.getLevel();
            if (level == null) {
                redstoneState = YesNo.UNDECIDED;
            } else {
                redstoneState = be.getLevel().hasNeighborSignal(be.getBlockPos()) ? YesNo.YES : YesNo.NO;
            }
        }
        return redstoneState == YesNo.YES;
    }

    public void resetCraftingLock() {
        if (unlockEvent != null) {
            unlockEvent = null;
            unlockStack = null;
            saveChanges();
        }
    }

    /**
     * 当前供应器是否繁忙
     */
    @Override
    public boolean isBusy() {
        return !sendList.isEmpty();
    }

    // ICraftingRequester
    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        int slot = this.craftingTracker.getSlot(link);
        return storage.insert(slot, what, amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    private boolean isAllowedInStorageSlot(int slot, AEKey what) {
        if (slot < configInv.size()) {
            var configured = configInv.getKey(slot);
            if (configured == null || configured.equals(what)) {
                return true;
            }
            if (upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                var fuzzyMode = configManager.getSetting(Settings.FUZZY_MODE);
                return configured.fuzzyEquals(what, fuzzyMode);
            }
            return false;
        }
        return true;
    }

    private void onConfigChanged() {
        this.saveChanges();
        // FUZZY_MODE 变了也会走这里，所以要重算计划
        updatePlan();
    }

    private void onConfigRowChanged() {
        this.saveChanges();
        readConfig();
    }

    private void onStorageChanged() {
        this.saveChanges();
        updatePlan();
    }

    private void onUpgradesChanged() {
        this.saveChanges();

        // 移除合成卡时，取消所有挂起的合成任务
        if (!upgrades.isInstalled(AEItems.CRAFTING_CARD)) {
            cancelCrafting();
        }

        // fuzzy 卡插拔也会改变匹配规则
        updatePlan();
    }

    private void readConfig() {
        BlockEntity be = this.host.getBlockEntity();
        Level level = be.getLevel();
        this.hasConfig = !this.configInv.isEmpty();
        updatePlan();

        if (level != null)
            level.invalidateCapabilities(be.getBlockPos());
    }

    private boolean hasStorageWork() {
        for (GenericStack work : this.plannedWork) {
            if (work != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新计算所有槽位的 plannedWork，并根据有无工作唤醒/睡眠设备
     */
    private void updatePlan() {
        boolean hadWork = hasStorageWork();

        for (int i = 0; i < this.configInv.size(); i++) {
            updatePlan(i);
        }

        boolean hasWork = hasStorageWork();
        if (hadWork != hasWork) {
            mainNode.ifPresent((grid, node) -> {
                if (hasWork) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }
    }

    /**
     * 单槽位：根据 config vs storage 生成 plannedWork[slot]
     */
    private void updatePlan(int slot) {
        var req = this.configInv.getStack(slot); // 想要的
        var stored = this.storage.getStack(slot); // 现在有的

        if (req == null && stored != null) {
            // 不想存，但现在有东西 -> 全部退回网络
            this.plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
        } else if (req != null) {
            if (stored == null) {
                // 想存但现在空 -> 从网络拉 req
                this.plannedWork[slot] = req;
            } else if (storedRequestEquals(req.what(), stored.what())) {
                long delta = req.amount() - stored.amount();
                this.plannedWork[slot] = (delta != 0) ? new GenericStack(req.what(), delta) : null;
            } else {
                // 槽里是别的东西 -> 先全部退回网络
                this.plannedWork[slot] = new GenericStack(stored.what(), -stored.amount());
            }
        } else {
            // req == null && stored == null
            this.plannedWork[slot] = null;
        }
    }

    private boolean storedRequestEquals(AEKey request, AEKey stored) {
        if (upgrades.isInstalled(AEItems.FUZZY_CARD) && request.supportsFuzzyRangeSearch()) {
            return request.fuzzyEquals(stored, configManager.getSetting(Settings.FUZZY_MODE));
        } else {
            return request.equals(stored);
        }
    }

    /**
     * 执行存储相关的计划（plannedWork），返回本 tick 是否做成了任何事情
     */
    private boolean updateStorage() {
        boolean didSomething = false;

        for (int slot = 0; slot < plannedWork.length; slot++) {
            GenericStack work = plannedWork[slot];
            if (work != null) {
                int amount = (int) work.amount();
                if (usePlan(slot, work.what(), amount)) {
                    didSomething = true;
                }
            }
        }

        return didSomething;
    }

    private boolean usePlan(int slot, AEKey what, int amount) {
        boolean changed = tryUsePlan(slot, what, amount);
        if (changed) {
            updatePlan(slot);
        }
        return changed;
    }

    /**
     * 根据配置plan拉取/退回资源
     */
    private boolean tryUsePlan(int slot, AEKey what, int amount) {
        var grid = mainNode.getGrid();
        if (grid == null) {
            return false;
        }

        var networkInv = grid.getStorageService().getInventory();
        var energySrc = grid.getEnergyService();

        // amount < 0 - 从接口推回网络
        if (amount < 0) {
            amount = -amount;

            GenericStack inSlot = storage.getStack(slot);
            if (inSlot == null || !what.matches(inSlot) || inSlot.amount() < amount) {
                // 槽位状态改了，要求重新规划
                return true;
            }

            // 注入网络
            long inserted = StorageHelper.poweredInsert(
                    energySrc,
                    networkInv,
                    what,
                    amount,
                    this.actionSource);

            if (inserted > 0) {
                // 从本地仓扣掉
                storage.extract(slot, what, inserted, Actionable.MODULATE);

                // 通知RESULT锁
                onStackReturnedToNetwork(new GenericStack(what, inserted));
            }

            return inserted > 0;
        }

        // 如果该槽位已经有挂起的合成任务，优先走合成处理
        if (this.craftingTracker.isBusy(slot)) {
            return handleCrafting(slot, what, amount);
        }

        // amount > 0 - 从网络拉货进接口
        if (amount > 0) {
            // 先模拟是否能插满 amount，避免计划过期
            long canInsert = storage.insert(slot, what, amount, Actionable.SIMULATE);
            if (canInsert != amount) {
                return true; // 槽位状态变了，重新规划
            }

            // 尝试直接从网络抽取指定 key
            if (acquireFromNetwork(energySrc, networkInv, slot, what, amount)) {
                return true;
            }

            // 如果这个槽现在还空，并且有 FUZZY 卡，则尝试模糊拉货
            if (storage.getStack(slot) == null && upgrades.isInstalled(AEItems.FUZZY_CARD)) {
                FuzzyMode fuzzyMode = configManager.getSetting(Settings.FUZZY_MODE);
                for (var entry : grid.getStorageService().getCachedInventory().findFuzzy(what, fuzzyMode)) {
                    long maxAmount = storage.insert(slot, entry.getKey(), amount, Actionable.SIMULATE);
                    if (acquireFromNetwork(energySrc, networkInv, slot, entry.getKey(), maxAmount)) {
                        return true;
                    }
                }
            }

            // 网络里没有现货 - 尝试拉起合成任务
            return handleCrafting(slot, what, amount);
        }

        return false;
    }

    /**
     * 从网络中拉指定 key 进入 storage[slot]，返回是否成功拉到一点
     */
    private boolean acquireFromNetwork(IEnergyService energySrc, MEStorage networkInv,
                                       int slot, AEKey what, long amount) {
        long acquired = StorageHelper.poweredExtraction(energySrc, networkInv, what, amount, this.actionSource);
        if (acquired > 0) {
            long inserted = storage.insert(slot, what, acquired, Actionable.MODULATE);
            if (inserted < acquired) {
                // TODO 添加Log，而不直接报异常
                // throw new IllegalStateException("Bad inventory plan: voided items: " + (acquired - inserted));
            }
            return true;
        }
        return false;
    }

    private boolean handleCrafting(int slot, AEKey key, long amount) {
        IGrid grid = mainNode.getGrid();
        if (grid != null && upgrades.isInstalled(AEItems.CRAFTING_CARD) && key != null) {
            return this.craftingTracker.handleCrafting(
                    slot,
                    key,
                    amount,
                    host.getBlockEntity().getLevel(),
                    grid.getCraftingService(),
                    this.actionSource);
        }
        return false;
    }

    private void cancelCrafting() {
        this.craftingTracker.cancel();
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    /**
     * 获取样板管理终端中显示的分组，使用与推送相同的有效目标面。
     */
    public PatternContainerGroup getTerminalGroup() {
        BlockEntity blockEntity = host.getBlockEntity();
        Level hostLevel = blockEntity.getLevel();

        // 如果有自定义名称，则使用自定义名称
        if (this.host instanceof Nameable nameable && nameable.hasCustomName()) {
            Component name = nameable.getCustomName();
            return new PatternContainerGroup(
                    this.host.getTerminalIcon(),
                    name,
                    List.of());
        }

        var groups = new LinkedHashSet<PatternContainerGroup>();
        for (Direction side : getActiveSides()) {
            BlockPos sidePos = blockEntity.getBlockPos().relative(side);
            PatternContainerGroup group = PatternContainerGroup.fromMachine(hostLevel, sidePos, side.getOpposite());
            if (group != null) {
                groups.add(group);
            }
        }

        // 如果附近有了一个组，使用它的组
        if (groups.size() == 1) {
            return groups.iterator().next();
        }

        List<Component> tooltip = List.of();
        // 如果有多个组，一起显示出来
        if (groups.size() > 1) {
            tooltip = new ArrayList<>();
            tooltip.add(GuiText.AdjacentToDifferentMachines.text().withStyle(ChatFormatting.BOLD));
            for (PatternContainerGroup group : groups) {
                tooltip.add(group.name());
                for (Component line : group.tooltip()) {
                    tooltip.add(Component.literal("  ").append(line));
                }
            }
        }

        // 如果没有任何东西，则自己显示
        AEItemKey hostIcon = this.host.getTerminalIcon();
        return new PatternContainerGroup(
                hostIcon,
                hostIcon.getDisplayName(),
                tooltip);
    }

    private class Ticker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            // 有工作就醒着，没有工作就可以睡
            boolean idle = !hasStorageWork() && sendList.isEmpty();
            return new TickingRequest(TickRates.Interface, idle);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }

            boolean didSomething = updateStorage() | sendStacksOut();

            boolean stillHasWork = hasStorageWork() || !sendList.isEmpty();

            if (!stillHasWork) {
                return TickRateModulation.SLEEP;
            } else if (didSomething) {
                return TickRateModulation.URGENT;
            } else {
                return TickRateModulation.SLOWER;
            }
        }
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        // 配置 / 存储
        this.configInv.writeToChildTag(tag, "config", registries);
        this.storage.writeToChildTag(tag, "storage", registries);

        // 升级
        this.upgrades.writeToNBT(tag, "upgrades", registries);

        // 机器配置（FUZZY_MODE / BLOCKING / LOCK_CRAFTING_MODE / PATTERN_ACCESS_TERMINAL）
        this.configManager.writeToNBT(tag, registries);

        // 样板槽
        this.patternInventory.writeToNBT(tag, "patterns", registries);

        // 合成跟踪
        this.craftingTracker.writeToNBT(tag);

        // 样板推送相关
        tag.putInt("roundRobinIndex", this.roundRobinIndex);
        tag.putByte("redstoneState", (byte) this.redstoneState.ordinal());

        if (unlockEvent != null) {
            tag.putByte("unlockEvent", (byte) unlockEvent.ordinal());
        }
        if (unlockStack != null && registries != null) {
            tag.put("unlockStack", GenericStack.writeTag(registries, unlockStack));
        }

        if (!sendList.isEmpty() && registries != null) {
            var list = new ListTag();
            for (var gs : sendList) {
                list.add(GenericStack.writeTag(registries, gs));
            }
            tag.put("sendList", list);
        }
        if (sendDirection != null) {
            tag.putByte("sendDirection", (byte) sendDirection.get3DDataValue());
        }
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        // 合成跟踪必须先读（里面可能依赖其它字段）
        this.craftingTracker.readFromNBT(tag);

        // 升级
        this.upgrades.readFromNBT(tag, "upgrades", registries);

        // 配置
        this.configInv.readFromChildTag(tag, "config", registries);

        // 存储-支持两个key，使其可以通过升级模板从接口或者供应器任意一方升级得到
        if (tag.contains("storage"))
            this.storage.readFromChildTag(tag, "storage", registries);
        else if (tag.contains("returnInv"))
            this.storage.readFromChildTag(tag, "returnInv", registries);

        // 机器配置
        this.configManager.readFromNBT(tag, registries);

        // 样板槽
        this.patternInventory.readFromNBT(tag, "patterns", registries);

        // 配置读完后要重新计算 hasConfig + plannedWork
        readConfig();

        // 样板槽更新 patternInputs / patterns 列表
        updatePatterns();

        // 样板推送状态
        this.roundRobinIndex = tag.getInt("roundRobinIndex");
        this.redstoneState = YesNo.values()[tag.getByte("redstoneState")];

        if (tag.contains("unlockEvent")) {
            byte u = tag.getByte("unlockEvent");
            if (u >= 0 && u < UnlockCraftingEvent.values().length) {
                this.unlockEvent = UnlockCraftingEvent.values()[u];
            }
        } else {
            this.unlockEvent = null;
        }

        if (tag.contains("unlockStack") && registries != null) {
            this.unlockStack = GenericStack.readTag(registries, tag.getCompound("unlockStack"));
        } else {
            this.unlockStack = null;
        }

        this.sendList.clear();
        if (tag.contains("sendList") && registries != null) {
            var list = tag.getList("sendList", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                var gs = GenericStack.readTag(registries, list.getCompound(i));
                if (gs != null) {
                    this.sendList.add(gs);
                }
            }
        }

        if (tag.contains("sendDirection")) {
            this.sendDirection = Direction.from3DDataValue(tag.getByte("sendDirection"));
        } else {
            this.sendDirection = null;
        }
    }

    /**
     * 邻居红石变化时调用，处理 LOCK_UNTIL_PULSE
     */
    public void updateRedstoneState() {
        if (unlockEvent == UnlockCraftingEvent.REDSTONE_POWER && getRedstoneState()) {
            // 等待有电 -> 现在有电 -> 解锁
            unlockEvent = null;
            saveChanges();
        } else if (unlockEvent == UnlockCraftingEvent.REDSTONE_PULSE && !getRedstoneState()) {
            // 等待断电再等下一次上电
            unlockEvent = UnlockCraftingEvent.REDSTONE_POWER;
            redstoneState = YesNo.UNDECIDED;
            saveChanges();
        } else {
            // 其他情况仅仅重置缓存，下次读取重新测红石
            redstoneState = YesNo.UNDECIDED;
        }
    }

    // 后续在能力暴露中使用即可
    public MEStorage getExposedMEStorage(Direction side) {
        var grid = mainNode.getGrid();
        if (!hasConfig && grid != null) {
            // 没配备货，就直接暴露整个网络
            return grid.getStorageService().getInventory();
        }
        // 有 config，则暴露本地仓
        return storage;
    }

    private void onStackReturnedToNetwork(GenericStack stack) {
        // 当前不是 RESULT 模式，直接忽略
        if (unlockEvent != UnlockCraftingEvent.RESULT) {
            return;
        }

        if (unlockStack == null) {
            // 状态不一致，直接解锁防止死锁
            unlockEvent = null;
            saveChanges();
            return;
        }

        // 只统计主输出类型
        if (!unlockStack.what().equals(stack.what())) {
            return;
        }

        long remaining = unlockStack.amount() - stack.amount();

        if (remaining <= 0) {
            // 数量足够了，解锁
            unlockStack = null;
            unlockEvent = null;
        } else {
            // 继续等待
            unlockStack = new GenericStack(unlockStack.what(), remaining);
        }

        saveChanges();
    }

    public void saveChanges() {
        this.host.saveChanges();
    }

    // 把所有样板转成空白样板并给玩家
    private void clearPatternInventory(Player player) {
        if (player.getAbilities().instabuild) {
            for (int i = 0; i < patternInventory.size(); i++) {
                patternInventory.setItemDirect(i, ItemStack.EMPTY);
            }
            return;
        }

        Inventory playerInv = player.getInventory();

        // 清空样板并给玩家
        int blankPatternCount = 0;
        for (int i = 0; i < patternInventory.size(); i++) {
            ItemStack pattern = patternInventory.getStackInSlot(i);
            if (pattern.is(AEItems.CRAFTING_PATTERN.asItem()) || pattern.is(AEItems.PROCESSING_PATTERN.asItem()) || pattern.is(AEItems.SMITHING_TABLE_PATTERN.asItem()) || pattern.is(AEItems.STONECUTTING_PATTERN.asItem()) || pattern.is(AEItems.BLANK_PATTERN.asItem())) {
                blankPatternCount += pattern.getCount();
            } else {
                playerInv.placeItemBackInInventory(pattern);
            }
            patternInventory.setItemDirect(i, ItemStack.EMPTY);
        }

        if (blankPatternCount > 0) {
            playerInv.placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(blankPatternCount), false);
        }
    }

    public void exportSettings(DataComponentMap.Builder builder) {
        builder.set(AEComponents.EXPORTED_PATTERNS, patternInventory.toItemContainerContents());
    }

    public void importSettings(DataComponentMap input, @Nullable Player player) {
        ItemContainerContents patterns = input.getOrDefault(AEComponents.EXPORTED_PATTERNS, ItemContainerContents.EMPTY);

        if (player != null && !player.level().isClientSide) {
            clearPatternInventory(player);

            AppEngInternalInventory desiredPatterns = new AppEngInternalInventory(patternInventory.size());
            desiredPatterns.fromItemContainerContents(patterns);

            // 从玩家背包将空白样板恢复出来
            Inventory playerInv = player.getInventory();
            int blankPatternsAvailable = player.getAbilities().instabuild ? Integer.MAX_VALUE : playerInv.countItem(AEItems.BLANK_PATTERN.asItem());
            int blankPatternsUsed = 0;
            for (int i = 0; i < desiredPatterns.size(); i++) {
                if (desiredPatterns.getStackInSlot(i).isEmpty()) {
                    continue;
                }

                IPatternDetails pattern = PatternDetailsHelper.decodePattern(desiredPatterns.getStackInSlot(i),
                        host.getBlockEntity().getLevel());
                if (pattern == null) {
                    continue;
                }

                ++blankPatternsUsed;
                if (blankPatternsAvailable >= blankPatternsUsed) {
                    if (!patternInventory.addItems(pattern.getDefinition().toStack()).isEmpty()) {
                        AELog.warn("Failed to add pattern to pattern provider");
                        blankPatternsUsed--;
                    }
                }
            }

            if (blankPatternsUsed > 0 && !player.getAbilities().instabuild) {
                new PlayerInternalInventory(playerInv)
                        .removeItems(blankPatternsUsed, AEItems.BLANK_PATTERN.stack(), null);
            }

            // 如果无法恢复所有样板，则发出警告
            if (blankPatternsUsed > blankPatternsAvailable) {
                player.sendSystemMessage(
                        PlayerMessages.MissingBlankPatterns.text(blankPatternsUsed - blankPatternsAvailable));
            }
        }
    }

    public void onMainNodeStateChanged() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
        }
    }

    public ConfigInventory getConfigInv() {
        return configInv;
    }

    public ConfigInventory getStorageInv() {
        return storage;
    }

    public AppEngInternalInventory getPatternInventory() {
        return patternInventory;
    }

    @Nullable
    public IGrid getGrid() {
        return mainNode.getGrid();
    }

    public void addDrops(List<ItemStack> drops) {
        for (ItemStack stack : this.patternInventory) {
            drops.add(stack);
        }

        for (ItemStack stack : upgrades) {
            drops.add(stack);
        }

        for (GenericStack stack : this.sendList) {
            stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(),
                    this.host.getBlockEntity().getBlockPos());
        }

        for (GenericStack stack : this.getStorageInv().toList()) {
            if (stack == null) continue;

            stack.what().addDrops(stack.amount(), drops, this.host.getBlockEntity().getLevel(),
                    this.host.getBlockEntity().getBlockPos());
        }
    }

    public void cleanContent() {
        this.patternInventory.clear();
        this.upgrades.clear();
        this.sendList.clear();
        this.storage.clear();
    }
}
