package io.github.lounode.ae2cs;

import com.mojang.logging.LogUtils;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.util.PatternAccessTermQuickMoveHelper;
import io.github.lounode.ae2cs.common.init.*;
import io.github.lounode.ae2cs.common.me.AEPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(AECSConstants.MODID)
public class AE2CrystalScience
{
    public static final Logger LOGGER = LogUtils.getLogger();

    public AE2CrystalScience()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        Config.register(ModLoadingContext.get(), modEventBus);

        AECSItems.register(modEventBus);
        AECSParts.register(modEventBus);
        AECSBlocks.register(modEventBus);
        AECSBlockEntities.register(modEventBus);
        //AECSDataComponents.register(modEventBus);
        AECSCreativeModeTabs.register(modEventBus);
        AECSMenus.registerMenus(modEventBus);
        AECSRecipeTypes.register(modEventBus);
        AECSRecipeSerializers.register(modEventBus);
        AECSEnchantments.register(modEventBus);
        AECSPackets.register();

        AEPlugin.onInit();
        AEPlugin.onRegister(modEventBus, MinecraftForge.EVENT_BUS);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            AE2CrystalScienceClient.clientInit();
            AE2CrystalScienceClient.clientRegister(modEventBus, MinecraftForge.EVENT_BUS);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(
                () -> {
                    AEPlugin.onCommonSetup();
                    PatternAccessTermQuickMoveHelper.init();

                    if (FMLEnvironment.dist == Dist.CLIENT)
                        AE2CrystalScienceClient.clientCommonSetup();
                }
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("AE2CrystalScience - Server started");
    }

    public static ResourceLocation makeId(String path)
    {
        return new ResourceLocation(AECSConstants.MODID, path);
    }

    public static ResourceLocation parseOrMakeId(String path)
    {
        if (path.indexOf(':') >= 0)
        {
            return ResourceLocation.tryParse(path);
        }
        return AE2CrystalScience.makeId(path);
    }

}
