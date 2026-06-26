package io.github.lounode.ae2cs.api.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RegistryItem<T extends Item> implements Supplier<T>, ItemLike {

    private final RegistryObject<T> item;

    public RegistryItem(RegistryObject<T> item) {
        this.item = item;
    }

    @Override
    public T get() {
        return this.item.get();
    }

    @Override
    public @NotNull T asItem() {
        return this.item.get();
    }

    public ItemStack toStack(int count) {
        ItemStack stack = this.asItem().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }

    public ItemStack toStack() {
        return this.toStack(1);
    }
}
