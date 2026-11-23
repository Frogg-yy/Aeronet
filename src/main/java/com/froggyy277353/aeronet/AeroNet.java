package com.froggyy277353.aeronet;

import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.froggyy277353.aeronet.registry.ModMenus;
import com.froggyy277353.aeronet.registry.ModTabs;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static com.froggyy277353.aeronet.client.ClientEvents.registerBindings;
import static com.froggyy277353.aeronet.registry.ModBlocks.BLOCKS;
import static com.froggyy277353.aeronet.registry.ModItems.ITEMS;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AeroNet.MOD_ID)
public class AeroNet {
    public static final String MOD_ID = "aeronet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AeroNet(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        ModMenus.MENUS.register(modEventBus);


        modEventBus.addListener(this::addCreative);


        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {


    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
