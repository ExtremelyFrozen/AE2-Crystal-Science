package io.github.lounode.ae2cs.common.block.entity;

import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import io.github.lounode.ae2cs.common.machine.MachineContext;

import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.util.SettingsFrom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AENetworkedComponentBlockEntity extends AENetworkBlockEntity implements IMachineHost,
                                             ServerTickingBlockEntity, ClientTickingBlockEntity {

    private final MachineComponentContainer machineComponents;

    public AENetworkedComponentBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.machineComponents = new MachineComponentContainer(this);
    }

    @Override
    public MachineComponentContainer getMachineComponents() {
        return this.machineComponents;
    }

    @Override
    public @Nullable Level getLevel() {
        return this.level;
    }

    @Override
    public void markChanged() {
        this.saveChanges();
    }

    /**
     * 同步1.21.1的AENetworkBlockEntity相关实现
     */
    @Override
    public void markForClientUpdate() {
        this.requestModelDataUpdate();

        if (this.level != null && !this.isRemoved() && !notLoaded()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void invalidCap() {
        this.invalidateCaps();
    }

    @Override
    public void updateBlockState(BlockState newState, int flags) {
        if (level != null) {
            level.setBlock(getBlockPos(), newState, flags);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        machineComponents.onLoad(new MachineContext(this, level, worldPosition, getBlockState()));
    }

    @Override
    public void serverTick() {
        machineComponents.onServerTick(new MachineContext(this, level, worldPosition, getBlockState()));
    }

    @Override
    public void clientTick() {
        machineComponents.onClientTick(new MachineContext(this, level, worldPosition, getBlockState()));
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        machineComponents.importSettings(new MachineContext(this, level, worldPosition, getBlockState()), input, player);
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag builder, @Nullable Player player) {
        super.exportSettings(mode, builder, player);
        machineComponents.exportSettings(new MachineContext(this, level, worldPosition, getBlockState()), builder, player);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        machineComponents.writeNbt(data);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        machineComponents.readNbt(data);
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        machineComponents.writeStream(data);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        boolean result = super.readFromStream(data);
        result |= machineComponents.readStream(data);
        return result;
    }

    @Override
    public void clearContent() {
        super.clearContent();
        machineComponents.clearContent();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        machineComponents.addDrops(level, pos, drops);
    }
}
