package io.github.lounode.ae2cs.common.init;

import appeng.menu.implementations.MenuTypeBuilder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.*;
import io.github.lounode.ae2cs.common.me.logic.IntegratedInterfaceHost;
import io.github.lounode.ae2cs.common.menu.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AECSMenus
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, AECSConstants.MODID);


    public static final Supplier<MenuType<CrystalGrowthChamberMenu>> CRYSTAL_GROWTH_CHAMBER_MENU = MENU_TYPES.register("crystal_growth_chamber_menu",
            () -> MenuTypeBuilder.create(CrystalGrowthChamberMenu::new, CrystalGrowthChamberBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_growth_chamber_menu"))
    );

    public static final Supplier<MenuType<IntegratedInterfaceMenu>> INTEGRATED_INTERFACE_MENU = MENU_TYPES.register("integrated_interface_menu",
            () -> MenuTypeBuilder.create(IntegratedInterfaceMenu::new, IntegratedInterfaceHost.class)
                    .build(AE2CrystalScience.makeId("integrated_interface_menu"))
    );

    public static final Supplier<MenuType<IntegratedInterfaceSetStockAmountMenu>> INTEGRATED_INTERFACE_SET_STOCK_AMOUNT_MENU = MENU_TYPES.register("integrated_interface_set_stock_amount_menu",
            () -> MenuTypeBuilder.create(IntegratedInterfaceSetStockAmountMenu::new, IntegratedInterfaceHost.class)
                    .build(AE2CrystalScience.makeId("integrated_interface_set_stock_amount_menu"))
    );

    public static final Supplier<MenuType<CrystalVibrationChamberMenu>> CRYSTAL_VIBRATION_CHAMBER_MENU = MENU_TYPES.register("crystal_vibration_chamber_menu",
            () -> MenuTypeBuilder.create(CrystalVibrationChamberMenu::new, CrystalVibrationChamberBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_vibration_chamber_menu"))
    );

    public static final Supplier<MenuType<CircuitEtcherMenu>> CIRCUIT_ETCHER_MENU = MENU_TYPES.register("circuit_etcher_menu",
            () -> MenuTypeBuilder.create(CircuitEtcherMenu::new, CircuitEtcherBlockEntity.class)
                    .build(AE2CrystalScience.makeId("circuit_etcher_menu"))
    );

    public static final Supplier<MenuType<CrystalPulverizerMenu>> CRYSTAL_PULVERIZER_MENU = MENU_TYPES.register("crystal_pulverizer_menu",
            () -> MenuTypeBuilder.create(CrystalPulverizerMenu::new, CrystalPulverizerBlockEntity.class)
                    .build(AE2CrystalScience.makeId("crystal_pulverizer_menu"))
    );

    public static final Supplier<MenuType<QuartzGrindstoneMenu>> QUARTZ_GRINDSTONE_MENU = MENU_TYPES.register("quartz_grindstone_menu",
            () -> MenuTypeBuilder.create(QuartzGrindstoneMenu::new, QuartzGrindstoneBlockEntity.class)
                    .build(AE2CrystalScience.makeId("quartz_grindstone_menu"))
    );

    public static final Supplier<MenuType<MeteoriteCrafterMenu>> METEORITE_CRAFTER_MENU = MENU_TYPES.register("meteorite_crafter_menu",
            () -> MenuTypeBuilder.create(MeteoriteCrafterMenu::new, MeteoriteCrafterBlockEntity.class)
                    .build(AE2CrystalScience.makeId("meteorite_crafter_menu"))
    );

    public static void registerMenus(IEventBus eventBus)
    {
        MENU_TYPES.register(eventBus);
    }
}
