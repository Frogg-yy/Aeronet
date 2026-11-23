package com.froggyy277353.aeronet.registry;

import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.block.entity.server.ServerModuleType;
import com.froggyy277353.aeronet.item.ServerModuleItem;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public  static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AeroNet.MOD_ID);

    public static final DeferredItem<BlockItem> ANALOG_LINK =
            ITEMS.registerSimpleBlockItem(
                    "analog_link",
                    ModBlocks.ANALOG_LINK);
    public  static final DeferredItem<BlockItem> SERVER_BLOCK =
            ITEMS.registerSimpleBlockItem("server_block",
                     ModBlocks.SERVER_BLOCK);

    public static final DeferredItem<BlockItem> SERVO = ITEMS.registerSimpleBlockItem("servo", ModBlocks.SERVO);

    public  static final DeferredItem<BlockItem> ANALOG_GEARSHIFT =
            ITEMS.registerSimpleBlockItem("analog_gearshift",
                    ModBlocks.ANALOG_GEARSHIFT);


    public static final DeferredItem<BlockItem> DISPLAY =
            ITEMS.registerSimpleBlockItem("display", ModBlocks.DISPLAY);

    public static final DeferredItem<ServerModuleItem> GYROSCOPE_MODULE =
            ITEMS.register("gyroscope_module",
                    () -> new ServerModuleItem(
                            ServerModuleType.GYROSCOPE,
                            new Item.Properties().stacksTo(1)
                    ));

    public static final DeferredItem<ServerModuleItem> ALTITUDE_MODULE =
            ITEMS.register("altitude_module",
                    () -> new ServerModuleItem(
                            ServerModuleType.ALTITUDE,
                            new Item.Properties().stacksTo(1)
                    ));

    public static final DeferredItem<ServerModuleItem> COMPASS_MODULE =
            ITEMS.register("compass_module",
                    () -> new ServerModuleItem(
                            ServerModuleType.COMPASS,
                            new Item.Properties().stacksTo(1)
                    ));

    public static final DeferredItem<ServerModuleItem> LINK_MODULE =
            ITEMS.register("link_module", () -> new ServerModuleItem(ServerModuleType.LINK,
                    new Item.Properties().stacksTo(1)));

    public static final DeferredItem<ServerModuleItem> GPS_MODULE =
            ITEMS.register("gps_module",
                    () -> new ServerModuleItem(
                            ServerModuleType.GPS,
                            new Item.Properties().stacksTo(1)
                    ));

    public static final DeferredItem<BlockItem> SCANNER =
            ITEMS.registerSimpleBlockItem("surface_scanner", ModBlocks.SCANNER);
}
