package com.froggyy277353.aeronet.network.servernodespayloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LinkSyncPayload(UpdateNodesPayload node) implements CustomPacketPayload {
    public static final Type<LinkSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("aeronet", "link_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LinkSyncPayload> STREAM_CODEC = StreamCodec.of(
            (b, p) -> UpdateNodesPayload.STREAM_CODEC.encode(b, p.node),
            b -> new LinkSyncPayload(UpdateNodesPayload.STREAM_CODEC.decode(b)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
