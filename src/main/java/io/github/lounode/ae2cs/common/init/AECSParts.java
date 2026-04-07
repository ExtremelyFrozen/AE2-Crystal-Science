package io.github.lounode.ae2cs.common.init;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.api.util.AEColor;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.ids.AECSPartIds;
import io.github.lounode.ae2cs.api.util.RegistryItem;
import io.github.lounode.ae2cs.common.item.ResonatingPatternProviderPartItem;
import io.github.lounode.ae2cs.common.me.part.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class AECSParts
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AECSConstants.MODID);
    private static final List<RegistryItem<? extends PartItem<?>>> ALL = new ArrayList<>();

    public static final RegistryItem<PartItem<EnderInterfacePart>> ENDER_INTERFACE_PART = registerPart(
            AECSPartIds.ENDER_INTERFACE,
            EnderInterfacePart.class,
            EnderInterfacePart::new
    );

    public static final RegistryItem<PartItem<EnderInterfacePart>> EX_ENDER_INTERFACE_PART = registerPart(
            AECSPartIds.EX_ENDER_INTERFACE,
            EnderInterfacePart.class,
            EnderInterfacePart::new
    );

    public static final RegistryItem<PartItem<IntegratedInterfacePart>> INTEGRATE_INTERFACE_PART = registerPart(
            AECSPartIds.INTEGRATED_INTERFACE,
            IntegratedInterfacePart.class,
            IntegratedInterfacePart::new
    );

    public static final RegistryItem<PartItem<IntegratedInterfacePart>> EX_INTEGRATE_INTERFACE_PART = registerPart(
            AECSPartIds.EX_INTEGRATED_INTERFACE,
            IntegratedInterfacePart.class,
            IntegratedInterfacePart::new
    );

    public static final RegistryItem<PartItem<ResonatingPatternProviderPart>> RESONATING_PATTERN_PROVIDER_PART = registerCustomPartItem(
            AECSPartIds.RESONATING_PATTERN_PROVIDER,
            ResonatingPatternProviderPart.class,
            ResonatingPatternProviderPartItem::new
    );

    public static final RegistryItem<PartItem<ResonatingPatternProviderPart>> EX_RESONATING_PATTERN_PROVIDER_PART = registerCustomPartItem(
            AECSPartIds.EX_RESONATING_PATTERN_PROVIDER,
            ResonatingPatternProviderPart.class,
            ResonatingPatternProviderPartItem::new
    );

    public static final RegistryItem<PartItem<SimplePatternProviderPart>> SIMPLE_PATTERN_PROVIDER_PART = registerPart(
            AECSPartIds.SIMPLE_PATTERN_PROVIDER,
            SimplePatternProviderPart.class,
            SimplePatternProviderPart::new
    );

    public static final RegistryItem<PartItem<MeteoritePatternProviderPart>> METEORITE_PATTERN_PROVIDER_PART = registerPart(
            AECSPartIds.METEORITE_PATTERN_PROVIDER,
            MeteoritePatternProviderPart.class,
            MeteoritePatternProviderPart::new
    );

    public static final RegistryItem<PartItem<QuartzOscillatorClockPart>> QUARTZ_OSCILLATOR_CLOCK_PART = registerPart(
            AECSPartIds.QUARTZ_OSCILLATOR_CLOCK,
            QuartzOscillatorClockPart.class,
            QuartzOscillatorClockPart::new
    );

    public static List<RegistryItem<? extends PartItem<?>>> getAll()
    {
        return Collections.unmodifiableList(ALL);
    }

    /**
     * 等价于 AE2 的 createPart
     */
    public static <T extends IPart> RegistryItem<PartItem<T>> registerPart(
            String idPath,
            Class<T> partClass,
            Function<IPartItem<T>, T> partFactory
    )
    {
        return registerPart(idPath, partClass, partFactory, AECSItems::defaultBuilder);
    }

    public static <T extends IPart> RegistryItem<PartItem<T>> registerPart(
            String idPath,
            Class<T> partClass,
            Function<IPartItem<T>, T> partFactory,
            Supplier<Item.Properties> props
    )
    {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));

        RegistryObject<PartItem<T>> obj = ITEMS.register(idPath,
                () -> new PartItem<>(props.get(), partClass, partFactory));
        RegistryItem<PartItem<T>> registryItem = new RegistryItem<>(obj);
        ALL.add(registryItem);
        return registryItem;
    }

    /**
     * 等价于 AE2 的 createCustomPartItem
     */
    public static <T extends IPart> RegistryItem<PartItem<T>> registerCustomPartItem(
            String idPath,
            Class<T> partClass,
            Function<Item.Properties, PartItem<T>> itemFactory
    )
    {
        return registerCustomPartItem(idPath, partClass, itemFactory, AECSItems::defaultBuilder);
    }

    public static <T extends IPart> RegistryItem<PartItem<T>> registerCustomPartItem(
            String idPath,
            Class<T> partClass,
            Function<Item.Properties, PartItem<T>> itemFactory,
            Supplier<Item.Properties> props
    )
    {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));

        RegistryObject<PartItem<T>> obj = ITEMS.register(idPath, () -> itemFactory.apply(props.get()));
        RegistryItem<PartItem<T>> registryItem = new RegistryItem<>(obj);
        ALL.add(registryItem);
        return registryItem;
    }

    /**
     * 等价于 AE2 的 constructColoredDefinition 批量注册 17 色 Part
     */
    public static <T extends IPart> ColoredPartSet<T> registerColoredPart(
            String idSuffix, // 例如 "smart_cable" -> "white_smart_cable" ...
            Class<T> partClass,
            Function<ColoredPartItem<T>, T> partFactory
    )
    {
        return registerColoredPart(idSuffix, partClass, partFactory, AECSItems::defaultBuilder);
    }

    public static <T extends IPart> ColoredPartSet<T> registerColoredPart(
            String idSuffix,
            Class<T> partClass,
            Function<ColoredPartItem<T>, T> partFactory,
            Supplier<Item.Properties> props
    )
    {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));

        EnumMap<AEColor, RegistryItem<ColoredPartItem<T>>> map = new EnumMap<>(AEColor.class);

        for (AEColor color : AEColor.values())
        {
            String idPath = color.registryPrefix + "_" + idSuffix;

            RegistryObject<ColoredPartItem<T>> obj = ITEMS.register(idPath,
                    () -> new ColoredPartItem<>(props.get(), partClass, partFactory, color));

            RegistryItem<ColoredPartItem<T>> registryItem = new RegistryItem<>(obj);

            map.put(color, registryItem);
            ALL.add(registryItem);
        }

        return new ColoredPartSet<>(map);
    }

    /**
     * 颜色套件：按颜色取对应 DeferredItem
     */
    public static final class ColoredPartSet<T extends IPart>
    {
        private final EnumMap<AEColor, RegistryItem<ColoredPartItem<T>>> byColor;

        private ColoredPartSet(EnumMap<AEColor, RegistryItem<ColoredPartItem<T>>> byColor)
        {
            this.byColor = byColor;
        }

        public RegistryItem<ColoredPartItem<T>> get(AEColor color)
        {
            return byColor.get(color);
        }

        public Collection<RegistryItem<ColoredPartItem<T>>> values()
        {
            return Collections.unmodifiableCollection(byColor.values());
        }
    }

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }

    private AECSParts()
    {
    }
}
