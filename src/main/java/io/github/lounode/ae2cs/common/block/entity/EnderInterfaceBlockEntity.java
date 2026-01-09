package io.github.lounode.ae2cs.common.block.entity;

import appeng.api.AECapabilities;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class EnderInterfaceBlockEntity extends InterfaceBlockEntity implements EnderInterfaceHost
{
    public EnderInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected InterfaceLogic createLogic()
    {
        return new EnderInterfaceLogic(getMainNode(), this, getItemFromBlockEntity().asItem());
    }

    /**
     * 注册AE节点和能量能力
     */
    public static void onRegisterCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                AECSBlockEntities.ENDER_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.getInterfaceLogic().getStorage()
        );
        event.registerBlockEntity(
                AECapabilities.ME_STORAGE,
                AECSBlockEntities.ENDER_INTERFACE_BLOCK_ENTITY.get(),
                (be, direction) -> be.getInterfaceLogic().getInventory()
        );
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator)
    {
        MenuOpener.open(AECSMenus.ENDER_INTERFACE_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.ENDER_INTERFACE_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return AECSBlocks.ENDER_INTERFACE_BLOCK.toStack();
    }

    @Override
    public EnderInterfaceLogic getEnderInterfaceLogic()
    {
        return (EnderInterfaceLogic) getInterfaceLogic();
    }

    @Override
    public void markForLogicClientUpdate()
    {
        if (level != null && !level.isClientSide())
            this.markForClientUpdate();
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data)
    {
        super.writeToStream(data);
        data.writeBoolean(this.getEnderInterfaceLogic().isRenderRangeInClient());
        data.writeInt(getEnderInterfaceLogic().getRange());
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data)
    {
        super.readFromStream(data);
        this.getEnderInterfaceLogic().setRenderRangeInClient(data.readBoolean());
        this.getEnderInterfaceLogic().setRange(data.readInt());
        return true;
    }
}
