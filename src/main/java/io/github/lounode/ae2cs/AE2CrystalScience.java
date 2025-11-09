package io.github.lounode.ae2cs;

import com.mojang.logging.LogUtils;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.init.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(AECSConstants.MODID)
public class AE2CrystalScience
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2CrystalScience(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        AECSItems.register(modEventBus);
        AECSBlocks.register(modEventBus);
        AECSBlockEntities.register(modEventBus);
        AECSDataComponents.register(modEventBus);
        AECSCreativeModeTabs.register(modEventBus);
    }

    public static ResourceLocation makeId(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(AECSConstants.MODID, path);
    }
}
