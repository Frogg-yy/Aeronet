package com.froggyy277353.aeronet;

import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.transmission.SplitShaftRenderer;
import com.simibubi.create.content.kinetics.transmission.SplitShaftVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = AeroNet.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = AeroNet.MOD_ID, value = Dist.CLIENT)
public class AeroNetClient {
    public AeroNetClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SimpleBlockEntityVisualizer
                    .builder(ModBlockEntities.ANALOG_GEARSHIFT_BLOCK_ENTITY.get())
                    .factory(SplitShaftVisual::new)
                    .skipVanillaRender(be -> true)
                    .apply();
        });
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SERVO_BLOCK_ENTITY.get(),
                com.froggyy277353.aeronet.client.ServoRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY_BLOCK_ENTITY.get(),
                com.froggyy277353.aeronet.client.DisplayRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.ANALOG_GEARSHIFT_BLOCK_ENTITY.get(),
                SplitShaftRenderer::new
        );
    }


}
