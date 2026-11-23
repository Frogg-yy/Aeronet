package com.froggyy277353.aeronet.network.servernodespayloads;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record NodeDeletePayload(BlockPos pos, UUID id) implements CustomPacketPayload {
    public static final Type<NodeDeletePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "delete_node_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeDeletePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                buf.writeLong(payload.id().getMostSignificantBits());
                buf.writeLong(payload.id().getLeastSignificantBits());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                UUID id = new UUID(buf.readLong(), buf.readLong());
                return new NodeDeletePayload(pos, id);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
