package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import net.minecraft.core.BlockPos;

public interface NetworkDevice {
    NetworkDeviceHelper getNetwork();
    BlockPos getBlockPos();
    void receivePacket(AnalogDevicesPacket packet);
}
