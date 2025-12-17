package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.IPriorityHost;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.util.ConfigInventory;
import appeng.util.SettingsFrom;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import io.github.lounode.ae2cs.api.util.AEKeyHelper;
import io.github.lounode.ae2cs.api.util.ForgeEnergyAdapterUpgrade;
import io.github.lounode.ae2cs.api.util.GenericStackInvHelper;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MeteoriteCrafterBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IPriorityHost,
        IUpgradeableObject, PatternContainer, ICraftingProvider
{

    /**
     * 在没有任何加速卡的情况下，我们每次合成的能量消耗
     */
    public static int energyPerWork = 50;

    /**
     * 优先级
     */
    public int priority = 0;

    /**
     * 升级槽
     */
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.METEORITE_CRAFTER_BLOCK, 4, this::onUpgradesChange);

    /**
     * 样板槽 - 只允许UI存取
     */
    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(null, 72, 1)
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
        }
    };

    /**
     * 返回仓，用于临时存放物品
     */
    private final ConfigInventory returnInventory = ConfigInventory.storage(9)
            .changeListener(this::setChanged)
            .build();

    /**
     * 完成产物表，即将对外输出的物品会被临时存放在此，等待tick中再实际对外发送
     */
    Object2LongOpenHashMap<AEKey> sendContents = new Object2LongOpenHashMap<>();

    /**
     * 解析后的样板信息
     */
    private final List<IPatternDetails> patterns = new ArrayList<>();

    /**
     * 本tick已经完成过的工作次数
     */
    private int worksInRound = 0;

    /**
     * 本轮最大可完成工作次数
     */
    private int maxWorksInRound = 8;

    /**
     * 插入源
     */
    IActionSource actionSource = IActionSource.ofMachine(this);


    public MeteoriteCrafterBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.METEORITE_CRAFTER_BLOCK_ENTITY.get(), pos, blockState, 160000);
        getMainNode().setIdlePowerUsage(0)
                .setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY)
                .addService(ICraftingProvider.class, this);
        returnInventory.useRegisteredCapacities();
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.METEORITE_CRAFTER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                AECSBlockEntities.METEORITE_CRAFTER_BLOCK_ENTITY.get(),
                (be, direction) -> new ForgeEnergyAdapterUpgrade(be, AccessRestriction.READ)
        );
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.METEORITE_CRAFTER_BLOCK_ENTITY.get(),
                (be, direction) -> be.returnInventory
        );
    }

    public AppEngInternalInventory getPatternInventory()
    {
        return patternInventory;
    }

    public ConfigInventory getReturnInventory()
    {
        return returnInventory;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();
        this.worksInRound = 0; // 每tick重置

        // 尝试完成sendContents的内容，或者对returnInventory进行清理
        if (!sendContents.isEmpty() || !returnInventory.isEmpty())
        {
            @Nullable MEStorage gridInv = null;
            @Nullable MEStorage targetInv = null;

            IGrid grid = getMainNode().getGrid();
            if (grid != null)
            {
                gridInv = grid.getStorageService().getInventory();
            }

            Direction targetDir = getPushDirection().getDirection();
            if (targetDir != null)
            {
                BlockPos targetPos = worldPosition.relative(targetDir);
                targetInv = GenericStackInvHelper.getAdjacentMeStorage(level, targetPos, null, targetDir.getOpposite());
            }

            // 完成sendContent内容
            if (!sendContents.isEmpty())
            {
                ObjectIterator<Object2LongMap.Entry<AEKey>> it = sendContents.object2LongEntrySet().iterator();
                while (it.hasNext())
                {
                    Object2LongMap.Entry<AEKey> entry = it.next();
                    AEKey key = entry.getKey();
                    long remaining = entry.getLongValue();
                    if (key == null || remaining <= 0)
                    {
                        it.remove();
                        continue;
                    }

                    // 尝试塞入可用库存
                    if (targetInv != null)
                    {
                        long inserted = targetInv.insert(key, remaining, Actionable.MODULATE, actionSource);
                        remaining -= inserted;
                    }
                    if (gridInv != null && remaining > 0)
                    {
                        long inserted = gridInv.insert(key, remaining, Actionable.MODULATE, actionSource);
                        remaining -= inserted;
                    }
                    if (remaining > 0)
                    {
                        long inserted = returnInventory.insert(key, remaining, Actionable.MODULATE, actionSource);
                        remaining -= inserted;
                    }

                    // 更新 sendContents
                    if (remaining <= 0)
                    {
                        it.remove();
                    }
                    else
                    {
                        entry.setValue(remaining);
                    }
                }
            }

            // 尝试把自身返回槽的物品送回ME网络或输出到附近容器
            if (!returnInventory.isEmpty())
            {
                for (int i = 0; i < returnInventory.size(); i++)
                {
                    GenericStack stack = returnInventory.getStack(i);
                    if (stack == null) continue;

                    long inserted = 0;
                    // 优先尝试容器目标
                    if (targetInv != null)
                    {
                        inserted += targetInv.insert(stack.what(), stack.amount(), Actionable.MODULATE, actionSource);
                    }
                    if (gridInv != null && inserted < stack.amount())
                    {
                        long remaining = stack.amount() - inserted;
                        inserted += gridInv.insert(stack.what(), remaining, Actionable.MODULATE, actionSource);
                    }
                    if (inserted > 0)
                    {
                        returnInventory.extract(i, stack.what(), inserted, Actionable.MODULATE);
                    }
                }
            }
            setChanged();
        }
    }

    /**
     * 重新解析样板信息，并通知ae网络获取
     */
    private void updatePatterns()
    {
        if (level == null || level.isClientSide()) return;

        patterns.clear();

        for (ItemStack stack : this.patternInventory)
        {
            IPatternDetails details = PatternDetailsHelper.decodePattern(stack, getLevel());

            if (canAcceptPattern(details))
            {
                patterns.add(details);
            }
        }

        ICraftingProvider.requestUpdate(getMainNode());
    }

    private void onUpgradesChange()
    {
        this.maxWorksInRound = 8 << getInstalledUpgrades(AEItems.SPEED_CARD);
        this.setChanged();
    }

    private double getEnergyPerWorkAfterSpeed()
    {
        return energyPerWork << getInstalledUpgrades(AEItems.SPEED_CARD);
    }

    private PushDirection getPushDirection()
    {
        return getBlockState().getValue(PatternProviderBlock.PUSH_DIRECTION);
    }

    private boolean canAcceptPattern(@Nullable IPatternDetails details)
    {
        if (details == null) return false;
        return details instanceof IMolecularAssemblerSupportedPattern;
    }

    // 把所有样板转成空白样板并给玩家
    private void clearPatternInventory(Player player)
    {
        if (player.getAbilities().instabuild)
        {
            for (int i = 0; i < patternInventory.size(); i++)
            {
                patternInventory.setItemDirect(i, ItemStack.EMPTY);
            }
            return;
        }

        Inventory playerInv = player.getInventory();

        // 清空样板并给玩家
        int blankPatternCount = 0;
        for (int i = 0; i < patternInventory.size(); i++)
        {
            ItemStack pattern = patternInventory.getStackInSlot(i);
            if (pattern.is(AEItems.CRAFTING_PATTERN.asItem())
                    || pattern.is(AEItems.PROCESSING_PATTERN.asItem())
                    || pattern.is(AEItems.SMITHING_TABLE_PATTERN.asItem())
                    || pattern.is(AEItems.STONECUTTING_PATTERN.asItem())
                    || pattern.is(AEItems.BLANK_PATTERN.asItem()))
            {
                blankPatternCount += pattern.getCount();
            }
            else
            {
                playerInv.placeItemBackInInventory(pattern);
            }
            patternInventory.setItemDirect(i, ItemStack.EMPTY);
        }

        if (blankPatternCount > 0)
        {
            playerInv.placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(blankPatternCount), false);
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        if(mode == SettingsFrom.MEMORY_CARD && level != null)
        {
            ItemContainerContents patterns = input.getOrDefault(AEComponents.EXPORTED_PATTERNS, ItemContainerContents.EMPTY);

            if (player != null && !level.isClientSide)
            {
                clearPatternInventory(player);

                AppEngInternalInventory desiredPatterns = new AppEngInternalInventory(patternInventory.size());
                desiredPatterns.fromItemContainerContents(patterns);

                // 从玩家背包将空白样板恢复出来
                Inventory playerInv = player.getInventory();
                int blankPatternsAvailable = player.getAbilities().instabuild ? Integer.MAX_VALUE
                        : playerInv.countItem(AEItems.BLANK_PATTERN.asItem());
                int blankPatternsUsed = 0;
                for (int i = 0; i < desiredPatterns.size(); i++)
                {
                    if (desiredPatterns.getStackInSlot(i).isEmpty())
                    {
                        continue;
                    }

                    IPatternDetails pattern = PatternDetailsHelper.decodePattern(desiredPatterns.getStackInSlot(i), getLevel());
                    if (pattern == null)
                    {
                        continue;
                    }

                    ++blankPatternsUsed;
                    if (blankPatternsAvailable >= blankPatternsUsed)
                    {
                        if (!patternInventory.addItems(pattern.getDefinition().toStack()).isEmpty())
                        {
                            AELog.warn("Failed to add pattern to pattern provider");
                            blankPatternsUsed--;
                        }
                    }
                }

                if (blankPatternsUsed > 0 && !player.getAbilities().instabuild)
                {
                    new PlayerInternalInventory(playerInv)
                            .removeItems(blankPatternsUsed, AEItems.BLANK_PATTERN.stack(), null);
                }

                // 如果无法恢复所有样板，则发出警告
                if (blankPatternsUsed > blankPatternsAvailable)
                {
                    player.sendSystemMessage(
                            PlayerMessages.MissingBlankPatterns.text(blankPatternsUsed - blankPatternsAvailable));
                }
            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player)
    {
        super.exportSettings(mode, builder, player);
        if(mode == SettingsFrom.MEMORY_CARD)
        {
            builder.set(AEComponents.EXPORTED_PATTERNS, patternInventory.toItemContainerContents());
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        returnInventory.writeToChildTag(data, "return_inventory", registries);
        patternInventory.writeToNBT(data, "patterns", registries);
        upgrades.writeToNBT(data, "upgrades", registries);
        data.putInt("priority", priority);
        AEKeyHelper.writeKeyAmountMap(data, "send_contents", sendContents, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        returnInventory.readFromChildTag(data, "return_inventory", registries);
        patternInventory.readFromNBT(data, "patterns", registries);
        upgrades.readFromNBT(data, "upgrades", registries);
        priority = data.getInt("priority");
        AEKeyHelper.readKeyAmountMap(data, "send_contents", sendContents, registries);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        updatePatterns();
        onUpgradesChange();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : patternInventory)
        {
            drops.add(stack);
        }
        for (ItemStack stack : upgrades)
        {
            drops.add(stack);
        }
        for (GenericStack stack : this.returnInventory.toList())
        {
            if (stack == null) continue;

            stack.what().addDrops(stack.amount(), drops, this.getLevel(),
                    this.getBlockPos());
        }
        for (Object2LongMap.Entry<AEKey> e : sendContents.object2LongEntrySet())
        {
            AEKey key = e.getKey();
            long amount = e.getLongValue();

            key.addDrops(amount, drops, this.getLevel(), this.getBlockPos());
        }
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        patternInventory.clear();
        upgrades.clear();
        returnInventory.clear();
        sendContents.clear();
    }

    // selfPowered
    @Override
    public boolean isAEPublicPowerStorage()
    {
        return false;
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return AccessRestriction.WRITE;
    }

    // IPriorityHost
    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(int newValue)
    {
        this.priority = newValue;
        setChanged();
    }

    // IUpgradeableObject
    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    // PatternContainer
    @Override
    public @Nullable IGrid getGrid()
    {
        return getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory()
    {
        return patternInventory;
    }

    @Override
    public long getTerminalSortOrder()
    {
        BlockPos blockPos = getBlockEntity().getBlockPos();
        return (long) blockPos.getZ() << 24 ^ (long) blockPos.getX() << 8 ^ blockPos.getY();
    }

    /**
     * 获取样板在终端中分类的显示组
     */
    @Override
    public PatternContainerGroup getTerminalGroup()
    {
        // 如果由自定义名称，则使用自定义名称
        if (this instanceof Nameable nameable && nameable.hasCustomName())
        {
            Component name = nameable.getCustomName();
            return new PatternContainerGroup(
                    this.getTerminalIcon(),
                    name,
                    List.of());
        }
        AEItemKey hostIcon = getTerminalIcon();
        return new PatternContainerGroup(
                hostIcon,
                hostIcon.getDisplayName(),
                List.of());
    }

    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSBlocks.METEORITE_CRAFTER_BLOCK.get());
    }

    //ICraftingProvider
    @Override
    public List<IPatternDetails> getAvailablePatterns()
    {
        return patterns;
    }

    @Override
    public int getPatternPriority()
    {
        return priority;
    }

    /**
     * 总的来说，当发送样板时，只要本轮工作数仍可用，并且周围模拟能通过插入检查，我们就认为本次工作可以完成
     */
    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder)
    {
        if (level == null || level.isClientSide()) return false;

        // 如果本轮工作轮数已经超出上限，则直接返回
        if (worksInRound >= maxWorksInRound) return false;

        // 如果样板类型不支持则返回
        if (!canAcceptPattern(patternDetails)) return false;

        // 如果能量不足则返回
        double neededEnergy = getEnergyPerWorkAfterSpeed();
        if (extractAEPower(neededEnergy, Actionable.SIMULATE) < neededEnergy)
            return false;

        // ME仓库
        @Nullable MEStorage meInv = null;
        IGrid grid = getMainNode().getGrid();
        if (grid != null)
        {
            meInv = grid.getStorageService().getInventory();
        }

        // 相邻容器的 MEStorage 包装
        @Nullable MEStorage targetInv = null;
        Direction targetDir = getPushDirection().getDirection();
        if (targetDir != null)
        {
            BlockPos neighborPos = worldPosition.relative(targetDir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            targetInv = GenericStackInvHelper.getAdjacentMeStorage(
                    level,
                    neighborPos,
                    neighborBe,
                    targetDir.getOpposite()
            );
        }

        // 先模拟：确保产物可以完全接收
        for (GenericStack result : patternDetails.getOutputs())
        {
            if (result == null || result.amount() <= 0) continue;

            long remaining = result.amount();

            if (targetInv != null)
            {
                remaining -= targetInv.insert(result.what(), remaining, Actionable.SIMULATE, this.actionSource);
            }
            if (meInv != null && remaining > 0)
            {
                remaining -= meInv.insert(result.what(), remaining, Actionable.SIMULATE, this.actionSource);
            }
            if (remaining > 0)
            {
                remaining -= returnInventory.insert(result.what(), remaining, Actionable.SIMULATE, this.actionSource);
            }

            // 三者都放不下，停止本次工作
            if (remaining > 0) return false;
        }

        // 实际执行
        for (GenericStack result : patternDetails.getOutputs())
        {
            if (result == null || result.amount() <= 0) continue;

            sendContents.addTo(result.what(), result.amount());
        }

        setChanged();
        extractAEPower(neededEnergy, Actionable.MODULATE);
        worksInRound++;
        return true;
    }

    @Override
    public boolean isBusy()
    {
        return worksInRound >= maxWorksInRound;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.METEORITE_CRAFTER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(AECSBlocks.METEORITE_CRAFTER_BLOCK.get());
    }
}
