package com.froggyy277353.aeronet.registry;

import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.block.entity.AnalogDeviceBE;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.network.NetworkManager;
import com.froggyy277353.aeronet.widgets.Connections;
import com.froggyy277353.aeronet.widgets.Node;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AeroNet.MOD_ID)
public class ModCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("getNodes")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof ServerBlockEntity server) {
                                        for (Node node  : server.getNodes())
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(node.getId().toString()),
                                                    false
                                            );
                                        }
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getServerNetworkName")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof ServerBlockEntity server) {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(server.getNetWorkName()),
                                                    false
                                            );
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getServerPassword")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof ServerBlockEntity server) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(server.getNetWorkPassword()),
                                                false
                                        );
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getAnalogDeviceNetwork")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof AnalogDeviceBE server) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(server.getNetworkName()),
                                                false
                                        );
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getAnalogDevicePassword")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof AnalogDeviceBE server) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(server.getNetworkPassword()),
                                                false
                                        );
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getConnectedDevices")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof ServerBlockEntity server) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(server.getConnectedDevices().toString()),
                                                false
                                        );
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getConnections")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof ServerBlockEntity server) {
                                        List<Connections.Connection> allConnections = server.getConnections().getAllConnections();

                                        Map<UUID, String> nameById = new HashMap<>();
                                        for (Node node : server.getNodes()) {
                                            nameById.put(node.getId(), node.getType().toString());
                                        }

                                        if (allConnections.isEmpty()) {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("No connections").withStyle(ChatFormatting.GRAY),
                                                    false
                                            );
                                            return 1;
                                        }

                                        MutableComponent result = Component.literal("From          | Port | To            | Port\n")
                                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
                                        result.append(Component.literal("--------------------------------------------\n")
                                                .withStyle(ChatFormatting.DARK_GRAY));

                                        for (Connections.Connection conn : allConnections) {
                                            String fromName = nameById.getOrDefault(conn.fromId(), conn.fromId().toString().substring(0, 8));
                                            String toName = nameById.getOrDefault(conn.toId(), conn.toId().toString().substring(0, 8));

                                            String line = String.format(
                                                    "%-14s | %-4d | %-14s | %-4d\n",
                                                    fromName, conn.fromPort(), toName, conn.toPort()
                                            );

                                            result.append(Component.literal(line).withStyle(ChatFormatting.WHITE));
                                        }

                                        context.getSource().sendSuccess(() -> result, false);
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("getRegisteredServers")
                        .executes(context -> {

                            ServerLevel level = context.getSource().getLevel();
                            context.getSource().sendSuccess(
                                    () -> Component.literal(NetworkManager.getInstance().getServers().toString()).withStyle(ChatFormatting.AQUA),
                                    false
                            );
                            return 0;
                        })
        );

        dispatcher.register(
                Commands.literal("registerServer")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    ServerLevel level = context.getSource().getLevel();

                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                            context,
                                            "pos"
                                    );

                                    BlockEntity be = level.getBlockEntity(pos);

                                    if (be instanceof ServerBlockEntity server) {
                                        NetworkManager.getInstance().register(server);
                                        return 1;
                                    }

                                    return 0;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("setNetwork")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("Network name", StringArgumentType.string())
                                    .executes(context -> {

                                        ServerLevel level = context.getSource().getLevel();

                                        BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                                context,
                                                "pos"
                                        );

                                        BlockEntity be = level.getBlockEntity(pos);

                                        if (be instanceof ServerBlockEntity server) {
                                            server.setNetworkName(StringArgumentType.getString(context,"Network name"));
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(server.getNetWorkName()),
                                                    false
                                            );
                                            return 1;
                                        }

                                        return 0;
                                    })
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("setPassword")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("Network password", StringArgumentType.string())
                                        .executes(context -> {

                                            ServerLevel level = context.getSource().getLevel();

                                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(
                                                    context,
                                                    "pos"
                                            );

                                            BlockEntity be = level.getBlockEntity(pos);

                                            if (be instanceof ServerBlockEntity server) {
                                                server.setNetWorkPassword(StringArgumentType.getString(context,"Network password"));
                                                context.getSource().sendSuccess(
                                                        () -> Component.literal(server.getNetWorkPassword()),
                                                        false
                                                );
                                                return 1;
                                            }

                                            return 0;
                                        })
                                )
                        )
        );

    }
}