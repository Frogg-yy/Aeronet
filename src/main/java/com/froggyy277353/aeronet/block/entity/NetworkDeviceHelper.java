package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import com.froggyy277353.aeronet.network.servernodespayloads.NetworkEnum;
import com.froggyy277353.aeronet.network.servernodespayloads.NetworkResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NetworkDeviceHelper {
    private String networkName = "";
    private String networkPassword = "";
    private String deviceName = "";
    private BlockPos serverPos;
    private boolean isConnected = false;

    public interface Owner {
        void receivePacket(AnalogDevicesPacket packet);

        Level getOwnerLevel();
        BlockPos getOwnerPos();
        BlockState getOwnerState();
        void ownerSetChanged();
    }

    private final Owner owner;

    public NetworkDeviceHelper(Owner owner) {
        this.owner = owner;
    }

    public NetworkResult receiveNetworkPayload(String networkName, String networkPassword, String deviceName, BlockPos serverPos, NetworkEnum networkEnum) {
        Level level = owner.getOwnerLevel();

        if (networkEnum.equals(NetworkEnum.CONNECT)) {
            if (networkName == null || networkName.isBlank()
                    || networkPassword == null || networkPassword.isBlank()) {
                return NetworkResult.MISSING_NETWORK_DATA;
            }
            if (deviceName == null || deviceName.isBlank()) {
                return NetworkResult.MISSING_DEVICE_NAME;
            }
            if (serverPos != null && level != null) {
                BlockEntity be = level.getBlockEntity(serverPos);
                if (be instanceof ServerBlockEntity server
                        && server.isConnected()
                        && networkName.equals(server.getNetWorkName())
                        && networkPassword.equals(server.getNetWorkPassword())) {

                    if (!server.canUseDeviceName(owner.getOwnerPos(), deviceName, owner.getClass())) {
                        return NetworkResult.DEVICE_EXISTS;
                    }

                    this.networkName = networkName;
                    this.networkPassword = networkPassword;
                    this.deviceName = deviceName;
                    this.serverPos = serverPos;
                    isConnected = true;
                    server.connectDevice(owner.getOwnerPos());
                    sync();
                    return NetworkResult.CONNECTED;
                }
            }
        } else if (networkEnum.equals(NetworkEnum.DISCONNECT)) {
            isConnected = false;
            if (level != null && serverPos != null) {
                BlockEntity be = level.getBlockEntity(serverPos);
                if (be instanceof ServerBlockEntity server) {
                    server.disconnectDevice(owner.getOwnerPos());
                }
            }
            this.serverPos = null;
            sync();
            return NetworkResult.DISCONNECTED;
        }

        return NetworkResult.NETWORK_NOT_FOUND;
    }

    private void sync() {
        Level level = owner.getOwnerLevel();
        owner.ownerSetChanged();
        if (level != null) {
            level.sendBlockUpdated(owner.getOwnerPos(), owner.getOwnerState(), owner.getOwnerState(), 3);
        }
    }

    public void connectServer(ServerBlockEntity server) {
        this.serverPos = server.getBlockPos();
        server.connectDevice(owner.getOwnerPos());
        Level level = owner.getOwnerLevel();
        if (level != null) {
            level.sendBlockUpdated(owner.getOwnerPos(), owner.getOwnerState(), owner.getOwnerState(), 3);
        }
        owner.ownerSetChanged();
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        owner.ownerSetChanged();
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
        owner.ownerSetChanged();
    }

    public void setNetworkPassword(String networkPassword) {
        this.networkPassword = networkPassword;
        owner.ownerSetChanged();
    }

    public void setServerPos(BlockPos serverPos) {
        this.serverPos = serverPos;
        owner.ownerSetChanged();
    }

    public String getDeviceName() { return deviceName; }
    public String getNetworkName() { return networkName; }
    public String getNetworkPassword() { return networkPassword; }
    public BlockPos getServerPos() { return serverPos; }
    public boolean isConnected() { return isConnected; }

    public void save(CompoundTag tag) {
        if (networkName != null) tag.putString("networkName", networkName);
        if (networkPassword != null) tag.putString("networkPassword", networkPassword);
        if (deviceName != null) tag.putString("device", deviceName);
        if (serverPos != null) tag.putLong("serverPos", serverPos.asLong());
        tag.putBoolean("isConnected", isConnected);
    }

    public void load(CompoundTag tag) {
        this.networkName = tag.contains("networkName") ? tag.getString("networkName") : null;
        this.networkPassword = tag.contains("networkPassword") ? tag.getString("networkPassword") : null;
        this.deviceName = tag.contains("device") ? tag.getString("device") : null;
        this.serverPos = tag.contains("serverPos") ? BlockPos.of(tag.getLong("serverPos")) : null;
        this.isConnected = tag.contains("isConnected") && tag.getBoolean("isConnected");
    }
}
