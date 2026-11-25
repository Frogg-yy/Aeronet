package com.froggyy277353.aeronet.registry;


import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.block.entity.*;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AeroNet.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerBlockEntity>> SERVER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("server_block_entity",
                    () -> BlockEntityType.Builder.of(ServerBlockEntity::new, ModBlocks.SERVER_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnalogLinkBlockEntity>> ANALOG_LINK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("analog_link_block_entity",
                    () -> BlockEntityType.Builder.of(AnalogLinkBlockEntity::new, ModBlocks.ANALOG_LINK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServoBlockEntity>> SERVO_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("servo",
                    () -> BlockEntityType.Builder.of(ServoBlockEntity::new, ModBlocks.SERVO.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnalogGearshiftBlockEntity>> ANALOG_GEARSHIFT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("analog_gearshift_block_entity",
                    () -> BlockEntityType.Builder.of(AnalogGearshiftBlockEntity::new, ModBlocks.ANALOG_GEARSHIFT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisplayBlockEntity>> DISPLAY_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("display_block_entity",
                    () -> BlockEntityType.Builder.of(DisplayBlockEntity::new, ModBlocks.DISPLAY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SurfaceScannerBlockEntity>> SCANNER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("surface_scanner_block_entity",
                    () -> BlockEntityType.Builder.of(SurfaceScannerBlockEntity::new, ModBlocks.SCANNER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ButtonBlockEntity>> BUTTON_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("button_block_entity",
                    () -> BlockEntityType.Builder.of(ButtonBlockEntity::new, ModBlocks.BUTTON.get()).build(null));


}