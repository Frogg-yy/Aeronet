package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import com.froggyy277353.aeronet.network.PacketType;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ServoBlockEntity extends SplitShaftBlockEntity implements NetworkDeviceHelper.Owner, NetworkDevice {
    private final NetworkDeviceHelper network = new NetworkDeviceHelper(this);
    private double targetAngle;
    private double currentAngle;
    private double previousAngle;
    private float outputModifier;

    public ServoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SERVO_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        previousAngle = currentAngle;
        if (hasSource()) currentAngle += getSpeed() * outputModifier * 0.3;
        if (level.isClientSide) return;

        float next = hasSource() ? ServoMotion.modifier(currentAngle, targetAngle, getSpeed()) : 0;
        if (Float.compare(next, outputModifier) != 0) {
            // Remove connections using the old ratio before propagating the new one.
            detachKinetics();
            outputModifier = next;
            attachKinetics();
            sendData();
        } else if (outputModifier != 0 && level.getGameTime() % 10 == 0) {
            sendData();
        }
        if (currentAngle != previousAngle) setChanged();
    }

    @Override
    public void receivePacket(AnalogDevicesPacket packet) {
        if (level == null || level.isClientSide || packet.getType() != PacketType.SERVO
                || !packet.getSenderId().equals(network.getNetworkName())
                || !(packet.getValue() instanceof Number number)) return;
        double angle = number.doubleValue();
        if (!Double.isFinite(angle) || angle == targetAngle) return;
        targetAngle = angle;
        setChanged();
        sendData();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        return !hasSource() || face == getSourceFacing() ? 1 : outputModifier;
    }

    public double getRenderAngle(float partialTicks) {
        return previousAngle + (currentAngle - previousAngle) * partialTicks;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("TargetAngle", targetAngle);
        tag.putDouble("CurrentAngle", currentAngle);
        tag.putFloat("OutputModifier", outputModifier);
        network.save(tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        targetAngle = tag.getDouble("TargetAngle");
        currentAngle = tag.getDouble("CurrentAngle");
        previousAngle = currentAngle;
        outputModifier = tag.getFloat("OutputModifier");
        network.load(tag);
    }

    @Override public NetworkDeviceHelper getNetwork() { return network; }
    @Override public Level getOwnerLevel() { return level; }
    @Override public BlockPos getOwnerPos() { return getBlockPos(); }
    @Override public BlockState getOwnerState() { return getBlockState(); }
    @Override public void ownerSetChanged() { setChanged(); }
}
