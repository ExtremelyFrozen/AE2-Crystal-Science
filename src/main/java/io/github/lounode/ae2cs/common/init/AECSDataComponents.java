package io.github.lounode.ae2cs.common.init;

import io.github.lounode.ae2cs.api.networking.SideConfigField;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


/**
 * 原1.21.1放置数据组件的类，现用于存放其相关的NBT字段以及用于读写的辅助方法
 */
public class AECSDataComponents
{
    public static final String TAG_GROW_PROCESS = "grow_process";

    public static final String TAG_ENDER_EMITTER_POS = "ender_emitter_pos";
    public static final String TAG_ENDER_EMITTER_DIMENSION = "dimension";
    public static final String TAG_ENDER_EMITTER_POS_VALUE = "pos";

    public static final String TAG_ENCODED_RESONATING_PATTERN = "encoded_resonating_pattern";
    public static final String TAG_RESONATING_PATTERN_SELECTED_INPUT = "resonating_pattern_selected_input";

    public static final String TAG_RESONATING_CONVERTER_INV = "resonating_converter_inv";
    public static final String TAG_SIDE_CONFIG_FOR_MEMORY_CARD = "side_config_for_memory_card";

    public static void setEnderEmitterPos(ItemStack stack, GlobalPos pos)
    {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag posTag = new CompoundTag();
        posTag.putString(TAG_ENDER_EMITTER_DIMENSION, pos.dimension().location().toString());
        posTag.put(TAG_ENDER_EMITTER_POS_VALUE, NbtUtils.writeBlockPos(pos.pos()));
        root.put(TAG_ENDER_EMITTER_POS, posTag);
    }

    public static @Nullable GlobalPos getEnderEmitterPos(ItemStack stack)
    {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_ENDER_EMITTER_POS, 10))
        {
            return null;
        }

        CompoundTag posTag = root.getCompound(TAG_ENDER_EMITTER_POS);
        if (!posTag.contains(TAG_ENDER_EMITTER_DIMENSION, 8) || !posTag.contains(TAG_ENDER_EMITTER_POS_VALUE, 10))
        {
            return null;
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(posTag.getString(TAG_ENDER_EMITTER_DIMENSION));
        if (dimensionId == null)
        {
            return null;
        }

        BlockPos blockPos = NbtUtils.readBlockPos(posTag.getCompound(TAG_ENDER_EMITTER_POS_VALUE));
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        return GlobalPos.of(dimensionKey, blockPos);
    }

    public static void setEncodedResonatingPattern(ItemStack stack, EncodedResonatingPattern pattern)
    {
        stack.getOrCreateTag().put(TAG_ENCODED_RESONATING_PATTERN, EncodedResonatingPattern.writeToNBT(pattern));
    }

    public static @Nullable EncodedResonatingPattern getEncodedResonatingPattern(ItemStack stack)
    {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_ENCODED_RESONATING_PATTERN, 10))
        {
            return null;
        }
        return EncodedResonatingPattern.readFromNBT(root.getCompound(TAG_ENCODED_RESONATING_PATTERN));
    }

    public static void setResonatingPatternSelectedInput(ItemStack stack, int selected)
    {
        stack.getOrCreateTag().putInt(TAG_RESONATING_PATTERN_SELECTED_INPUT, selected);
    }

    public static int getResonatingPatternSelectedInput(ItemStack stack, int fallback)
    {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_RESONATING_PATTERN_SELECTED_INPUT, 3))
        {
            return fallback;
        }
        return root.getInt(TAG_RESONATING_PATTERN_SELECTED_INPUT);
    }

    public static void setSideConfigForMemoryCard(CompoundTag tag, SideConfigField value)
    {
        tag.put(TAG_SIDE_CONFIG_FOR_MEMORY_CARD, SideConfigField.writeToNBT(value));
    }

    public static @Nullable SideConfigField getSideConfigForMemoryCard(CompoundTag tag)
    {
        if (!tag.contains(TAG_SIDE_CONFIG_FOR_MEMORY_CARD, 10))
        {
            return null;
        }
        return SideConfigField.readFromNBT(tag.getCompound(TAG_SIDE_CONFIG_FOR_MEMORY_CARD));
    }
}
