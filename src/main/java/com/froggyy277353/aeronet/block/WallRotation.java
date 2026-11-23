package com.froggyy277353.aeronet.block;

import net.minecraft.util.StringRepresentable;

public enum WallRotation implements StringRepresentable {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right");

    private final String name;

    WallRotation(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}