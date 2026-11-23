package com.froggyy277353.aeronet.network.servernodespayloads;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AnalogDeviceNetworkPayload(BlockPos pos, String networkName, String networkPassword, String deviceName, BlockPos serverPos, NetworkEnum networkEnum) implements CustomPacketPayload {
    public static final Type<AnalogDeviceNetworkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "analog_device_network"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnalogDeviceNetworkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.networkName());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.networkPassword());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.deviceName());
                BlockPos.STREAM_CODEC.encode(buf, payload.serverPos);
                buf.writeByte(payload.networkEnum.ordinal());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                String networkName = ByteBufCodecs.STRING_UTF8.decode(buf);
                String networkPassword = ByteBufCodecs.STRING_UTF8.decode(buf);
                String deviceName = ByteBufCodecs.STRING_UTF8.decode(buf);
                BlockPos serverPos = BlockPos.STREAM_CODEC.decode(buf);
                NetworkEnum networkEnum = NetworkEnum.values()[buf.readByte()];
                return new AnalogDeviceNetworkPayload(pos, networkName, networkPassword, deviceName, serverPos, networkEnum);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}