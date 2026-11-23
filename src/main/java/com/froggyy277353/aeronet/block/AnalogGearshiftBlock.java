package com.froggyy277353.aeronet.block;

import com.froggyy277353.aeronet.block.entity.AnalogDeviceBE;
import com.froggyy277353.aeronet.block.entity.AnalogGearshiftBlockEntity;
import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.client.gui.AnalogDevicesScreen;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

import javax.annotation.Nullable;

public class AnalogGearshiftBlock extends AbstractEncasedShaftBlock implements EntityBlock, IWrenchable {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");

    public AnalogGearshiftBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(ENABLED, false)
                .setValue(REVERSED, false));
    }

    @Override
    public void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof KineticBlockEntity kinetic) {
            RotationPropagator.handleAdded(level, pos, kinetic);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnalogGearshiftBlockEntity(pos, state);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
        builder.add(REVERSED);
    }
    public void refreshKinetics(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof KineticBlockEntity kinetic)) {
            return;
        }

        RotationPropagator.handleRemoved(level, pos, kinetic);
        level.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
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
            if (blockEntity instanceof AnalogGearshiftBlockEntity) {
                final AnalogGearshiftBlockEntity gearshiftBlock = (AnalogGearshiftBlockEntity) blockEntity;
                Minecraft.getInstance().setScreen((Screen)new AnalogDevicesScreen(pos, (NetworkDevice)gearshiftBlock));
            }
        }
        return InteractionResult.SUCCESS;
    }

}
