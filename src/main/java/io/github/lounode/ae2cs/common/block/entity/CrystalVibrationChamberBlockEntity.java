package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.ConfigInventory;
import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import io.github.lounode.ae2cs.common.machine.component.GenericStackInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@ProvideCaps(GenericInternalInventory.class)
public class CrystalVibrationChamberBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
        CustomReturnableSubMenuHost
{
    private final IUpgradeInventory upgrades;

    private int maxBurnTime = 0;
    private int remainingBurnTime = 0;
    private double energyPerTick = 0;
    private int speedCards = 0;

    public CrystalVibrationChamberBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.CRYSTAL_VIBRATION_CHAMBER_BLOCK_ENTITY.get(), pos, blockState,
                1000000, false, AccessRestriction.READ);
        this.getMainNode().setIdlePowerUsage(0);

        this.upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK, 3, this::onUpgradesChange);

        ConfigInventory inv = ConfigInventory.storage(1)
                .slotFilter(input -> input.getPrimaryKey() instanceof PureCrystalItem)
                .changeListener(this::saveChanges)
                .build();
        inv.useRegisteredCapacities();

        GenericStackInvComponent component = new GenericStackInvComponent();
        component.addPort(InvPort.WORK, inv);
        getMachineComponents().add(component);
        getMachineComponents().add(new SideConfigComponent());
    }

    public GenericStackInv getInv()
    {
        return getMachineComponents().getService(GenericStackInvComponent.class).port(InvPort.WORK);
    }

    public int getMaxBurnTime()
    {
        return maxBurnTime;
    }

    public int getRemainingBurnTime()
    {
        return remainingBurnTime;
    }

    public double getEnergyPerTick()
    {
        return energyPerTick;
    }

    public void checkActive(boolean active)
    {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AECSBlockProperties.ACTIVE) && state.getValue(AECSBlockProperties.ACTIVE) != active)
        {
            level.setBlock(worldPosition, getBlockState().setValue(AECSBlockProperties.ACTIVE, active), 2);
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction dir)
    {
        return AECableType.COVERED;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries)
    {
        super.saveAdditional(data, registries);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        data.putInt("max_burn_time", this.maxBurnTime);
        data.putInt("burn_time", this.remainingBurnTime);
        data.putDouble("energy_per_tick", this.energyPerTick);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.maxBurnTime = data.getInt("max_burn_time");
        this.remainingBurnTime = data.getInt("burn_time");
        this.energyPerTick = data.getDouble("energy_per_tick");
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        onUpgradesChange();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades)
        {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        upgrades.clear();
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return this.upgrades;
    }

    protected void onUpgradesChange()
    {
        this.speedCards = upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
        saveChanges();
    }

    @Override
    public void serverTick()
    {
        // 每tick执行燃烧产能逻辑
        // 分为三部分
        // 1-查看当前槽位的物品，如果为合适物品就消耗掉1个，填充燃烧数据（如果正在燃烧则跳过此步）
        // 2-尝试将目前能量注入ae网络
        // 3-执行燃烧逻辑，产生固定数量能量，并减少tick，即使能量已满也继续燃烧
        // 其中，父类已经包含了将能量注入的逻辑

        super.serverTick();

        // 查看当前槽位的物品，如果为合适物品就消耗掉1个，填充燃烧数据（如果正在燃烧则跳过此步）
        if (remainingBurnTime <= 0)
        {
            GenericStack fuel = getInv().getStack(0);
            if (fuel != null
                    && fuel.what().getPrimaryKey() instanceof PureCrystalItem pureCrystalItem
                    && fuel.amount() > 0)
            {
                long extracted = getInv().extract(0, fuel.what(), 1, Actionable.MODULATE);
                if (extracted > 0)
                {
                    this.remainingBurnTime = pureCrystalItem.getBurnTime();
                    this.maxBurnTime = pureCrystalItem.getBurnTime();
                    this.energyPerTick = pureCrystalItem.getEnergyPerTick();
                }
            }
        }

        // 执行燃烧逻辑
        if (remainingBurnTime > 0)
        {
            remainingBurnTime -= getSpeedupBurnTimeCost();

            injectAEPower(this.getSpeedupEnergyPerTick(), Actionable.MODULATE);

            if (remainingBurnTime <= 0)
                clearBurnState();
        }

        // 整备方块状态
        checkActive(remainingBurnTime > 0);
    }

    private double getSpeedupEnergyPerTick()
    {
        return this.energyPerTick * (1 + 0.5 * speedCards);
    }

    private int getSpeedupBurnTimeCost()
    {
        return (1 + speedCards);
    }

    private void clearBurnState()
    {
        this.remainingBurnTime = 0;
        this.energyPerTick = 0;
        this.maxBurnTime = 0;
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return new ItemStack(getItemFromBlockEntity());
    }
}
