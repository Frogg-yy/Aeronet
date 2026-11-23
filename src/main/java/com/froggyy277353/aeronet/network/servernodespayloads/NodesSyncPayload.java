package com.froggyy277353.aeronet.network.servernodespayloads;

import com.froggyy277353.aeronet.AeroNet;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record NodesSyncPayload(
        BlockPos pos,
        List<NodeSyncData> nodes
) implements CustomPacketPayload {

    public static final Type<NodesSyncPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            AeroNet.MOD_ID,
                            "nodes_sync"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, NodesSyncPayload> STREAM_CODEC =
            StreamCodec.of(

                    (buf, payload) -> {
                        buf.writeBlockPos(payload.pos());

                        buf.writeVarInt(payload.nodes().size());

                        for (NodeSyncData node : payload.nodes()) {
                            buf.writeUUID(node.id());

                            buf.writeVarInt(node.inputValues().size());

                            for (NodeValue value : node.inputValues()) {
                                NodeValue.STREAM_CODEC.encode(buf, value);
                            }

                            buf.writeVarInt(node.outputValues().size());

                            for (NodeValue value : node.outputValues()) {
                                NodeValue.STREAM_CODEC.encode(buf, value);
                            }
                        }
                    },

                    buf -> {
                        BlockPos pos = buf.readBlockPos();

                        int nodeCount = buf.readVarInt();

                        List<NodeSyncData> nodes = new ArrayList<>();

                        for (int i = 0; i < nodeCount; i++) {
                            UUID id = buf.readUUID();

                            int inputCount = buf.readVarInt();
                            List<NodeValue> inputValues =
                                    new ArrayList<>(inputCount);

                            for (int j = 0; j < inputCount; j++) {
                                inputValues.add(
                                        NodeValue.STREAM_CODEC.decode(buf)
                                );
                            }

                            int outputCount = buf.readVarInt();
                            List<NodeValue> outputValues =
                                    new ArrayList<>(outputCount);

                            for (int j = 0; j < outputCount; j++) {
                                outputValues.add(
                                        NodeValue.STREAM_CODEC.decode(buf)
                                );
                            }

                            nodes.add(
                                    new NodeSyncData(
                                            id,
                                            inputValues,
                                            outputValues
                                    )
                            );
                        }

                        return new NodesSyncPayload(pos, nodes);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record NodeSyncData(
            UUID id,
            List<NodeValue> inputValues,
            List<NodeValue> outputValues
    ) {
    }
}