package io.github.lounode.ae2cs.common.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

final class GrowthFactoryProducts {

    private GrowthFactoryProducts() {}

    static ItemStack get(Block motherRock, int target) {
        if (target <= ResonatingGrowthFactoryBlockEntity.CLUSTER_TARGET) {
            return CrystalGrowthChamberBlockEntity.growthProduct(motherRock, target);
        }
        ItemStack cluster = CrystalGrowthChamberBlockEntity.growthProduct(motherRock,
                ResonatingGrowthFactoryBlockEntity.CLUSTER_TARGET);
        if (cluster.isEmpty()) return ItemStack.EMPTY;
        String clusterId = BuiltInRegistries.ITEM.getKey(cluster.getItem()).toString();
        String result = target == ResonatingGrowthFactoryBlockEntity.DUST_TARGET ? dust(clusterId) : crystal(clusterId);
        return result == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(result))
                .map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    private static String dust(String cluster) {
        return switch (cluster) {
            case "ae2:quartz_cluster", "ae2cs:charged_certus_quartz_crystal_cluster" -> "ae2:certus_quartz_dust";
            case "ae2cs:fluix_crystal_cluster" -> "ae2:fluix_dust";
            case "ae2cs:ender_quartz_crystal_cluster" -> "ae2:ender_dust";
            case "ae2cs:nether_quartz_crystal_cluster", "create_crystal_industry:quartz_cluster" -> "ae2cs:nether_quartz_dust";
            case "ae2cs:meteor_crystal_cluster" -> "ae2:sky_dust";
            case "ae2cs:link_crystal_cluster" -> "ae2cs:link_crystal_dust";
            case "ae2cs:quantum_crystal_cluster" -> "ae2cs:quantum_crystal_dust";
            case "ae2cs:resonating_crystal_cluster" -> "ae2cs:resonating_crystal_dust";
            case "ae2cs:redstone_crystal_cluster" -> "ae2cs:redstone_crystal_dust";
            case "ae2cs:energized_fluix_crystal_cluster" -> "neoecoae:energized_fluix_crystal_dust";
            case "extendedae:entro_cluster" -> "extendedae:entro_dust";
            case "extendedae_plus:lattra_crystal_cluster" -> "extendedae_plus:lattra_dust";
            case "ae2lt:overload_crystal_cluster" -> "ae2lt:overload_crystal_dust";
            case "appgen:ember_cluster" -> "appgen:ember_dust";
            case "data_energistics:data_crystal_cluster" -> "data_energistics:data_dust";
            case "neoecoae:energized_crystal_cluster" -> "neoecoae:energized_crystal_dust";
            case "create_crystal_industry:diamond_cluster" -> "appflux:diamond_dust";
            case "create_crystal_industry:emerald_cluster" -> "appflux:emerald_dust";
            case "create_crystal_industry:glowstone_cluster" -> "minecraft:glowstone_dust";
            case "create_crystal_industry:redstone_cluster" -> "minecraft:redstone";
            default -> null;
        };
    }

    private static String crystal(String cluster) {
        return switch (cluster) {
            case "minecraft:amethyst_cluster" -> "minecraft:amethyst_shard";
            case "ae2:quartz_cluster" -> "ae2:certus_quartz_crystal";
            case "ae2cs:charged_certus_quartz_crystal_cluster" -> "ae2:charged_certus_quartz_crystal";
            case "ae2cs:fluix_crystal_cluster" -> "ae2:fluix_crystal";
            case "ae2cs:ender_quartz_crystal_cluster" -> "ae2cs:ender_quartz";
            case "ae2cs:nether_quartz_crystal_cluster", "create_crystal_industry:quartz_cluster" -> "minecraft:quartz";
            case "create_crystal_industry:rose_quartz_cluster" -> "create:rose_quartz";
            case "ae2cs:meteor_crystal_cluster" -> "ae2cs:meteor_crystal";
            case "ae2cs:link_crystal_cluster" -> "ae2cs:link_crystal";
            case "ae2cs:quantum_crystal_cluster" -> "ae2cs:quantum_crystal";
            case "ae2cs:resonating_crystal_cluster" -> "ae2cs:resonating_crystal";
            case "ae2cs:redstone_crystal_cluster" -> "appflux:redstone_crystal";
            case "ae2cs:energized_fluix_crystal_cluster" -> "neoecoae:energized_fluix_crystal";
            case "extendedae:entro_cluster" -> "extendedae:entro_crystal";
            case "extendedae_plus:lattra_crystal_cluster" -> "extendedae_plus:lattra_crystal";
            case "ae2lt:overload_crystal_cluster" -> "ae2lt:overload_crystal";
            case "appgen:ember_cluster" -> "appgen:ember_crystal";
            case "data_energistics:data_crystal_cluster" -> "data_energistics:data_crystal";
            case "neoecoae:energized_crystal_cluster" -> "neoecoae:energized_crystal";
            case "create_crystal_industry:ancient_debris_cluster" -> "minecraft:netherite_scrap";
            case "create_crystal_industry:arclight_cluster" -> "create_crystal_industry:arclight";
            case "create_crystal_industry:diamond_cluster" -> "minecraft:diamond";
            case "create_crystal_industry:echo_cluster" -> "minecraft:echo_shard";
            case "create_crystal_industry:emerald_cluster" -> "minecraft:emerald";
            case "create_crystal_industry:flammable_ice_cluster" -> "create_crystal_industry:flammable_ice";
            case "create_crystal_industry:glowstone_cluster" -> "minecraft:glowstone_dust";
            case "create_crystal_industry:lapis_cluster" -> "minecraft:lapis_lazuli";
            case "create_crystal_industry:raw_copper_cluster" -> "minecraft:raw_copper";
            case "create_crystal_industry:raw_gold_cluster" -> "minecraft:raw_gold";
            case "create_crystal_industry:raw_iron_cluster" -> "minecraft:raw_iron";
            case "create_crystal_industry:raw_zinc_cluster" -> "create:raw_zinc";
            case "create_crystal_industry:redstone_cluster" -> "minecraft:redstone";
            default -> null;
        };
    }
}
