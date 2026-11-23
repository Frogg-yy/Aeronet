package com.froggyy277353.aeronet.network;

import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.client.ClientEvents;
import com.froggyy277353.aeronet.network.servernodespayloads.*;
import com.froggyy277353.aeronet.widgets.Node;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;

@EventBusSubscriber(modid = AeroNet.MOD_ID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(LinkActionPayload.TYPE, LinkActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player().level().getBlockEntity(payload.pos()) instanceof ServerBlockEntity server) {
                        ServerLinks.action(server, payload);
                    }
                }));
        registrar.playToClient(LinkSyncPayload.TYPE, LinkSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientEvents.receiveLinkSync(payload)));

        registrar.playToServer(
                UpdateNodesPayload.TYPE,
                UpdateNodesPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player.level().getBlockEntity(payload.pos()) instanceof ServerBlockEntity be) {
                        be.receiveUpdateNodesPacket(payload.nodeData());
                    }
                })
        );

        registrar.playToServer(
                UpdateConnectionsPayload.TYPE,
                UpdateConnectionsPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player.level().getBlockEntity(payload.pos()) instanceof ServerBlockEntity be) {
                        be.receiveUpdateConnectionsPacket(payload.connections());
                    }
                })
        );
        registrar.playToServer(
                NodeDeletePayload.TYPE,
                NodeDeletePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player.level().getBlockEntity(payload.pos()) instanceof ServerBlockEntity be) {
                        be.receiveDeleteNodePacket(payload.id());
                    }
                })
        );

        registrar.playToClient(
                NodesSyncPayload.TYPE,
                NodesSyncPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player().level().getBlockEntity(payload.pos())
                                instanceof ServerBlockEntity be) {
                            ClientEvents.trackCompassServer(be);
                            for (NodesSyncPayload.NodeSyncData data : payload.nodes()) {
                                for (Node node : be.getNodes()) {
                                    if (node.getId().equals(data.id())) {
                                        node.setInputArray(
                                                new ArrayList<>(data.inputValues())
                                        );
                                        node.setOutputArray(
                                                new ArrayList<>(data.outputValues())
                                        );
                                        break;
                                    }
                                }
                            }
                        }
                    });
                }
        );

        registrar.playToClient(
                NetworkResultPayload.TYPE,
                NetworkResultPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        ClientEvents.receiveNetworkResult(payload))
        );

        registrar.playToServer(
                NetworkPayload.TYPE,
                NetworkPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player.level().getBlockEntity(payload.pos()) instanceof ServerBlockEntity be) {
                        NetworkResult result = be.receiveNetworkPayload(payload.networkName(), payload.networkPassword(), payload.connection());
                        PacketDistributor.sendToPlayer(player, new NetworkResultPayload(payload.pos(), result));
                    }
                })
        );

        registrar.playToServer(
                AnalogDeviceNetworkPayload.TYPE,
                AnalogDeviceNetworkPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (player.level().getBlockEntity(payload.pos()) instanceof NetworkDevice be) {
                        NetworkResult result = be.getNetwork().receiveNetworkPayload(payload.networkName(), payload.networkPassword(), payload.deviceName(), payload.serverPos(), payload.networkEnum());
                        PacketDistributor.sendToPlayer(player, new NetworkResultPayload(payload.pos(), result));
                    }
                })
        );

    }
}
