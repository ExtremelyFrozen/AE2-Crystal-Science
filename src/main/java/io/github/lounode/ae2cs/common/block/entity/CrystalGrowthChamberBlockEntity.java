package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSTags;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class CrystalGrowthChamberBlockEntity extends AENetworkedPoweredBlockEntity implements IUpgradeableObject,
        ServerTickingBlockEntity
{
    /**
     * 晶体催生仓提供的基础生长值
     */
    private static final int growthProgressBase = 1;

    /**
     * 每张加速卡提供的生长值
     */
    private static final int growthProgressPerSpeedCard = 1;

    /**
     * 周围每个催生器提供的生长值
     */
    private static final int growthProgressPerGrowth = 1;

    /**
     * 每个工作消耗的AE能量，每张加速卡再额外增加一次该数值
     */
    private static final double energyPerGrowth = 100;

    // 升级卡仓 4卡槽 包含四个加速卡
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK, 4, this::onUpgradesChanged);

    // 催生槽位
    private final AppEngInternalInventory inventory = new AppEngInternalInventory(54)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    // 暴露能力
    private final FilteredInternalInventory outputInv = new FilteredInternalInventory(inventory, new IAEItemFilter()
    {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
        {
            return stack != null && !stack.isEmpty() && stack.is(AECSTags.CRYSTAL_SEEDS);
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount)
        {
            return inv.getStackInSlot(slot).is(AECSTags.PURIFIED_CRYSTAL);
        }
    });

    private int lastWorkTick = 0;

    public CrystalGrowthChamberBlockEntity(BlockPos pos, BlockState state)
    {
        super(AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(), pos, state);

        getMainNode().setIdlePowerUsage(0.0) // 0待机消耗，我们从能量管道中获取能量
                .setFlags(GridFlags.CANNOT_CARRY) // 此节点不允许传输频道
                .setExposedOnSides(EnumSet.allOf(Direction.class)); // 可以用于连接的方向

        inventory.setFilter(new IAEItemFilter()
        {
            // 这里限定只允许水晶种子进入，但不限制输出，以便UI中能手动拿取任意物品
            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack)
            {
                return stack != null && !stack.isEmpty() && stack.is(AECSTags.CRYSTAL_SEEDS);
            }
        });

        // 给予8wFE缓存（即4wAE），不允许对外输出能量，只准输入
        setInternalMaxPower(40000);
        setInternalPowerFlow(AccessRestriction.WRITE);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(),
                AEBasePoweredBlockEntity::getEnergyStorage
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                AECSBlockEntities.CRYSTAL_GROWTH_CHAMBER.get(),
                (be, direction) -> be.outputInv.toItemHandler()
        );
    }

    /**
     * 取当前网络的MEStorage
     */
    public @Nullable MEStorage getNetworkInventory()
    {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return null;
        IStorageService ss = grid.getStorageService();
        return ss != null ? ss.getInventory() : null;
    }

    /**
     * 存储槽
     */
    @Override
    public InternalInventory getInternalInventory()
    {
        return this.inventory;
    }

    /* 对外返回升级仓 */
    @Override
    public IUpgradeInventory getUpgrades()
    {
        return upgrades;
    }

    // inv的save与load均已由AEBaseInvBlockEntity处理
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        this.upgrades.writeToNBT(tag, "interface_upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadTag(tag, registries);
        this.upgrades.readFromNBT(tag, "interface_upgrades", registries);
    }

    // load完成之后，且level被注入后
    @Override
    public void onLoad()
    {
        super.onLoad();
        onUpgradesChanged(); // 加载后刷新一次升级状态
    }

    private void onUpgradesChanged()
    {
        setChanged();
    }

    @Override
    public void serverTick()
    {
        if (level == null || level.isClientSide) return;

        ++lastWorkTick;
        if (lastWorkTick < 10) return; // 每10tick工作一次

        tickEnergyWithME();

        // TODO 周围的催生器数量
        int growthNum = 0;

        int speedCard = upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
        double energyCost = (1 + speedCard) * energyPerGrowth;
        if (getAECurrentPower() < energyCost) return;

        int growthTick = growthProgressBase + speedCard * growthProgressPerSpeedCard + growthNum * growthProgressPerGrowth;

        boolean worked = false;
        for (int i = 0; i < inventory.size(); ++i)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(AECSTags.CRYSTAL_SEEDS)) continue;

            worked = true;
            ItemStack result = CrystalSeedItem.grow(stack, growthTick * 10); // 每10tick运行一次，乘10以补足
            inventory.setItemDirect(i, result);
        }
        if (worked)
            extractAEPower(energyCost, Actionable.MODULATE);

        setChanged();
    }

    /**
     * 如果连接到ae，则每次工作前会尝试从ae补充电量
     */
    private void tickEnergyWithME()
    {
        final IGrid grid = this.getMainNode().getGrid();
        if (grid == null) return;
        final IEnergyService energy = grid.getEnergyService();
        if (energy == null) return;

        double need = Math.max(0, getAEMaxPower() - getAECurrentPower());
        if (need <= 0) return;

        double had = energy.extractAEPower(need, Actionable.MODULATE, PowerMultiplier.ONE);

        double insertEnergy = Math.min(need, had);

        this.injectAEPower(insertEnergy, Actionable.MODULATE);
    }


    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        // 继承链中AEBaseInvBlockEntity已经处理inv的掉落，这里只处理升级槽
        super.addAdditionalDrops(level, pos, drops);

        for (int i = 0; i < upgrades.size(); i++)
        {
            ItemStack slotContent = upgrades.getStackInSlot(i);
            if (slotContent.isEmpty()) continue;
            drops.add(slotContent.copy());
        }
    }
}
