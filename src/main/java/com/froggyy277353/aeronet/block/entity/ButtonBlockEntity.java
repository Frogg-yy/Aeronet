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

public class ButtonBlockEntity extends AnalogDeviceBE{
    private boolean isPressed = false;
    private float pressProgress = 0.0f;
    private float prevPressProgress = 0.0f;

    public ButtonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BUTTON_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void runTick(Level level, BlockPos pos, BlockState state) {
        tick(level, pos, state, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ButtonBlockEntity be) {
        if (level == null) {return;}
        if (level.isClientSide) {
            be.prevPressProgress = be.pressProgress;
            float step = 1.0f / 5.0f;

            if (be.isPressed()) {
                be.pressProgress = Math.min(1.0f, be.pressProgress + step);
            } else {
                be.pressProgress = Math.max(0.0f, be.pressProgress - step);
            }
        }

        if (be.getServerPos() != null) {
            BlockEntity serverBe = level.getBlockEntity(be.getServerPos());
            if (serverBe instanceof ServerBlockEntity serverObj) {
                AnalogDevicesPacket packet = new AnalogDevicesPacket(be.getDeviceName(), PacketType.BUTTON, be.isPressed());
                serverObj.receivePacket(packet);
            }
        }
        if (level.getGameTime() % 20 == 0 && be.isPressed())
        {
            be.setPressed(false);
        }
    }

    public boolean isPressed(){return isPressed;}
    public void setPressed(boolean pressed) {
        if (this.isPressed != pressed) {
            this.isPressed = pressed;
            setChanged();
            if (this.level != null && !this.level.isClientSide) {
                BlockState state = getBlockState();
                this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
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
        tag.putBoolean("pressed", isPressed);
        network.save(tag);
    }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isPressed = tag.getBoolean("pressed");
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
    public float getInterpolatedPress(float partialTick) {
        return Mth.lerp(partialTick, this.prevPressProgress, this.pressProgress);
    }
    public void receivePacket(AnalogDevicesPacket packet)
    {
        if ( getServerPos() != null)
        {
            BlockEntity serverBe = level.getBlockEntity(getServerPos());
            if (serverBe instanceof ServerBlockEntity serverObj && getServerPos() != null)
            {

            }
        }
    }
}
