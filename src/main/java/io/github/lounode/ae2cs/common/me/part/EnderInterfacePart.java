package io.github.lounode.ae2cs.common.me.part;

import appeng.api.AECapabilities;
import appeng.api.parts.IPartItem;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.helpers.InterfaceLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.parts.misc.InterfacePart;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceHost;
import io.github.lounode.ae2cs.common.me.logic.EnderInterfaceLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EnderInterfacePart extends InterfacePart implements EnderInterfaceHost
{
    public EnderInterfacePart(IPartItem<?> partItem)
    {
        super(partItem);
    }

    /**
     * 注册能力
     */
    public static void onRegisterCaps(RegisterPartCapabilitiesEvent event)
    {
        event.register(
                AECapabilities.GENERIC_INTERNAL_INV,
                (part, direction) -> part.getInterfaceLogic().getStorage(),
                EnderInterfacePart.class
        );
        event.register(
                AECapabilities.ME_STORAGE,
                (part, direction) -> part.getInterfaceLogic().getInventory(),
                EnderInterfacePart.class
        );
    }

    @Override
    public boolean isExtended()
    {
        return this.getPartItem() == AECSParts.EX_ENDER_INTERFACE_PART.get();
    }

    @Override
    protected InterfaceLogic createLogic()
    {
        int slotSize = 9;
        int absorbConfigSlots = isExtended() ? 36 : 18;
        return new EnderInterfaceLogic(getMainNode(), this, getPartItem().asItem(), slotSize, absorbConfigSlots);
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
        return new ItemStack(getPartItem());
    }

    @Override
    public EnderInterfaceLogic getEnderInterfaceLogic()
    {
        return (EnderInterfaceLogic) getInterfaceLogic();
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data)
    {
        super.writeToStream(data);
        data.writeBoolean(this.getEnderInterfaceLogic().isRenderRangeInClient());
        data.writeInt(this.getEnderInterfaceLogic().getRange());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data)
    {
        super.readFromStream(data);
        this.getEnderInterfaceLogic().setRenderRangeInClient(data.readBoolean());
        this.getEnderInterfaceLogic().setRange(data.readInt());
        return true;
    }

    @Override
    public void markForLogicClientUpdate()
    {
        if (this.getBlockEntity().getLevel() != null && !this.getBlockEntity().getLevel().isClientSide())
        {
            this.getHost().markForUpdate();
        }
    }
}
