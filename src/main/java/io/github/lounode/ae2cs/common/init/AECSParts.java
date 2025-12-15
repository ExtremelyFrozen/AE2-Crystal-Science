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
import io.github.lounode.ae2cs.common.me.part.IntegratedInterfacePart;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class AECSParts
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AECSConstants.MODID);
    private static final List<DeferredItem<? extends PartItem<?>>> ALL = new ArrayList<>();

    public static final DeferredItem<PartItem<IntegratedInterfacePart>> INTEGRATE_INTERFACE_PART = registerPart(
            AECSPartIds.INTEGRATED_INTERFACE,
            IntegratedInterfacePart.class,
            IntegratedInterfacePart::new
    );

    public static List<DeferredItem<? extends PartItem<?>>> getAll()
    {
        return Collections.unmodifiableList(ALL);
    }

    /**
     * 等价于 AE2 的 createPart
     */
    public static <T extends IPart> DeferredItem<PartItem<T>> registerPart(
            String idPath,
            Class<T> partClass,
            Function<IPartItem<T>, T> partFactory
    )
    {
        return registerPart(idPath, partClass, partFactory, AECSItems::defaultBuilder);
    }

    public static <T extends IPart> DeferredItem<PartItem<T>> registerPart(
            String idPath,
            Class<T> partClass,
            Function<IPartItem<T>, T> partFactory,
            Supplier<Item.Properties> props
    )
    {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));

        DeferredItem<PartItem<T>> obj = ITEMS.register(idPath,
                () -> new PartItem<>(props.get(), partClass, partFactory));
        ALL.add(obj);
        return obj;
    }

    /**
     * 等价于 AE2 的 createCustomPartItem
     */
    public static <T extends IPart> DeferredItem<PartItem<T>> registerCustomPartItem(
            String idPath,
            Class<T> partClass,
            Function<Item.Properties, PartItem<T>> itemFactory
    )
    {
        return registerCustomPartItem(idPath, partClass, itemFactory, AECSItems::defaultBuilder);
    }

    public static <T extends IPart> DeferredItem<PartItem<T>> registerCustomPartItem(
            String idPath,
            Class<T> partClass,
            Function<Item.Properties, PartItem<T>> itemFactory,
            Supplier<Item.Properties> props
    )
    {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));

        DeferredItem<PartItem<T>> obj = ITEMS.register(idPath, () -> itemFactory.apply(props.get()));
        ALL.add(obj);
        return obj;
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

        EnumMap<AEColor, DeferredItem<ColoredPartItem<T>>> map = new EnumMap<>(AEColor.class);

        for (AEColor color : AEColor.values())
        {
            String idPath = color.registryPrefix + "_" + idSuffix;

            DeferredItem<ColoredPartItem<T>> obj = ITEMS.register(idPath,
                    () -> new ColoredPartItem<>(props.get(), partClass, partFactory, color));

            map.put(color, obj);
            ALL.add(obj);
        }

        return new ColoredPartSet<>(map);
    }

    /**
     * 颜色套件：按颜色取对应 DeferredItem
     */
    public static final class ColoredPartSet<T extends IPart>
    {
        private final EnumMap<AEColor, DeferredItem<ColoredPartItem<T>>> byColor;

        private ColoredPartSet(EnumMap<AEColor, DeferredItem<ColoredPartItem<T>>> byColor)
        {
            this.byColor = byColor;
        }

        public DeferredItem<ColoredPartItem<T>> get(AEColor color)
        {
            return byColor.get(color);
        }

        public Collection<DeferredItem<ColoredPartItem<T>>> values()
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
