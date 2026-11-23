package com.froggyy277353.aeronet.client.gui;

import com.froggyy277353.aeronet.network.servernodespayloads.NetworkResult;
import net.minecraft.core.BlockPos;

public interface NetworkResultReceiver {
    void receiveNetworkResult(BlockPos pos, NetworkResult result);
}
