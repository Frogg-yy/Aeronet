package com.froggyy277353.aeronet.network.servernodespayloads;

import com.froggyy277353.aeronet.widgets.Connections;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public record UpdateConnectionsPayload(BlockPos pos, List<Connections.Connection> connections) implements CustomPacketPayload {

    public static final Type<UpdateConnectionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "update_connections_payload"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Connections.Connection> CONNECTION_CODEC = StreamCodec.of(
            (buf, conn) -> {
                buf.writeLong(conn.fromId().getMostSignificantBits());
                buf.writeLong(conn.fromId().getLeastSignificantBits());
                buf.writeByte(conn.fromPort());
                buf.writeLong(conn.toId().getMostSignificantBits());
                buf.writeLong(conn.toId().getLeastSignificantBits());
                buf.writeByte(conn.toPort());
            },
            buf -> new Connections.Connection(
                    new UUID(buf.readLong(), buf.readLong()),
                    buf.readByte(),
                    new UUID(buf.readLong(), buf.readLong()),
                    buf.readByte()
            )
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateConnectionsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                CONNECTION_CODEC.apply(ByteBufCodecs.list()).encode(buf, payload.connections());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                List<Connections.Connection> connections = CONNECTION_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                return new UpdateConnectionsPayload(pos, connections);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}