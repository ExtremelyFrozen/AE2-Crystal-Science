package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.ids.AECSConstants;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.jetbrains.annotations.NotNull;

public final class AECSEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, AECSConstants.MODID);

    private static final EnchantmentCategory TOOLS_CATEGORY = EnchantmentCategory.create("ae2cs_tools", item -> item.builtInRegistryHolder().is(Tags.Items.TOOLS));

    /**
     * 末影链接
     */
    public static final RegistryObject<Enchantment> ENDER_LINK = ENCHANTMENTS.register(
            "ender_link",
            () -> new Enchantment(Enchantment.Rarity.VERY_RARE, TOOLS_CATEGORY, new EquipmentSlot[] { EquipmentSlot.MAINHAND }) {

                @Override
                public int getMaxLevel() {
                    return 1;
                }

                @Override
                public int getMinCost(int level) {
                    return 1;
                }

                @Override
                public int getMaxCost(int level) {
                    return 1;
                }

                @Override
                public boolean isTradeable() {
                    return false;
                }

                @Override
                public boolean isDiscoverable() {
                    return false;
                }

                @Override
                public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
                    return false;
                }
            });

    private AECSEnchantments() {}

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
