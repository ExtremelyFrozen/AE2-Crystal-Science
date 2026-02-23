package io.github.lounode.ae2cs.common.block.entity;

import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import io.github.lounode.ae2cs.common.init.AECSBlockEntities;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class EnderInterfaceBlockEntity extends InterfaceBlockEntity implements EnderInterfaceHost
{

    public EnderInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public boolean isExtended()
    {
        return this.getType() == AECSBlockEntities.EX_ENDER_INTERFACE_BLOCK_ENTITY.get();
    }

    @Override
    protected InterfaceLogic createLogic()
    {
        int slotSize = 9;
        int absorbConfigSlots = isExtended() ? 36 : 18;
        return new EnderInterfaceLogic(getMainNode(), this, getItemFromBlockEntity().asItem(), slotSize, absorbConfigSlots);
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return this.getCustomBoundingBox(this.getBlockPos());
    }

    @Override
    public void openMenu(Player player, MenuLocator locator)
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
        return new ItemStack(getItemFromBlockEntity());
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

    /**
     * 来自高版本移植
     */
    public void markForClientUpdate()
    {
        this.requestModelDataUpdate();

        if (this.level != null && !this.isRemoved() && !notLoaded())
        {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data)
    {
        super.writeToStream(data);
        data.writeBoolean(this.getEnderInterfaceLogic().isRenderRangeInClient());
        data.writeInt(getEnderInterfaceLogic().getRange());
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data)
    {
        super.readFromStream(data);
        this.getEnderInterfaceLogic().setRenderRangeInClient(data.readBoolean());
        this.getEnderInterfaceLogic().setRange(data.readInt());
        return true;
    }
}
