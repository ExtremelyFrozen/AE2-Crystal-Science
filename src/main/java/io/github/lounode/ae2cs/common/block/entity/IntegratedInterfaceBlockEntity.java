package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceLogic;

import appeng.api.AECapabilities;
import appeng.api.ids.AEComponents;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.util.SettingsFrom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 同时包含ME样板供应器与ME接口的能力。即旧版ME接口。
 * 接口形态见{@link io.github.lounode.ae2cs.common.me.part.IntegratedInterfacePart}
 */
public class IntegratedInterfaceBlockEntity extends AENetworkedBlockEntity implements IntegratedInterfaceHost {

    IntegratedInterfaceLogic logic = createLogic();
    private int priority;

    public IntegratedInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.INTEGRATED_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.getLogic().getStorageInv());
        event.registerBlockEntity(
                AECapabilities.ME_STORAGE,
                AECSBlockEntities.INTEGRATED_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.getLogic().getExposedMEStorage(direction));

        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.EX_INTEGRATED_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.getLogic().getStorageInv());
        event.registerBlockEntity(
                AECapabilities.ME_STORAGE,
                AECSBlockEntities.EX_INTEGRATED_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.getLogic().getExposedMEStorage(direction));
    }

    @Override
    public boolean isExtended() {
        return getType() == AECSBlockEntities.EX_INTEGRATED_INTERFACE_BLOCK_ENTITY.get();
    }

    protected IntegratedInterfaceLogic createLogic() {
        int size = isExtended() ? 36 : 9;
        return new IntegratedInterfaceLogic(this.getMainNode(), this, size, size);
    }

    @Override
    public IntegratedInterfaceLogic getLogic() {
        return logic;
    }

    /**
     * 获取接口对应的目标面
     */
    @Override
    public EnumSet<Direction> getTargets() {
        PushDirection pushDirection = getPushDirection();
        if (pushDirection.getDirection() == null) {
            return EnumSet.allOf(Direction.class);
        } else {
            return EnumSet.of(pushDirection.getDirection());
        }
    }

    /**
     * 可以用线缆进行连接的面，即除了目标面之外的面
     */
    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        Direction pushDirection = getPushDirection().getDirection();
        if (pushDirection == null) {
            return EnumSet.allOf(Direction.class);
        }
        return EnumSet.complementOf(EnumSet.of(pushDirection));
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.logic.onMainNodeStateChanged();
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(AECSBlocks.INTEGRATED_INTERFACE_BLOCK.get());
    }

    private PushDirection getPushDirection() {
        return getBlockState().getValue(PatternProviderBlock.PUSH_DIRECTION);
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder,
                               @Nullable Player player) {
        super.exportSettings(mode, builder, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.exportSettings(builder);

            var pushDirection = getPushDirection();
            builder.set(AEComponents.EXPORTED_PUSH_DIRECTION, pushDirection);
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input,
                               @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);

            // 恢复目标面
            PushDirection pushDirection = input.get(AEComponents.EXPORTED_PUSH_DIRECTION);
            if (pushDirection != null) {
                var level = getLevel();
                if (level != null) {
                    level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(
                            PatternProviderBlock.PUSH_DIRECTION,
                            pushDirection));
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.logic.save(data, registries);
        data.putInt("priority", this.priority);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.logic.load(data, registries);
        this.priority = data.getInt("priority");
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        setChanged();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        getLogic().addDrops(drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        getLogic().cleanContent();
    }
}
