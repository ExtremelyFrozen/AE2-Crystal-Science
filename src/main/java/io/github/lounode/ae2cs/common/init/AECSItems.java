package io.github.lounode.ae2cs.common.init;

import appeng.api.upgrades.Upgrades;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.ids.AECSItemIds;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import io.github.lounode.ae2cs.common.item.EnderLinkerItem;
import io.github.lounode.ae2cs.common.item.PureCrystalItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class AECSItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AECSConstants.MODID);

    /**
     * 全部注册物品
     */
    private static final List<DeferredItem<? extends Item>> ALL = new ArrayList<>();

    /**
     * 水晶种子
     */
    private static final List<DeferredItem<CrystalSeedItem>> CRYSTAL_SEEDS = new ArrayList<>();

    /**
     * 高纯水晶
     */
    private static final List<DeferredItem<PureCrystalItem>> PURE_CRYSTAL = new ArrayList<>();

    /**
     * 杂项物品
     */
    private static final List<DeferredItem<? extends Item>> OTHERS = new ArrayList<>();

    public static final DeferredItem<PureCrystalItem> pureCertusQuartzCrystal = registerPureCrystalItem(AECSItemIds.PURE_CERTUS_QUARTZ_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureFluixCrystal = registerPureCrystalItem(AECSItemIds.PURE_FLUIX_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureNetherQuartzCrystal = registerPureCrystalItem(AECSItemIds.PURE_NETHER_QUARTZ_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureEntroCrystal = registerPureCrystalItem(AECSItemIds.PURE_ENTRO_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureRedstoneCrystal = registerPureCrystalItem(AECSItemIds.PURE_REDSTONE_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureQuantumCrystal = registerPureCrystalItem(AECSItemIds.PURE_QUANTUM_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureRoseQuartz = registerPureCrystalItem(AECSItemIds.PURE_ROSE_QUARTZ, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureEnderQuartz = registerPureCrystalItem(AECSItemIds.PURE_ENDER_QUARTZ, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureResonatingCrystal = registerPureCrystalItem(AECSItemIds.PURE_RESONATING_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));
    public static final DeferredItem<PureCrystalItem> pureMeteorCrystal = registerPureCrystalItem(AECSItemIds.PURE_METEOR_CRYSTAL, () -> new PureCrystalItem(defaultBuilder()));

    public static final DeferredItem<CrystalSeedItem> certusQuartzSeed = registerCrystalSeedItem(AECSItemIds.CERTUS_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureCertusQuartzCrystal));
    public static final DeferredItem<CrystalSeedItem> fluixCrystalSeed = registerCrystalSeedItem(AECSItemIds.FLUIX_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureFluixCrystal));
    public static final DeferredItem<CrystalSeedItem> netherQuartzSeed = registerCrystalSeedItem(AECSItemIds.NETHER_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureNetherQuartzCrystal));
    public static final DeferredItem<CrystalSeedItem> entroCrystalSeed = registerCrystalSeedItem(AECSItemIds.ENTRO_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureEntroCrystal));
    public static final DeferredItem<CrystalSeedItem> redstoneCrystalSeed = registerCrystalSeedItem(AECSItemIds.REDSTONE_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureRedstoneCrystal));
    public static final DeferredItem<CrystalSeedItem> quantumCrystalSeed = registerCrystalSeedItem(AECSItemIds.QUANTUM_CRYSTAL_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureQuantumCrystal));
    public static final DeferredItem<CrystalSeedItem> roseQuartzSeed = registerCrystalSeedItem(AECSItemIds.ROSE_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureRoseQuartz));
    public static final DeferredItem<CrystalSeedItem> enderQuartzSeed = registerCrystalSeedItem(AECSItemIds.ENDER_QUARTZ_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureEnderQuartz));
    public static final DeferredItem<CrystalSeedItem> resonatingSeed = registerCrystalSeedItem(AECSItemIds.RESONATING_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureResonatingCrystal));
    public static final DeferredItem<CrystalSeedItem> meteorSeed = registerCrystalSeedItem(AECSItemIds.METEOR_SEED, () -> new CrystalSeedItem(defaultBuilder(), pureMeteorCrystal));

    public static final DeferredItem<Item> crystalGrowthCard = registerOtherItem(AECSItemIds.CRYSTAL_GROWTH_CARD, () -> Upgrades.createUpgradeCardItem(new Item.Properties()));

    public static final DeferredItem<EnderLinkerItem> enderLink = registerOtherItem(AECSItemIds.ENDER_LINKER, () -> new EnderLinkerItem(new Item.Properties()));

    // 快速物品状态
    public static Item.Properties defaultBuilder()
    {
        return new Item.Properties();
    }

    // getter
    public static List<DeferredItem<? extends Item>> getALL()
    {
        return Collections.unmodifiableList(ALL);
    }

    public static List<DeferredItem<CrystalSeedItem>> getCrystalSeeds()
    {
        return Collections.unmodifiableList(CRYSTAL_SEEDS);
    }

    public static List<DeferredItem<PureCrystalItem>> getPureCrystal()
    {
        return Collections.unmodifiableList(PURE_CRYSTAL);
    }

    public static List<DeferredItem<? extends Item>> getOthers()
    {
        return Collections.unmodifiableList(OTHERS);
    }

    // 工具方法
    private static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> supplier)
    {
        DeferredItem<T> obj = ITEMS.register(name, () -> {
            T t = supplier.get();
            return t instanceof Item ? t : (T) new Item(new Item.Properties());
        });
        ALL.add(obj);
        return obj;
    }

    private static DeferredItem<CrystalSeedItem> registerCrystalSeedItem(String name, Supplier<CrystalSeedItem> sup)
    {
        DeferredItem<CrystalSeedItem> obj = registerItem(name, sup);
        CRYSTAL_SEEDS.add(obj);
        return obj;
    }

    private static DeferredItem<PureCrystalItem> registerPureCrystalItem(String name, Supplier<PureCrystalItem> sup)
    {
        DeferredItem<PureCrystalItem> obj = registerItem(name, sup);
        PURE_CRYSTAL.add(obj);
        return obj;
    }

    private static <T extends Item> DeferredItem<T> registerOtherItem(String name, Supplier<T> sup)
    {
        DeferredItem<T> obj = registerItem(name, sup);
        OTHERS.add(obj);
        return obj;
    }

    // 注册监听
    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
