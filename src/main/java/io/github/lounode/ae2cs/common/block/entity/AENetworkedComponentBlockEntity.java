package io.github.lounode.ae2cs.common.block.entity;

import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.util.SettingsFrom;
import io.github.lounode.ae2cs.common.machine.IMachineHost;
import io.github.lounode.ae2cs.common.machine.MachineComponentContainer;
import io.github.lounode.ae2cs.common.machine.MachineContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AENetworkedComponentBlockEntity extends AENetworkedBlockEntity implements IMachineHost,
        ServerTickingBlockEntity, ClientTickingBlockEntity
{
    private final MachineComponentContainer machineComponents;

    public AENetworkedComponentBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
        this.machineComponents = new MachineComponentContainer(this);
    }

    @Override
    public MachineComponentContainer getMachineComponents()
    {
        return this.machineComponents;
    }

    @Override
    public @Nullable Level getLevel()
    {
        return this.level;
    }

    @Override
    public void markChanged()
    {
        this.saveChanges();
    }

    @Override
    public void invalidCap()
    {
        this.invalidateCapabilities();
    }

    @Override
    public void updateBlockState(BlockState newState, int flags)
    {
        if (level != null)
        {
            level.setBlock(getBlockPos(), newState, flags);
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        machineComponents.onLoad(new MachineContext(this, level, worldPosition, getBlockState()));
    }

    @Override
    public void serverTick()
    {
        machineComponents.onServerTick(new MachineContext(this, level, worldPosition, getBlockState()));
    }

    @Override
    public void clientTick()
    {
        machineComponents.onClientTick(new MachineContext(this, level, worldPosition, getBlockState()));
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player)
    {
        super.importSettings(mode, input, player);
        machineComponents.importSettings(new MachineContext(this, level, worldPosition, getBlockState()), input, player);
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player)
    {
        super.exportSettings(mode, builder, player);
        machineComponents.exportSettings(new MachineContext(this, level, worldPosition, getBlockState()), builder, player);
    }

    @Override
    public void saveAdditional(ValueOutput data)
    {
        super.saveAdditional(data);
        machineComponents.writeNbt(data);
    }

    @Override
    public void loadTag(ValueInput data)
    {
        super.loadTag(data);
        machineComponents.readNbt(data);
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data)
    {
        super.writeToStream(data);
        machineComponents.writeStream(data);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data)
    {
        boolean result = super.readFromStream(data);
        result |= machineComponents.readStream(data);
        return result;
    }

    @Override
    public void clearContent()
    {
        super.clearContent();
        machineComponents.clearContent();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops)
    {
        super.addAdditionalDrops(level, pos, drops);
        machineComponents.addDrops(level, pos, drops);
    }
}
