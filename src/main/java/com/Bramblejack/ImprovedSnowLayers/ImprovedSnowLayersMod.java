package com.Bramblejack.ImprovedSnowLayers;

import com.Bramblejack.ImprovedSnowLayers.config.ImprovedSnowLayersConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ImprovedSnowLayersMod.MODID)
public class ImprovedSnowLayersMod {

    public static final String MODID = "improvedsnowlayers";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ImprovedSnowLayersMod(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ImprovedSnowLayersConfig.load();
        LOGGER.info("Improved Snow Layers config loaded");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(ImprovedSnowLayersConfig::buildScreen));
        }
    }
}
