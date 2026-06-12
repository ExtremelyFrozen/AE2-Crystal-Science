package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.api.util.AEKeyHelper;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.util.KeyCounterHelper;

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
import appeng.me.helpers.MachineSource;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MeteoritePatternProviderLogic extends PatternProviderLogic implements IUpgradeableObject {

    private final IManagedGridNode mainNode;
    private final IActionSource actionSource;
    private final MeteoritePatternProviderHost meteoriteHost;

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

    /**
     * 缓存表上限
     */
    private static final int OUTPUT_CACHE_LIMIT = 10;

    /**
     * 快速缓存，用于对同一个样板进行快速判定
     */
    private final Reference2ObjectArrayMap<IPatternDetails, List<GenericStack>> outputCache = new Reference2ObjectArrayMap<>(OUTPUT_CACHE_LIMIT);

    /**
     * 记录缓存顺序，用于顺序清理
     */
    private final ReferenceArrayList<IPatternDetails> outputCacheOrder = new ReferenceArrayList<>(OUTPUT_CACHE_LIMIT + 4);

    public MeteoritePatternProviderLogic(IManagedGridNode mainNode, MeteoritePatternProviderHost host, int patternInventorySize) {
        super(mainNode, host, patternInventorySize);

        this.meteoriteHost = host;
        // 进行一些额外设置，并保存重要信息在子类
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY)
                .addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(mainNode::getNode);
    }

    private boolean canAcceptPattern(@Nullable IPatternDetails details) {
        if (details == null) return false;
        return details instanceof IMolecularAssemblerSupportedPattern;
    }

    private void onUpgradesChange() {
        this.maxWorksInRound = 8 << getInstalledUpgrades(AEItems.SPEED_CARD);
        this.saveChanges();
    }

    private double getEnergyPerWorkAfterSpeed() {
        return energyPerWork << getInstalledUpgrades(AEItems.SPEED_CARD);
    }

    private boolean workCraftedContents() {
        boolean worked = false;
        this.worksInRound = 0; // 每tick重置

        if (!craftedContents.isEmpty()) {
            @Nullable
            MEStorage gridInv = null;

            IGrid grid = getGrid();
            if (grid != null) {
                gridInv = grid.getStorageService().getInventory();
            }

            // 完成sendContent内容
            ObjectIterator<Object2LongMap.Entry<AEKey>> it = craftedContents.object2LongEntrySet().iterator();
            while (it.hasNext()) {
                Object2LongMap.Entry<AEKey> entry = it.next();
                AEKey key = entry.getKey();
                long remaining = entry.getLongValue();
                if (key == null || remaining <= 0) {
                    it.remove();
                    continue;
                }

                // 尝试塞入可用库存
                long allInserted = 0;
                if (gridInv != null) {
                    long inserted = gridInv.insert(key, remaining, Actionable.MODULATE, actionSource);
                    allInserted += inserted;
                    remaining -= inserted;
                }
                if (remaining > 0) {
                    long inserted = getReturnInv().insert(key, remaining, Actionable.MODULATE, actionSource);
                    allInserted += inserted;
                    remaining -= inserted;
                }

                // 更新 craftedContents并决定返回值
                if (remaining <= 0) {
                    it.remove();
                } else {
                    entry.setValue(remaining);
                }
                if (allInserted > 0) {
                    worked = true;
                }
            }
        }

        if (worked)
            saveChanges();
        return worked;
    }

    @Override
    public boolean isBusy() {
        // 显示保留，不要在这里检查工作轮数，让工作流进入pushPattern，以防设备tick无法被及时唤醒
        return super.isBusy();
    }

    /**
     * 如果是可通过陨石样板供应器快速完成的样板，则直接完成，否则回退到普通供应器逻辑
     */
    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        // 如果本轮工作轮数已经超出上限，则直接返回
        if (worksInRound >= maxWorksInRound) return false;

        // 检查样板
        if (!canAcceptPattern(patternDetails)) {
            return super.pushPattern(patternDetails, inputHolder);
        }
        if (!(patternDetails instanceof IMolecularAssemblerSupportedPattern pattern)) {
            return super.pushPattern(patternDetails, inputHolder);
        }

        // 如果能量不足则返回
        double neededEnergy = getEnergyPerWorkAfterSpeed();
        if (!tryConsumeEnergyFromGrid(neededEnergy))
            return false;

        // 记录一下当前发送表状态
        boolean wasEmpty = craftedContents.isEmpty();

        // 这里获取的output包括产物与剩余物（如空桶）
        List<GenericStack> output = getMolecularAssemblerSupportedPatternOutput(pattern, inputHolder);
        if (output == null) return false;

        // 将产物与剩余物添加入表
        for (GenericStack stack : output) {
            if (stack == null || stack.what() == null || stack.amount() <= 0) continue;
            craftedContents.addTo(stack.what(), stack.amount());
        }

        // 提交、计数、唤醒ticker
        saveChanges();
        worksInRound++;

        if (wasEmpty && !craftedContents.isEmpty())
            mainNode.ifPresent((iGrid, iGridNode) -> iGrid.getTickManager().alertDevice(iGridNode));
        return true;
    }

    /**
     * 尝试从网络中扣除指定数量的能量，如果不足不扣除
     */
    private boolean tryConsumeEnergyFromGrid(double energy) {
        IGrid grid = getGrid();
        if (grid == null) return false;
        IEnergyService energyService = grid.getEnergyService();
        if (energyService == null) return false;

        double extracted = energyService.extractAEPower(energy, Actionable.MODULATE, PowerMultiplier.ONE);
        if (extracted + 1.0e-9 >= energy) {
            return true;
        }

        try {
            energyService.injectPower(extracted, Actionable.MODULATE);
        } catch (Throwable ignored) {
            // 默许损耗
        }
        return false;
    }

    @Nullable
    private List<GenericStack> getMolecularAssemblerSupportedPatternOutput(IMolecularAssemblerSupportedPattern pattern, KeyCounter[] inputHolder) {
        List<GenericStack> cachedOutput = outputCache.get(pattern);
        if (cachedOutput != null) return cachedOutput;

        // 提供注册表信息，用于后续assemble实际输出
        var level = meteoriteHost.getBlockEntity().getLevel();
        if (level == null) {
            return null;
        }

        // 计算真实输出和剩余物
        final ItemStack[] grid3x3 = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid3x3[i] = ItemStack.EMPTY;
        }
        try {
            KeyCounter[] inputHolderCopy = new KeyCounter[inputHolder.length];
            for (int i = 0; i < inputHolder.length; i++) {
                KeyCounter kc = inputHolder[i];
                inputHolderCopy[i] = (kc == null) ? new KeyCounter() : KeyCounterHelper.deepCopy(kc);
            }
            pattern.fillCraftingGrid(inputHolderCopy, (slot, stack) -> {
                if (slot >= 0 && slot < 9) {
                    grid3x3[slot] = (stack == null) ? ItemStack.EMPTY : stack;
                }
            });
        } catch (RuntimeException e) {
            // 出现任何异常，此时便不稳定，直接返回null
            return null;
        }

        // 压缩边距
        int minX = 3, minY = 3, maxX = -1, maxY = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = grid3x3[slot];
            if (stack != null && !stack.isEmpty()) {
                int x = slot % 3;
                int y = slot / 3;
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }

        if (maxX < 0) {
            return null;
        }

        final int width = (maxX - minX + 1);
        final int height = (maxY - minY + 1);

        final List<ItemStack> compressedItems = new ArrayList<>(width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcSlot = (minX + x) + (minY + y) * 3;
                ItemStack stack = grid3x3[srcSlot];
                compressedItems.add(stack == null ? ItemStack.EMPTY : stack);
            }
        }

        final CraftingInput input = CraftingInput.of(width, height, compressedItems);

        ItemStack output = pattern.assemble(input, level);
        if (output == null || output.isEmpty()) {
            // 无输出
            return null;
        }
        NonNullList<ItemStack> remainders = pattern.getRemainingItems(input);

        // 构造结果
        List<GenericStack> finalOutput = new ArrayList<>();
        GenericStack outputStack = GenericStack.fromItemStack(output);
        if (outputStack != null) {
            finalOutput.add(outputStack);
        }
        for (ItemStack stack : remainders) {
            GenericStack remainingStack = GenericStack.fromItemStack(stack);
            if (remainingStack != null) {
                finalOutput.add(remainingStack);
            }
        }

        // 写入并清理缓存
        outputCache.put(pattern, finalOutput);
        outputCacheOrder.add(pattern);
        while (outputCache.size() > OUTPUT_CACHE_LIMIT && !outputCacheOrder.isEmpty()) {
            IPatternDetails oldest = outputCacheOrder.removeFirst();
            outputCache.remove(oldest);
        }
        return finalOutput;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        upgrades.writeToNBT(tag, "upgrades", registries);
        AEKeyHelper.writeKeyAmountMap(tag, "crafted_contents", craftedContents, registries);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        upgrades.readFromNBT(tag, "upgrades", registries);
        AEKeyHelper.readKeyAmountMap(tag, "crafted_contents", craftedContents, registries);
        onUpgradesChange();
    }

    @Override
    public void addDrops(List<ItemStack> drops) {
        super.addDrops(drops);
        for (ItemStack stack : upgrades) {
            drops.add(stack);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }

    /**
     * 对原始的Ticker类进行一个扩展，使其在操作返回仓物品的同时，能顺便把我们的合成完成物发送回网络
     */
    private class Ticker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, !hasWorkToDo() && craftedContents.isEmpty());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean couldDoWork = doWork();
            boolean workedForCrafter = workCraftedContents();
            couldDoWork = couldDoWork || workedForCrafter;
            boolean hasWorkToDo = hasWorkToDo() || !craftedContents.isEmpty();
            return hasWorkToDo ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER : TickRateModulation.SLEEP;
        }
    }
}
