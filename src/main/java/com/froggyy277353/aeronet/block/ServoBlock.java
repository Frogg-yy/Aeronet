package com.froggyy277353.aeronet.block;

import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.block.entity.ServoBlockEntity;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.client.gui.AnalogDevicesScreen;
import com.froggyy277353.aeronet.client.gui.ServerSreen;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ServoBlock extends AbstractEncasedShaftBlock implements IBE<ServoBlockEntity>, IWrenchable {
    public ServoBlock(Properties properties) { super(properties); }

    @Override
    public Class<ServoBlockEntity> getBlockEntityClass() { return ServoBlockEntity.class; }

    @Override
    public BlockEntityType<? extends ServoBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SERVO_BLOCK_ENTITY.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_WRENCH)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hit) {
        if (level.isClientSide) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ServoBlockEntity) {
                final ServoBlockEntity servo = (ServoBlockEntity)blockEntity;
                Minecraft.getInstance().setScreen((Screen)new AnalogDevicesScreen(pos, (NetworkDevice)servo));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
