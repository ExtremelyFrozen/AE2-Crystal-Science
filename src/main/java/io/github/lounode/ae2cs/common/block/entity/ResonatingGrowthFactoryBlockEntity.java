package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.api.cap.ProvideCaps;
import io.github.lounode.ae2cs.api.submenu.CustomReturnableSubMenuHost;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlockProperties;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import io.github.lounode.ae2cs.common.inventory.ResonatingFactoryUpgradeInventory;
import io.github.lounode.ae2cs.common.inventory.ResonatingStackingInventory;
import io.github.lounode.ae2cs.common.machine.component.AppEngInvComponent;
import io.github.lounode.ae2cs.common.machine.component.InvPort;
import io.github.lounode.ae2cs.common.machine.component.SideConfigComponent;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.util.inv.AppEngInternalInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 48 个真实槽位暂不限制物品；库存由机器组件负责保存、清空与掉落。
 * 通电外观跟随 ME 网络供电状态，不消耗加工能量，也不要求频道。
 */
@ProvideCaps(IItemHandler.class)
public class ResonatingGrowthFactoryBlockEntity extends AENetworkedComponentBlockEntity
                                                implements IUpgradeableObject, CustomReturnableSubMenuHost {

    public static final int INVENTORY_SIZE = 48;

    private final IUpgradeInventory upgrades = new ResonatingFactoryUpgradeInventory(
            AECSBlocks.RESONATING_GROWTH_FACTORY_BLOCK, this::saveChanges,
            cards -> ResonatingStackingInventory.canReduceCapacity(cards, getInventory()));
    private final ResonatingStackingInventory inventory = new ResonatingStackingInventory(
            INVENTORY_SIZE, this::getStackCapacity, this::saveChanges);

    public ResonatingGrowthFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(AECSBlockEntities.RESONATING_GROWTH_FACTORY_BLOCK_ENTITY.get(), pos, state);
        getMainNode().setIdlePowerUsage(0);
        getMachineComponents().add(new AppEngInvComponent().addPort(InvPort.WORK, inventory));
        getMachineComponents().add(new SideConfigComponent());
    }

    public AppEngInternalInventory getInventory() {
        return inventory;
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getItemFromBlockEntity());
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public int getStackCapacity() {
        return 64 << (2 * upgrades.getInstalledUpgrades(AECSItems.RESONATING_STACKING_CARD));
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        List<ItemStack> raw = new ArrayList<>();
        super.addAdditionalDrops(level, pos, raw);
        ResonatingStackingInventory.splitDrops(raw, drops);
        for (ItemStack stack : upgrades) drops.add(stack.copy());
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (level == null || level.isClientSide()) return;
        var node = getMainNode().getNode();
        boolean powered = node != null && node.isPowered();
        BlockState state = getBlockState();
        if (state.getValue(AECSBlockProperties.ACTIVE) != powered) {
            level.setBlock(worldPosition, state.setValue(AECSBlockProperties.ACTIVE, powered), 3);
        }
    }
}
