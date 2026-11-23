package com.froggyy277353.aeronet.registry;


import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AeroNet.MOD_ID);

    public static final DeferredBlock<Block> ANALOG_LINK =
            BLOCKS.register("analog_link",
                    () -> new AnalogLinkBlock(BlockBehaviour.Properties.of()));
    public  static final DeferredBlock<Block> SERVER_BLOCK =
            BLOCKS.register("server_block",
                    () -> new ServerBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> SERVO =
            BLOCKS.register("servo", () -> new ServoBlock(BlockBehaviour.Properties.of().strength(3.0F).noOcclusion()));

    public  static final DeferredBlock<Block> ANALOG_GEARSHIFT =
            BLOCKS.register("analog_gearshift",
                    () -> new AnalogGearshiftBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> DISPLAY =
            BLOCKS.register("display", () -> new DisplayBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> SCANNER  =
            BLOCKS.register("surface_scanner", () -> new SurfaceScannerBlock(BlockBehaviour.Properties.of().noOcclusion()));

}
