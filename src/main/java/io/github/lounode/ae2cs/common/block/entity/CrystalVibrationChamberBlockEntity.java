package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.util.ConfigInventory;
import io.github.lounode.ae2cs.api.util.ForgeEnergyAdapterUpgrade;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

public class CrystalVibrationChamberBlockEntity extends AENetworkedSelfPoweredBlockEntity implements IUpgradeableObject,
        ServerTickingBlockEntity
{
    private final ConfigInventory inv;

    private final IUpgradeInventory upgrades;

    private int maxBurnTime = 0;
    private int remainingBurnTime = 0;
    private double energyPerTick = 0;

    public CrystalVibrationChamberBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(AECSBlockEntities.CRYSTAL_VIBRATION_CHAMBER_BLOCK_ENTITY.get(), pos, blockState, 1000000);
        this.getMainNode().setIdlePowerUsage(0);

        this.upgrades = UpgradeInventories.forMachine(AECSBlocks.CRYSTAL_VIBRATION_CHAMBER_BLOCK, 3, this::saveChanges);

        inv = ConfigInventory.storage(1)
                .slotFilter(input -> input.getPrimaryKey() instanceof PureCrystalItem)
                .changeListener(this::saveChanges)
                .build();
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AECSBlockEntities.CRYSTAL_VIBRATION_CHAMBER_BLOCK_ENTITY.get(),
                (be, unused) -> be
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                AECSBlockEntities.CRYSTAL_VIBRATION_CHAMBER_BLOCK_ENTITY.get(),
                (be, direction) -> new ForgeEnergyAdapterUpgrade(be, AccessRestriction.READ)
        );
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.CRYSTAL_VIBRATION_CHAMBER_BLOCK_ENTITY.get(),
                (be, direction) -> be.inv
        );
    }

    public ConfigInventory getInv()
    {
        return inv;
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
        this.inv.writeToChildTag(data, "inv", registries);
        data.putInt("max_burn_time", this.maxBurnTime);
        data.putInt("burn_time", this.remainingBurnTime);
        data.putDouble("energy_per_tick", this.energyPerTick);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries)
    {
        super.loadTag(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        this.inv.readFromChildTag(data, "inv", registries);
        this.maxBurnTime = data.getInt("max_burn_time");
        this.remainingBurnTime = data.getInt("burn_time");
        this.energyPerTick = data.getDouble("energy_per_tick");
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades)
        {
            drops.add(upgrade);
        }

        for (GenericStack stack : this.inv.toList())
        {
            if (stack == null) continue;
            stack.what().addDrops(stack.amount(), drops, getLevel(), getBlockPos());
        }
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        upgrades.clear();
        inv.clear();
    }

    @Override
    public IUpgradeInventory getUpgrades()
    {
        return this.upgrades;
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
            GenericStack fuel = inv.getStack(0);
            if (fuel != null
                    && fuel.what().getPrimaryKey() instanceof PureCrystalItem pureCrystalItem
                    && fuel.amount() > 0)
            {
                long extracted = inv.extract(0, fuel.what(), 1, Actionable.MODULATE);
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
            remainingBurnTime--;

            injectAEPower(this.energyPerTick, Actionable.MODULATE);

            if (remainingBurnTime <= 0)
                clearBurnState();
        }

        // 整备方块状态
        checkActive(remainingBurnTime > 0);
    }

    private void clearBurnState()
    {
        this.remainingBurnTime = 0;
        this.energyPerTick = 0;
        this.maxBurnTime = 0;
    }

    @Override
    public boolean isAEPublicPowerStorage()
    {
        return false;
    }

    @Override
    public AccessRestriction getPowerFlow()
    {
        return AccessRestriction.READ;
    }
}
