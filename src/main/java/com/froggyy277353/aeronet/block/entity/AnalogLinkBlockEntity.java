package com.froggyy277353.aeronet.block.entity;


import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import com.froggyy277353.aeronet.network.PacketType;
import com.froggyy277353.aeronet.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class AnalogLinkBlockEntity extends AnalogDeviceBE {
    private int redstonePower;
    private boolean isSender = true;

    public AnalogLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANALOG_LINK_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void runTick(Level level, BlockPos pos, BlockState state) {
        tick(level, pos, state, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AnalogLinkBlockEntity be) {

        if (level == null || level.isClientSide) return;
        int signal = level.getBestNeighborSignal(pos);
        if (be.getServerPos() != null) {
                AnalogDevicesPacket packet = new AnalogDevicesPacket(be.getDeviceName(), PacketType.ANALOG, signal);
                BlockEntity serverBe = level.getBlockEntity(be.getServerPos());
                if (serverBe instanceof ServerBlockEntity serverObj) {
                    serverObj.receivePacket(packet);
                }

        }

    }

    @Override
    public NetworkDeviceHelper getNetwork() {
        return network;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        network.save(tag);
    }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        network.load(tag);
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void receivePacket(AnalogDevicesPacket packet)
    {
        if ( getServerPos() != null)
        {
            BlockEntity serverBe = level.getBlockEntity(getServerPos());
            if (serverBe instanceof ServerBlockEntity serverObj && getServerPos() != null)
            {
                if (packet.getSenderId().equals(serverObj.getNetWorkName()) && packet.getType().equals(PacketType.ANALOG))
                {
                    setRedstonePower(((Number) packet.getValue()).intValue());
                }
                else
                {
                    redstonePower = 0;
                }
            }
        }
    }


    public int getRedstonePower() {
        return redstonePower;
    }
    public void setRedstonePower(int power) {
        redstonePower = Mth.clamp(power, 0, 15);
        setChanged();

        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }
}
