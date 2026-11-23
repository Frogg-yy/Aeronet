package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.network.servernodespayloads.NetworkEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AnalogDeviceBE extends BlockEntity implements NetworkDeviceHelper.Owner, NetworkDevice {
    protected final NetworkDeviceHelper network = new NetworkDeviceHelper(this);

    protected AnalogDeviceBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Level getOwnerLevel() { return level; }
    @Override
    public BlockPos getOwnerPos() { return getBlockPos(); }
    @Override
    public BlockState getOwnerState() { return getBlockState(); }
    @Override
    public void ownerSetChanged() { setChanged(); }

    public void receiveNetworkPayload(String networkName, String networkPassword, String deviceName, BlockPos serverPos, NetworkEnum networkEnum) {
        network.receiveNetworkPayload(networkName, networkPassword, deviceName, serverPos, networkEnum);
    }

    public void connectServer(ServerBlockEntity server) {
        network.connectServer(server);
    }

    public void setDeviceName(String deviceName) { network.setDeviceName(deviceName); }
    public String getDeviceName() { return network.getDeviceName(); }
    public String getNetworkName() { return network.getNetworkName(); }
    public void setNetworkName(String networkName) { network.setNetworkName(networkName); }
    public String getNetworkPassword() { return network.getNetworkPassword(); }
    public void setNetworkPassword(String networkPassword) { network.setNetworkPassword(networkPassword); }
    public BlockPos getServerPos() { return network.getServerPos(); }
    public void setServerPos(BlockPos serverPos) { network.setServerPos(serverPos); }
    public boolean isConnected() { return network.isConnected(); }

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
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag tag = this.saveCustomOnly(registries);
        this.removeComponentsFromTag(tag);
        tag.remove("isConnected");
        tag.remove("connected");
        tag.remove("serverPos");

        if (tag.contains("network", CompoundTag.TAG_COMPOUND)) {
            CompoundTag netTag = tag.getCompound("network");
            netTag.putBoolean("isConnected", false);
            netTag.remove("serverPos");
        }

        BlockItem.setBlockEntityData(stack, this.getType(), tag);
        stack.applyComponents(this.collectComponents());
    }

    public abstract void runTick(Level level, BlockPos pos, BlockState state);
}