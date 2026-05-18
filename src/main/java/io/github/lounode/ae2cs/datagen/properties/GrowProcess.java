package io.github.lounode.ae2cs.datagen.properties;

import com.mojang.serialization.MapCodec;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GrowProcess implements RangeSelectItemModelProperty {
    public static final MapCodec<GrowProcess> MAP_CODEC = MapCodec.unit(new GrowProcess());

    @Override
    public float get(@NonNull ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        if (itemStack.getItem() instanceof CrystalSeedItem seedItem) {
            return seedItem.getGrowProcess(itemStack);
        }
        return 0.0F;
    }

    @Override
    public @NonNull MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
