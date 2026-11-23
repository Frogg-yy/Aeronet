package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import com.froggyy277353.aeronet.network.PacketType;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.froggyy277353.aeronet.widgets.physics.display.DisplayNode;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DisplayBlockEntity extends AnalogDeviceBE {
    private int[] pixels = new int[DisplayNode.PIXEL_COUNT];

    public DisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISPLAY_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public NetworkDeviceHelper getNetwork() { return network; }

    @Override
    public void runTick(Level level, BlockPos pos, BlockState state) { }

    @Override
    public void receivePacket(AnalogDevicesPacket packet) {
        if (level != null && !level.isClientSide()
                && packet.getType().equals(PacketType.DISPLAY)
                && packet.getSenderId().equals(network.getNetworkName())) {

            if (packet.getValue() instanceof List<?> list) {
                int[] nextPixels = new int[DisplayNode.PIXEL_COUNT];
                for (int i = 0; i < DisplayNode.PIXEL_COUNT && i < list.size(); i++) {
                    if (list.get(i) instanceof NodeValue val) {
                        nextPixels[i] = val.asInt();
                    }
                }
                this.pixels = nextPixels;
                setChanged();
                BlockState state = getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }
    }


    public int[] getPixels() { return pixels.clone(); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("pixels", pixels);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pixels = tag.getIntArray("pixels");

    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) { loadAdditional(tag, registries); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}