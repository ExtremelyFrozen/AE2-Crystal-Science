package io.github.lounode.ae2cs.common.me.part;

import appeng.api.AECapabilities;
import appeng.api.parts.IPartItem;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.parts.crafting.PatternProviderPart;
import io.github.lounode.ae2cs.common.init.AECSMenus;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SimplePatternProviderPart extends PatternProviderPart
{
    public SimplePatternProviderPart(IPartItem<?> partItem)
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
                (part, direction) -> part.getLogic().getReturnInv(),
                SimplePatternProviderPart.class
        );
    }

    @Override
    protected PatternProviderLogic createLogic()
    {
        return new PatternProviderLogic(getMainNode(), this, 5);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator)
    {
        MenuOpener.open(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu)
    {
        MenuOpener.returnTo(AECSMenus.SIMPLE_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon()
    {
        return AEItemKey.of(AECSParts.SIMPLE_PATTERN_PROVIDER_PART);
    }

    @Override
    public ItemStack getMainMenuIcon()
    {
        return AECSParts.SIMPLE_PATTERN_PROVIDER_PART.toStack();
    }
}
