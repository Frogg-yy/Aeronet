package com.froggyy277353.aeronet.network;

import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

import java.util.UUID;

public record NodesData(
        UUID id, int x, int y, int width, int height, int inputs, int outputs,
        List<NodeValue> inputArray, List<NodeValue> outputArray,
        CompoundTag extraData,
        NodeType nodeType
) {}