package com.froggyy277353.aeronet.network.servernodespayloads;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NetworkResultPayload(BlockPos pos, NetworkResult result) implements CustomPacketPayload {
    public static final Type<NetworkResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "network_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                buf.writeByte(payload.result().ordinal());
            },
            buf -> new NetworkResultPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    NetworkResult.values()[buf.readUnsignedByte()]
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
