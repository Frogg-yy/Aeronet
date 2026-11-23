package com.froggyy277353.aeronet.network.servernodespayloads;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record NetworkPayload(BlockPos pos, String networkName, String networkPassword, NetworkEnum connection) implements CustomPacketPayload {
    public static final Type<NetworkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "network_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.networkName());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.networkPassword());
                buf.writeByte(payload.connection().ordinal());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                String networkName = ByteBufCodecs.STRING_UTF8.decode(buf);
                String networkPassword = ByteBufCodecs.STRING_UTF8.decode(buf);
                NetworkEnum connection = NetworkEnum.values()[buf.readByte()];
                return new NetworkPayload(pos, networkName, networkPassword, connection);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}