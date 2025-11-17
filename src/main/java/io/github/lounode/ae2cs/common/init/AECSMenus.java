package io.github.lounode.ae2cs.common.init;

import appeng.menu.implementations.MenuTypeBuilder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.CrystalGrowthChamberBlockEntity;
import io.github.lounode.ae2cs.common.menu.CrystalGrowthChamberMenu;
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

    public static void registerMenus(IEventBus eventBus)
    {
        MENU_TYPES.register(eventBus);
    }
}
