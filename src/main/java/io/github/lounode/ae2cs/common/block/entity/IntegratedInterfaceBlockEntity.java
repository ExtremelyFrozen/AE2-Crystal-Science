package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.config.*;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.localization.GuiText;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.IPriorityHost;
import appeng.helpers.MultiCraftingTracker;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.helpers.patternprovider.UnlockCraftingEvent;
import appeng.me.helpers.MachineSource;
import appeng.menu.ISubMenu;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import com.google.common.collect.ImmutableSet;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** 同时包含ME样板供应器与ME接口的能力。即旧版ME接口 */
public class IntegratedInterfaceBlockEntity extends AENetworkedBlockEntity implements IConfigurableObject,
        IPriorityHost, PatternContainer, IUpgradeableObject, ICraftingProvider, ICraftingRequester
{
    /**
     * 机器配置
     */
    private final IConfigManager configManager;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 标记仓
     */
    private final ConfigInventory config;

    /**
     * 返回兼存储仓
     */
    private final ConfigInventory storage;

    /**
     * 样板槽 - 只允许UI存取
     */
    private final AppEngInternalInventory patternInventory;

    /** 解析后的样板信息 */
    private final List<IPatternDetails> patterns = new ArrayList<>();



    /**
     * 升级槽 - 允许UI以及右键机器
     */
    private final IUpgradeInventory upgrades =
            UpgradeInventories.forMachine(AECSBlocks.INTEGRATED_INTERFACE_BLOCK, 1, () -> {});


    /**
     * 包含了所有样板输入项的集合，用来给阻塞模式快速判断
     */
    private final Set<AEKey> patternInputs = new HashSet<>();
    /** 等待发生的资源列表 */
    private final List<GenericStack> sendList = new ArrayList<>();
    /** 下一次发生会尝试向哪个方向？ */
    private Direction sendDirection;


    private int roundRobinIndex = 0;
    private YesNo redstoneState = YesNo.UNDECIDED;
    @Nullable
    private UnlockCraftingEvent unlockEvent;
    @Nullable
    private GenericStack unlockStack;
    private final PatternProviderTargetCache_COPY[] targetCaches = new PatternProviderTargetCache_COPY[6];
    private final IActionSource actionSource;

    private final MultiCraftingTracker craftingTracker;

    public IntegratedInterfaceBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.INTEGRATED_INTERFACE_BLOCK_ENTITY.get(), pos, blockState);

        getMainNode().setIdlePowerUsage(8.0) // 待机消耗
                .setFlags(GridFlags.REQUIRE_CHANNEL) // 需要频道
                .setExposedOnSides(EnumSet.allOf(Direction.class)) // 可以用于连接的方向
                .addService(ICraftingProvider.class, this)
                .addService(ICraftingRequester.class, this);

        configManager = IConfigManager.builder(this::onConfigChanged)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
                .registerSetting(Settings.BLOCKING_MODE, YesNo.NO)
                .registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES)
                .registerSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE)
                .build();

        this.patternInventory = new AppEngInternalInventory(9)
        {
            @Override
            public boolean isItemValid(int slot, ItemStack stack)
            {
                return super.isItemValid(slot, stack) && stack.getItem() instanceof EncodedPatternItem;
            }

            @Override
            protected void onContentsChanged(int slot)
            {
                super.onContentsChanged(slot);
                updatePatterns();
                setChanged();
            }
        };

        this.actionSource = new MachineSource(getMainNode()::getNode);
        this.craftingTracker = new MultiCraftingTracker(this, 9);
    }

    // IConfigurableObject
    @Override
    public IConfigManager getConfigManager()
    {
        return this.configManager;
    }

    private void onConfigChanged()
    {
        this.saveChanges();
        //updatePlan();
    }

    // IPriorityHost
    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    // PatternContainer
    @Override
    public @Nullable IGrid getGrid()
    {
        return getMainNode().isReady() ? getMainNode().getGrid() : null;
    }

    @Override
    public boolean isVisibleInTerminal()
    {
        return getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES;
    }

    @Override
    public InternalInventory getTerminalPatternInventory()
    {
        return patternInventory;
    }

    @Override
    public long getTerminalSortOrder()
    {
        return (long) this.getBlockPos().getZ() << 24 ^ (long) this.getBlockPos().getX() << 8 ^ this.getBlockPos().getY();
    }

    @Override
    public PatternContainerGroup getTerminalGroup()
    {
        Level hostLevel = this.getLevel();

        // 如果由自定义名称，则使用自定义名称
        if (this.hasCustomName())
        {
            Component name = this.getCustomName();
            return new PatternContainerGroup(
                    this.getTerminalIcon(),
                    name,
                    List.of());
        }

        // TODO 从全向转为目标方向
        Set<Direction> sides = new HashSet<>(List.of(Direction.values()));
        var groups = new LinkedHashSet<PatternContainerGroup>(sides.size());
        for (var side : sides)
        {
            var sidePos = getBlockPos().relative(side);
            var group = PatternContainerGroup.fromMachine(hostLevel, sidePos, side.getOpposite());
            if (group != null)
            {
                groups.add(group);
            }
        }

        // 如果附近有了一个组，使用它的组
        if (groups.size() == 1)
        {
            return groups.iterator().next();
        }

        List<Component> tooltip = List.of();
        // 如果有多个组，一起显示出来
        if (groups.size() > 1)
        {
            tooltip = new ArrayList<>();
            tooltip.add(GuiText.AdjacentToDifferentMachines.text().withStyle(ChatFormatting.BOLD));
            for (var group : groups)
            {
                tooltip.add(group.name());
                for (var line : group.tooltip())
                {
                    tooltip.add(Component.literal("  ").append(line));
                }
            }
        }

        // 如果没有任何东西，则自己显示
        var hostIcon = this.getTerminalIcon();
        return new PatternContainerGroup(
                hostIcon,
                hostIcon.getDisplayName(),
                tooltip);
    }

    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get());
    }

    //IUpgradeableObject
    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    // ICraftingProvider
    @Override
    public List<IPatternDetails> getAvailablePatterns()
    {
        return this.patterns;
    }

    @Override
    public int getPatternPriority()
    {
        return this.priority;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder)
    {
        if (!sendList.isEmpty() || !this.getMainNode().isActive() || !this.patterns.contains(patternDetails))
        {
            return false;
        }

        Level level = getLevel();

        if (getCraftingLockedReason() != LockCraftingMode.NONE)
        {
            return false;
        }

        record PushTarget(Direction direction, PatternProviderTarget target)
        {
        }
        ArrayList<PushTarget> possibleTargets = new ArrayList<>();

        // TODO 把全向推送改成可调节面
        // Push to crafting machines first
        for (Direction direction : Direction.values())
        {
            var adjPos = getBlockPos().relative(direction);
            var adjBeSide = direction.getOpposite();

            var craftingMachine = ICraftingMachine.of(level, adjPos, adjBeSide);
            if (craftingMachine != null && craftingMachine.acceptsPlans())
            {
                if (craftingMachine.pushPattern(patternDetails, inputHolder, adjBeSide))
                {
                    onPushPatternSuccess(patternDetails);
                    return true;
                }
                continue;
            }

            var adapter = findAdapter(direction);
            if (adapter == null)
                continue;

            possibleTargets.add(new PushTarget(direction, adapter));
        }

        // If no dedicated crafting machine could be found, and the pattern does not support
        // generic external inventories, stop here.
        if (!patternDetails.supportsPushInputsToExternalInventory())
        {
            return false;
        }

        // Rearrange for round-robin
        rearrangeRoundRobin(possibleTargets);

        // Push to other kinds of blocks
        for (int i = 0; i < possibleTargets.size(); ++i)
        {
            var target = possibleTargets.get(i);
            var direction = target.direction();
            var adapter = target.target();

            if (this.isBlocking() && adapter.containsPatternInput(this.patternInputs))
            {
                continue;
            }

            if (this.adapterAcceptsAll(adapter, inputHolder))
            {
                patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                    var inserted = adapter.insert(what, amount, Actionable.MODULATE);
                    if (inserted < amount)
                    {
                        this.addToSendList(what, amount - inserted);
                    }
                });
                onPushPatternSuccess(patternDetails);
                this.sendDirection = direction;
                this.sendStacksOut();
                roundRobinIndex += i + 1;
                return true;
            }
        }

        return false;
    }

    public void updatePatterns()
    {
        patterns.clear();
        patternInputs.clear();

        for (ItemStack stack : this.patternInventory)
        {
            IPatternDetails details = PatternDetailsHelper.decodePattern(stack, this.getBlockEntity().getLevel());

            if (details != null)
            {
                patterns.add(details);

                for (IPatternDetails.IInput iinput : details.getInputs())
                {
                    for (GenericStack inputCandidate : iinput.getPossibleInputs())
                    {
                        patternInputs.add(inputCandidate.what().dropSecondary());
                    }
                }
            }
        }

        ICraftingProvider.requestUpdate(getMainNode());
    }

    /**
     * Gets if the crafting lock is in effect and why.
     *
     * @return null if the lock isn't in effect
     */
    public LockCraftingMode getCraftingLockedReason()
    {
        var lockMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        if (lockMode == LockCraftingMode.LOCK_WHILE_LOW && !getRedstoneState())
        {
            // Crafting locked by redstone signal
            return LockCraftingMode.LOCK_WHILE_LOW;
        }
        else if (lockMode == LockCraftingMode.LOCK_WHILE_HIGH && getRedstoneState())
        {
            return LockCraftingMode.LOCK_WHILE_HIGH;
        }
        else if (unlockEvent != null)
        {
            // Crafting locked by waiting for unlock event
            switch (unlockEvent)
            {
                case REDSTONE_POWER, REDSTONE_PULSE ->
                {
                    return LockCraftingMode.LOCK_UNTIL_PULSE;
                }
                case RESULT ->
                {
                    return LockCraftingMode.LOCK_UNTIL_RESULT;
                }
            }
        }
        return LockCraftingMode.NONE;
    }

    private void onPushPatternSuccess(IPatternDetails pattern)
    {
        resetCraftingLock();

        var lockMode = configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
        switch (lockMode)
        {
            case LOCK_UNTIL_PULSE ->
            {
                if (getRedstoneState())
                {
                    // Already have signal, wait for no signal before switching to REDSTONE_POWER
                    unlockEvent = UnlockCraftingEvent.REDSTONE_PULSE;
                }
                else
                {
                    // No signal, wait for signal
                    unlockEvent = UnlockCraftingEvent.REDSTONE_POWER;
                }
                redstoneState = YesNo.UNDECIDED; // Check redstone state again next update
                saveChanges();
            }
            case LOCK_UNTIL_RESULT ->
            {
                unlockEvent = UnlockCraftingEvent.RESULT;
                unlockStack = pattern.getPrimaryOutput();
                saveChanges();
            }
        }
    }

    @Nullable
    private PatternProviderTarget findAdapter(Direction side)
    {
        if (targetCaches[side.get3DDataValue()] == null)
        {
            var thisBe = getBlockEntity();
            targetCaches[side.get3DDataValue()] = new PatternProviderTargetCache_COPY(
                    (ServerLevel) thisBe.getLevel(),
                    thisBe.getBlockPos().relative(side),
                    side.getOpposite(),
                    actionSource);
        }

        return targetCaches[side.get3DDataValue()].find();
    }

    /**
     * Apply round-robin to list.
     */
    private <T> void rearrangeRoundRobin(List<T> list)
    {
        if (list.isEmpty())
        {
            return;
        }

        roundRobinIndex %= list.size();
        for (int i = 0; i < roundRobinIndex; ++i)
        {
            list.add(list.get(i));
        }
        list.subList(0, roundRobinIndex).clear();
    }

    public boolean isBlocking()
    {
        return this.configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES;
    }

    private boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder)
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

    private void addToSendList(AEKey what, long amount)
    {
        if (amount > 0)
        {
            this.sendList.add(new GenericStack(what, amount));

            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    private boolean sendStacksOut()
    {
        if (sendDirection == null)
        {
            if (!sendList.isEmpty())
            {
                throw new IllegalStateException("Invalid pattern provider state, this is a bug.");
            }
            return false;
        }

        var adapter = findAdapter(sendDirection);
        if (adapter == null)
        {
            return false;
        }

        boolean didSomething = false;

        for (var it = sendList.listIterator(); it.hasNext(); )
        {
            var stack = it.next();
            var what = stack.what();
            long amount = stack.amount();

            var inserted = adapter.insert(what, amount, Actionable.MODULATE);
            if (inserted >= amount)
            {
                it.remove();
                didSomething = true;
            }
            else if (inserted > 0)
            {
                it.set(new GenericStack(what, amount - inserted));
                didSomething = true;
            }
        }

        if (sendList.isEmpty())
        {
            sendDirection = null;
        }

        return didSomething;
    }

    private boolean getRedstoneState()
    {
        if (redstoneState == YesNo.UNDECIDED)
        {
            var be = this.getBlockEntity();
            redstoneState = be.getLevel().hasNeighborSignal(be.getBlockPos())
                    ? YesNo.YES
                    : YesNo.NO;
        }
        return redstoneState == YesNo.YES;
    }

    public void resetCraftingLock()
    {
        if (unlockEvent != null)
        {
            unlockEvent = null;
            unlockStack = null;
            saveChanges();
        }
    }

    @Override
    public boolean isBusy()
    {
        return !sendList.isEmpty();
    }

    // ICraftingRequester
    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs()
    {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode)
    {
        int slot = this.craftingTracker.getSlot(link);
        return storage.insert(slot, what, amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link)
    {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        //MenuOpener.returnTo(APMenus.ME_AMADRON_PROCESS_STATION_MENU.get(), player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(getBlockState().getBlock());
    }
}
