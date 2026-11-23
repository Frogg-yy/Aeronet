package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.block.AnalogGearshiftBlock;
import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import com.froggyy277353.aeronet.network.GearshiftControl;
import com.froggyy277353.aeronet.network.PacketType;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

public class AnalogGearshiftBlockEntity extends SplitShaftBlockEntity implements NetworkDeviceHelper.Owner, NetworkDevice {
    private final NetworkDeviceHelper network = new NetworkDeviceHelper(this);
    private boolean enabled = false;
    private boolean reversed = false;

    public AnalogGearshiftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANALOG_GEARSHIFT_BLOCK_ENTITY.get(), pos, state);
    }
    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void receivePacket(AnalogDevicesPacket packet) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (packet.getType() != PacketType.GEARSHIFT) {
            return;
        }

        if (!packet.getSenderId().equals(network.getNetworkName())) {
            return;
        }

        if (!(packet.getValue() instanceof GearshiftControl control)) {
            return;
        }

        if (enabled == control.enabled() && reversed == control.reversed()) {
            return;
        }
        if (getBlockState().getBlock() instanceof AnalogGearshiftBlock block) {
            block.refreshKinetics(level, getBlockPos());
        }

        enabled = control.enabled();
        reversed = control.reversed();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();

            if (state.getValue(AnalogGearshiftBlock.ENABLED) != enabled) {
                level.setBlock(worldPosition,
                        state.setValue(AnalogGearshiftBlock.ENABLED, enabled),
                        Block.UPDATE_CLIENTS);
            }
            if (state.getValue(AnalogGearshiftBlock.REVERSED) != reversed)
            {
                level.setBlock(worldPosition, state.setValue(AnalogGearshiftBlock.REVERSED, reversed), Block.UPDATE_CLIENTS);
            }
        }
        setChanged();
        sendData();
    }
    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (!hasSource() || face == getSourceFacing()) {
            return 1;
        }

        if (!enabled) {
            return 0;
        }
        return reversed ? -1 : 1;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("enabled", enabled);
        tag.putBoolean("reversed", reversed);
        network.save(tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        enabled = tag.getBoolean("enabled");
        reversed = tag.getBoolean("reversed");
        network.load(tag);
    }


    @Override
    public NetworkDeviceHelper getNetwork() {
        return network;
    }




    @Override
    public Level getOwnerLevel() {
        return level;
    }

    @Override
    public BlockPos getOwnerPos() {
        return getBlockPos();
    }

    @Override
    public BlockState getOwnerState() {
        return getBlockState();
    }

    @Override
    public void ownerSetChanged() {
        setChanged();
    }
}
