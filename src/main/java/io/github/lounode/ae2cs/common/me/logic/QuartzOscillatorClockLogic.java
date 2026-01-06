package io.github.lounode.ae2cs.common.me.logic;

import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import io.github.lounode.ae2cs.api.settings.AECSSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class QuartzOscillatorClockLogic implements IUpgradeableObject, IConfigurableObject
{
    /**
     * 脉冲宽度
     */
    private static final int PULSE_WIDTH_TICKS = 1;

    /**
     * 输出翻转后，输入采样冷却（避免输出回灌）
     */
    private static final int INPUT_SAMPLE_COOLDOWN_TICKS = 2;

    /**
     * 脉冲结束的世界时间
     */
    private long pulseEndGameTime = -1;

    protected final QuartzOscillatorClockHost host;
    protected final IManagedGridNode mainNode;
    protected final IUpgradeInventory upgrades;
    private final IConfigManager cm;

    private int currentHold = 10;
    private int currentCountDown = currentHold;
    private RedstoneMode currentRedStoneMode = RedstoneMode.IGNORE;

    /**
     * 读档/初始化时不直接碰世界层输出，在tick里统一同步
     */
    private boolean pendingPulseSync = false;

    /**
     * 外部红石输入缓存
     */
    private boolean cachedExternalPowered = false;

    /**
     * 输出翻转后若干tick内，不采样输入
     */
    private int inputSampleCooldown = 0;

    public QuartzOscillatorClockLogic(IManagedGridNode gridNode, QuartzOscillatorClockHost host, Item is)
    {
        this.host = host;
        this.mainNode = gridNode
                .setFlags()
                .addService(IGridTickable.class, new Ticker());

        this.cm = IConfigManager.builder(this::onConfigManagerChange)
                .registerSetting(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE, RedstoneMode.IGNORE)
                .build();

        this.upgrades = UpgradeInventories.forMachine(is, 1, this::onUpgradesChange);
    }

    public int getCurrentHold()
    {
        return currentHold;
    }

    public void setCurrentHold(int currentHold)
    {
        int newHold = Math.max(1, currentHold);
        newHold = Math.min(newHold, 12000);
        if (this.currentHold == newHold) return;

        this.currentHold = newHold;
        this.currentCountDown = this.currentHold;

        this.host.saveChanges();
    }

    private void onUpgradesChange()
    {
        if (this.upgrades.isInstalled(AEItems.REDSTONE_CARD))
        {
            this.currentRedStoneMode = this.cm.getSetting(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE);
        }
        else
        {
            this.currentRedStoneMode = RedstoneMode.IGNORE;
        }
        this.host.saveChanges();
    }

    private void onConfigManagerChange()
    {
        if (!this.upgrades.isInstalled(AEItems.REDSTONE_CARD))
        {
            return;
        }

        this.currentRedStoneMode = this.cm.getSetting(AECSSettings.REDSTONE_CONTROLLED_NO_PULSE);
        this.host.saveChanges();
    }

    private boolean isPulsing(Level level)
    {
        return this.pulseEndGameTime >= 0 && level.getGameTime() < this.pulseEndGameTime;
    }

    /**
     * 获取外部红石输入：
     * - 自己正在输出（或刚翻转输出）时，不采样邻居信号，直接返回缓存值，避免回灌死锁
     * - 自己不输出时：允许采样并刷新缓存
     */
    private boolean getExternalPowered(Level level, BlockPos pos)
    {
        // 脉冲期间不采样，避免读到自己输出形成的回灌
        if (this.pulseEndGameTime >= 0)
        {
            return this.cachedExternalPowered;
        }

        // 输出刚翻转后的冷却期不采样
        if (this.inputSampleCooldown > 0)
        {
            this.inputSampleCooldown--;
            return this.cachedExternalPowered;
        }

        boolean powered = level.hasNeighborSignal(pos);
        this.cachedExternalPowered = powered;
        return powered;
    }

    private boolean isAllowedByRedstoneMode(Level level, BlockPos pos)
    {
        if (this.currentRedStoneMode == RedstoneMode.IGNORE)
        {
            return true;
        }

        boolean powered = getExternalPowered(level, pos);

        return switch (this.currentRedStoneMode)
        {
            case HIGH_SIGNAL -> powered;
            case LOW_SIGNAL -> !powered;
            default -> true;
        };
    }

    private void beginPulse(Level level)
    {
        if (isPulsing(level))
        {
            return;
        }

        this.pulseEndGameTime = level.getGameTime() + Math.max(1, PULSE_WIDTH_TICKS);

        // 具体输出交给host
        this.host.setPulseActive(true);

        // 短暂不采样输入
        this.inputSampleCooldown = INPUT_SAMPLE_COOLDOWN_TICKS;
    }

    private void endPulse()
    {
        if (this.pulseEndGameTime < 0)
        {
            return;
        }

        this.pulseEndGameTime = -1;
        this.host.setPulseActive(false);

        // 短暂不采样输入
        this.inputSampleCooldown = INPUT_SAMPLE_COOLDOWN_TICKS;
    }

    private void tickServer(Level level, BlockPos pos, int ticksSinceLastCall)
    {
        int dt = Math.max(1, ticksSinceLastCall);

        // 统一在首次server tick把世界输出对齐为未脉冲
        if (this.pendingPulseSync)
        {
            this.pendingPulseSync = false;

            this.pulseEndGameTime = -1;
            this.host.setPulseActive(false);

            // 添加一点冷却
            this.inputSampleCooldown = INPUT_SAMPLE_COOLDOWN_TICKS;
        }

        // 收尾脉冲
        if (this.pulseEndGameTime >= 0 && level.getGameTime() >= this.pulseEndGameTime)
        {
            endPulse();
        }

        // 红石模式不允许：不走倒计时、不触发新脉冲
        if (!isAllowedByRedstoneMode(level, pos))
        {
            return;
        }

        // 满足红石模式：走倒计时
        int hold = Math.max(1, this.currentHold);

        // 保证倒计时在合理范围
        if (this.currentCountDown <= 0 || this.currentCountDown > hold)
        {
            this.currentCountDown = hold;
        }

        this.currentCountDown -= dt;

        if (this.currentCountDown <= 0)
        {
            // 触发一次脉冲
            beginPulse(level);

            int overshoot = -this.currentCountDown;
            int rem = overshoot % hold;
            this.currentCountDown = (rem == 0) ? hold : (hold - rem);
        }
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        this.upgrades.writeToNBT(tag, "upgrades", registries);
        this.cm.writeToNBT(tag, registries);

        tag.putInt("current_hold", this.currentHold);
        tag.putInt("current_countdown", this.currentCountDown);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries)
    {
        this.upgrades.readFromNBT(tag, "upgrades", registries);
        this.cm.readFromNBT(tag, registries);

        this.currentHold = Math.max(1, tag.getInt("current_hold"));
        this.currentCountDown = tag.getInt("current_countdown");

        // 重置红石状态
        onUpgradesChange();

        // 读档时 逻辑层强制关闭脉冲
        this.pulseEndGameTime = -1;

        // 标记需要重置状态
        this.pendingPulseSync = true;

        // 输入采样缓存重置
        this.cachedExternalPowered = false;
        this.inputSampleCooldown = INPUT_SAMPLE_COOLDOWN_TICKS;

        int hold = Math.max(1, this.currentHold);
        if (this.currentCountDown <= 0 || this.currentCountDown > hold)
        {
            this.currentCountDown = hold;
        }
    }

    @Override
    public IConfigManager getConfigManager()
    {
        return this.cm;
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return this.upgrades;
    }

    private class Ticker implements IGridTickable
    {
        @Override
        public TickingRequest getTickingRequest(IGridNode node)
        {
            return new TickingRequest(TickRates.IOPort, false);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall)
        {
            var be = host.getBlockEntity();
            var level = be.getLevel();

            if (level == null || level.isClientSide)
            {
                return TickRateModulation.IDLE;
            }

            var pos = be.getBlockPos();
            tickServer(level, pos, ticksSinceLastCall);

            if (isPulsing(level))
            {
                return TickRateModulation.URGENT;
            }

            if (isAllowedByRedstoneMode(level, pos))
            {
                return TickRateModulation.URGENT;
            }

            return TickRateModulation.SAME;
        }
    }
}