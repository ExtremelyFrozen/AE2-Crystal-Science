package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.MachineSource;
import io.github.lounode.ae2cs.api.util.AEKeyHelper;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class MeteoritePatternProviderLogic extends PatternProviderLogic implements IUpgradeableObject
{
    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;

    /**
     * 在没有任何加速卡的情况下，我们每次合成的能量消耗
     */
    public static int energyPerWork = 50;

    /**
     * 升级槽
     */
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.METEORITE_PATTERN_PROVIDER_BLOCK, 4, this::onUpgradesChange);

    /**
     * 完成产物表，即将对外输出的物品会被临时存放在此，等待tick中再实际对外发送
     */
    Object2LongOpenHashMap<AEKey> craftedContents = new Object2LongOpenHashMap<>();

    /**
     * 本tick已经完成过的工作次数
     */
    private int worksInRound = 0;

    /**
     * 本轮最大可完成工作次数
     */
    private int maxWorksInRound = 8;


    public MeteoritePatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize)
    {
        super(mainNode, host, patternInventorySize);

        // 进行一些额外设置，并保存重要信息在子类
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY)
                .addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(mainNode::getNode);
    }

    private boolean canAcceptPattern(@Nullable IPatternDetails details)
    {
        if (details == null) return false;
        return details instanceof IMolecularAssemblerSupportedPattern;
    }

    private void onUpgradesChange()
    {
        this.maxWorksInRound = 8 << getInstalledUpgrades(AEItems.SPEED_CARD);
        this.saveChanges();
    }

    private double getEnergyPerWorkAfterSpeed()
    {
        return energyPerWork << getInstalledUpgrades(AEItems.SPEED_CARD);
    }

    private boolean workCraftedContents()
    {
        boolean worked = false;
        this.worksInRound = 0; // 每tick重置

        if (!craftedContents.isEmpty())
        {
            @Nullable MEStorage gridInv = null;

            IGrid grid = getGrid();
            if (grid != null)
            {
                gridInv = grid.getStorageService().getInventory();
            }

            // 完成sendContent内容
            ObjectIterator<Object2LongMap.Entry<AEKey>> it = craftedContents.object2LongEntrySet().iterator();
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
                long allInserted = 0;
                if (gridInv != null && remaining > 0)
                {
                    long inserted = gridInv.insert(key, remaining, Actionable.MODULATE, actionSource);
                    allInserted += inserted;
                    remaining -= inserted;
                }
                if (remaining > 0)
                {
                    long inserted = getReturnInv().insert(key, remaining, Actionable.MODULATE, actionSource);
                    allInserted += inserted;
                    remaining -= inserted;
                }

                // 更新 craftedContents并决定返回值
                if (remaining <= 0)
                {
                    it.remove();
                }
                else
                {
                    entry.setValue(remaining);
                }
                if (allInserted > 0)
                {
                    worked = true;
                    saveChanges();
                }
            }
        }
        return worked;
    }

    @Override
    public boolean isBusy()
    {
        // 显示保留，不要在这里检查工作轮数，让工作流进入pushPattern，以防设备tick无法被及时唤醒
        return super.isBusy();
    }

    /**
     * 如果是可通过陨石样板供应器快速完成的样板，则直接完成，否则回退到普通供应器逻辑
     */
    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder)
    {
        if (canAcceptPattern(patternDetails))
        {
            // 如果本轮工作轮数已经超出上限，则直接返回
            if (worksInRound >= maxWorksInRound) return false;

            // 如果能量不足则返回
            double neededEnergy = getEnergyPerWorkAfterSpeed();
            if (extractAEPowerFromGrid(neededEnergy, Actionable.SIMULATE) < neededEnergy)
                return false;


            // 实际执行
            for (GenericStack result : patternDetails.getOutputs())
            {
                if (result == null || result.amount() <= 0) continue;

                craftedContents.addTo(result.what(), result.amount());
            }

            saveChanges();
            extractAEPowerFromGrid(neededEnergy, Actionable.MODULATE);
            worksInRound++;
            // 每次推送完任务之后立刻唤醒以清空任务
            mainNode.ifPresent((iGrid, iGridNode) -> iGrid.getTickManager().alertDevice(iGridNode));
            return true;
        }
        else
        {
            return super.pushPattern(patternDetails, inputHolder);
        }
    }

    /**
     * 从网络中取一些能量
     */
    private double extractAEPowerFromGrid(double energy, Actionable actionable)
    {
        IGrid grid = getGrid();
        if (grid == null) return 0;
        IEnergyService energyService = grid.getEnergyService();
        if (energyService == null) return 0;

        return energyService.extractAEPower(energy, actionable, PowerMultiplier.ONE);
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.writeToNBT(tag, registries);
        upgrades.writeToNBT(tag, "upgrades", registries);
        AEKeyHelper.writeKeyAmountMap(tag, "crafted_contents", craftedContents, registries);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.readFromNBT(tag, registries);
        upgrades.readFromNBT(tag, "upgrades", registries);
        AEKeyHelper.readKeyAmountMap(tag, "crafted_contents", craftedContents, registries);
        onUpgradesChange();
    }

    /**
     * 对原始的Ticker类进行一个扩展，使其在操作返回仓物品的同时，能顺便把我们的合成完成物发送回网络
     */
    private class Ticker implements IGridTickable
    {
        @Override
        public TickingRequest getTickingRequest(IGridNode node)
        {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo() && craftedContents.isEmpty());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall)
        {
            if (!mainNode.isActive())
            {
                return TickRateModulation.SLEEP;
            }
            boolean couldDoWork = doWork();
            boolean workedForCrafter = workCraftedContents();
            couldDoWork = couldDoWork || workedForCrafter;
            boolean hasWorkToDo = hasWorkToDo() || !craftedContents.isEmpty();
            return hasWorkToDo ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }
}
