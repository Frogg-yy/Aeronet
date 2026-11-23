package com.froggyy277353.aeronet.block;

import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.block.entity.ServoBlockEntity;
import com.froggyy277353.aeronet.block.entity.SurfaceScannerBlockEntity;
import com.froggyy277353.aeronet.client.gui.AnalogDevicesScreen;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.SurfaceScannerNode;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllItems;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SurfaceScannerBlock extends Block implements EntityBlock, IWrenchable {

    public static final MapCodec<SurfaceScannerBlock> CODEC = simpleCodec(SurfaceScannerBlock::new);
    public static final EnumProperty<WallRotation> ROTATION = EnumProperty.create("rotation", WallRotation.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final VoxelShape UP = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape DOWN = Block.box(0, 8, 0, 16, 16, 16);
    private static final VoxelShape NORTH = Block.box(0, 0, 8, 16, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 8);
    private static final VoxelShape EAST = Block.box(0, 0, 0, 8, 16, 16);
    private static final VoxelShape WEST = Block.box(8, 0, 0, 16, 16, 16);

    public SurfaceScannerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(ROTATION, WallRotation.UP));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();

        if (face == Direction.UP || face == Direction.DOWN) {
            WallRotation floorRot = switch (context.getHorizontalDirection()) {
                case SOUTH -> WallRotation.UP;
                case WEST  -> WallRotation.RIGHT;
                case NORTH -> WallRotation.DOWN;
                case EAST  -> WallRotation.LEFT;
                default    -> WallRotation.UP;
            };
            return this.defaultBlockState()
                    .setValue(FACING, face)
                    .setValue(ROTATION, floorRot);
        }

        BlockPos clickedPos = context.getClickedPos().relative(face.getOpposite());
        Vec3 hit = context.getClickLocation().subtract(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ());

        double u = switch (face) {
            case NORTH -> 1.0 - hit.x;
            case SOUTH -> hit.x;
            case WEST  -> hit.z;
            case EAST  -> 1.0 - hit.z;
            default    -> 0.5;
        };
        double v = hit.y;

        double du = u - 0.5;
        double dv = v - 0.5;

        WallRotation rotation;
        if (Math.abs(dv) > Math.abs(du)) {
            rotation = dv > 0 ? WallRotation.UP : WallRotation.DOWN;
        } else {
            rotation = du > 0 ? WallRotation.RIGHT : WallRotation.LEFT;
        }

        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(ROTATION, rotation);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        WallRotation next = switch (originalState.getValue(ROTATION)) {
            case UP    -> WallRotation.RIGHT;
            case RIGHT -> WallRotation.DOWN;
            case DOWN  -> WallRotation.LEFT;
            case LEFT  -> WallRotation.UP;
        };
        return originalState.setValue(ROTATION, next);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SCANNER_BLOCK_ENTITY.get(), SurfaceScannerBlockEntity::tick);
    }


    private static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> serverType, BlockEntityType<E> entityType, BlockEntityTicker<? super E> ticker) {
        //noinspection unchecked
        return serverType == entityType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SurfaceScannerBlockEntity(pos, state);
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
            if (blockEntity instanceof SurfaceScannerBlockEntity) {
                final SurfaceScannerBlockEntity scanner = (SurfaceScannerBlockEntity)blockEntity;
                Minecraft.getInstance().setScreen((Screen)new AnalogDevicesScreen(pos, (NetworkDevice)scanner));
            }
        }
        return InteractionResult.SUCCESS;
    }

}
