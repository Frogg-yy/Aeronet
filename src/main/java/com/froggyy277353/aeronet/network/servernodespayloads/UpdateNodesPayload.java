package com.froggyy277353.aeronet.network.servernodespayloads;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.network.NodesData;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record UpdateNodesPayload(
        BlockPos pos,
        NodesData nodeData
) implements CustomPacketPayload {

    public static final Type<UpdateNodesPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            "aeronet",
                            "update_nodes_payload"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNodesPayload> STREAM_CODEC =
            StreamCodec.of(

                    (buf, payload) -> {
                        BlockPos.STREAM_CODEC.encode(buf, payload.pos());

                        NodesData data = payload.nodeData();

                        buf.writeLong(
                                data.id().getMostSignificantBits()
                        );

                        buf.writeLong(
                                data.id().getLeastSignificantBits()
                        );

                        buf.writeInt(data.x());
                        buf.writeInt(data.y());
                        buf.writeInt(data.width());
                        buf.writeInt(data.height());

                        buf.writeByte(data.inputs());
                        buf.writeByte(data.outputs());

                        NodeValue.STREAM_CODEC
                                .apply(ByteBufCodecs.list())
                                .encode(buf, data.inputArray());

                        NodeValue.STREAM_CODEC
                                .apply(ByteBufCodecs.list())
                                .encode(buf, data.outputArray());

                        ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(
                                buf,
                                data.extraData()
                        );

                        buf.writeByte(data.nodeType().ordinal());
                    },

                    buf -> {
                        BlockPos pos =
                                BlockPos.STREAM_CODEC.decode(buf);

                        NodesData data = new NodesData(
                                new UUID(
                                        buf.readLong(),
                                        buf.readLong()
                                ),

                                buf.readInt(),
                                buf.readInt(),
                                buf.readInt(),
                                buf.readInt(),

                                buf.readByte(),
                                buf.readByte(),

                                NodeValue.STREAM_CODEC
                                        .apply(ByteBufCodecs.list())
                                        .decode(buf),

                                NodeValue.STREAM_CODEC
                                        .apply(ByteBufCodecs.list())
                                        .decode(buf),

                                ByteBufCodecs.TRUSTED_COMPOUND_TAG
                                        .decode(buf),

                                NodeType.values()[buf.readByte()]
                        );

                        return new UpdateNodesPayload(pos, data);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}