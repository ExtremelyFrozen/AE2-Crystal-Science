//package io.github.lounode.ae2cs.common.me.part;
//
//import appeng.api.AECapabilities;
//import appeng.api.parts.IPartItem;
//import appeng.api.parts.RegisterPartCapabilitiesEvent;
//import appeng.api.stacks.AEItemKey;
//import appeng.core.AppEng;
//import appeng.helpers.patternprovider.PatternProviderLogic;
//import appeng.menu.ISubMenu;
//import appeng.menu.MenuOpener;
//import appeng.menu.locator.MenuHostLocator;
//import appeng.parts.crafting.PatternProviderPart;
//import io.github.lounode.ae2cs.AE2CrystalScience;
//import io.github.lounode.ae2cs.common.init.AECSMenus;
//import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderHost;
//import io.github.lounode.ae2cs.common.me.logic.ResonatingPatternProviderLogic;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//
//public class ResonatingPatternProviderPart extends PatternProviderPart implements ResonatingPatternProviderHost
//{
//    public static final Identifier MODEL_BASE = AE2CrystalScience.makeId(
//            "part/resonating_pattern_provider/base");
//
//    @PartModels
//    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
//            AppEng.makeId("part/interface_off"));
//
//    @PartModels
//    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
//            AppEng.makeId("part/interface_on"));
//
//    @PartModels
//    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
//            AppEng.makeId("part/interface_has_channel"));
//
//    public static final Identifier MODEL_EXTENDED = AE2CrystalScience.makeId(
//            "part/resonating_pattern_provider/extended");
//
//    @PartModels
//    public static final PartModel EXTENDED_MODELS_OFF = new PartModel(MODEL_EXTENDED,
//            AppEng.makeId("part/interface_off"));
//
//    @PartModels
//    public static final PartModel EXTENDED_MODELS_ON = new PartModel(MODEL_EXTENDED,
//            AppEng.makeId("part/interface_on"));
//
//    @PartModels
//    public static final PartModel EXTENDED_MODELS_HAS_CHANNEL = new PartModel(MODEL_EXTENDED,
//            AppEng.makeId("part/interface_has_channel"));
//
//    public ResonatingPatternProviderPart(IPartItem<?> partItem)
//    {
//        super(partItem);
//    }
//
//    /**
//     * 注册能力
//     */
//    public static void onRegisterCaps(RegisterPartCapabilitiesEvent event)
//    {
//        event.register(
//                AECapabilities.GENERIC_INTERNAL_INV,
//                (part, direction) -> part.getLogic().getReturnInv(),
//                ResonatingPatternProviderPart.class
//        );
//    }
//
//    @Override
//    public IPartModel getStaticModels()
//    {
//        if (this.isActive() && this.isPowered())
//        {
//            return isExtended() ? EXTENDED_MODELS_HAS_CHANNEL : MODELS_HAS_CHANNEL;
//        }
//        else if (this.isPowered())
//        {
//            return isExtended() ? EXTENDED_MODELS_ON : MODELS_ON;
//        }
//        else
//        {
//            return isExtended() ? EXTENDED_MODELS_OFF : MODELS_OFF;
//        }
//    }
//
//    @Override
//    public boolean isExtended()
//    {
//        return getPartItem() == AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART.get();
//    }
//
//    @Override
//    protected PatternProviderLogic createLogic()
//    {
//        int patternSize = isExtended() ? 36 : 9;
//        return new ResonatingPatternProviderLogic(getMainNode(), this, patternSize);
//    }
//
//    @Override
//    public void openMenu(Player player, MenuHostLocator locator)
//    {
//        MenuOpener.open(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, locator);
//    }
//
//    @Override
//    public void returnToMainMenu(Player player, ISubMenu subMenu)
//    {
//        MenuOpener.returnTo(AECSMenus.RESONATING_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
//    }
//
//    @Override
//    public AEItemKey getTerminalIcon()
//    {
//        return AEItemKey.of(getPartItem());
//    }
//
//    @Override
//    public ItemStack getMainMenuIcon()
//    {
//        return new ItemStack(getPartItem());
//    }
//
//    @Override
//    public ResonatingPatternProviderLogic getResonatingLogic()
//    {
//        return (ResonatingPatternProviderLogic) getLogic();
//    }
//}
