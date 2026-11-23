package com.froggyy277353.aeronet.network.servernodespayloads;

import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

// 0: connect, 1/2: add input/output, 3/4: remove last input/output.
public record LinkActionPayload(BlockPos pos, UUID nodeId, int action, ValueType valueType,
                                String network, String password) implements CustomPacketPayload {
    public static final Type<LinkActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "link_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LinkActionPayload> STREAM_CODEC = StreamCodec.of(
            (b, p) -> {
                b.writeBlockPos(p.pos); b.writeUUID(p.nodeId); b.writeVarInt(p.action);
                b.writeEnum(p.valueType); b.writeUtf(p.network, 64); b.writeUtf(p.password, 64);
            }, b -> new LinkActionPayload(b.readBlockPos(), b.readUUID(), b.readVarInt(),
                    b.readEnum(ValueType.class), b.readUtf(64), b.readUtf(64)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
