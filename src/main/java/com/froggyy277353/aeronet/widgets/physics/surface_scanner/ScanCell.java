package com.froggyy277353.aeronet.widgets.physics.surface_scanner;

import net.minecraft.world.level.block.state.BlockState;

public record ScanCell(
        boolean hit,
        float distance,
        String blockId,
        boolean isShip
) {
    public static final ScanCell EMPTY = new ScanCell(false, -1.0f, "minecraft:air", false);
}