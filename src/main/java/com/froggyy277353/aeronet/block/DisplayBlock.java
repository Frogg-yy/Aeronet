package com.froggyy277353.aeronet.block;

import com.froggyy277353.aeronet.block.entity.DisplayBlockEntity;
import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.block.entity.ServoBlockEntity;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.client.gui.AnalogDevicesScreen;
import com.froggyy277353.aeronet.client.gui.ServerSreen;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DisplayBlock extends Block implements EntityBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 6);
    private static final VoxelShape NORTH = Block.box(0, 0, 10, 16, 16, 16);
    private static final VoxelShape EAST = Block.box(0, 0, 0, 6, 16, 16);
    private static final VoxelShape WEST = Block.box(10, 0, 0, 16, 16, 16);

    public DisplayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        Direction facing = clicked.getAxis().isHorizontal() ? clicked : context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> SOUTH;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DisplayBlockEntity(pos, state); }

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
            if (blockEntity instanceof DisplayBlockEntity) {
                final DisplayBlockEntity display = (DisplayBlockEntity)blockEntity;
                Minecraft.getInstance().setScreen((Screen)new AnalogDevicesScreen(pos, (NetworkDevice)display));
            }
        }
        return InteractionResult.SUCCESS;
    }
}