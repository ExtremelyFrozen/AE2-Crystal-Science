package io.github.lounode.ae2cs.datagen;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class AECSFluidTagsProvider extends FluidTagsProvider
{
    public AECSFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider)
    {
        super(output, provider, AECSConstants.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider)
    {
    }

    @Override
    public @NotNull String getName()
    {
        return "AECS Fluid Tags";
    }
}
