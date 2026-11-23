package com.froggyy277353.aeronet.block;

import com.froggyy277353.aeronet.block.entity.AnalogLinkBlockEntity;
import com.froggyy277353.aeronet.block.entity.NetworkDevice;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

import net.minecraft.world.phys.shapes.VoxelShape;

public class AnalogLinkBlock extends Block implements EntityBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape UP = Block.box(2,0,2,14,3,14);

    private static final VoxelShape DOWN =  Block.box(2,13,2,14,16,14);

    private static final VoxelShape NORTH = Block.box(3,0.5,0,13,14.5,4);

    private static final VoxelShape SOUTH = Block.box(3, 0.5, 12, 13, 14.5, 16);

    private static final VoxelShape EAST = Block.box(0, 0.5, 3, 4, 14.5, 13);

    private static final VoxelShape WEST = Block.box(12, 0.5, 3, 16, 14.5, 13);

    public AnalogLinkBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {

        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ANALOG_LINK_BLOCK_ENTITY.get(), AnalogLinkBlockEntity::tick);
    }

    private static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> serverType, BlockEntityType<E> entityType, BlockEntityTicker<? super E> ticker) {
        //noinspection unchecked
        return serverType == entityType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new AnalogLinkBlockEntity(pos, state);
    }
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof AnalogLinkBlockEntity link) {
            return link.getRedstonePower();
        }

        return 0;
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
            if (blockEntity instanceof AnalogLinkBlockEntity) {
                final AnalogLinkBlockEntity link = (AnalogLinkBlockEntity) blockEntity;
                Minecraft.getInstance().setScreen((Screen)new AnalogDevicesScreen(pos, (NetworkDevice)link));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
