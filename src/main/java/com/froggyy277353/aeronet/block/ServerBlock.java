package com.froggyy277353.aeronet.block;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.client.gui.ServerSreen;
import com.froggyy277353.aeronet.item.ServerModuleItem;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class ServerBlock extends Block implements EntityBlock, IWrenchable {
    public static final IntegerProperty SLOT_0 =
            IntegerProperty.create("slot_0", 0, 5);
    public static final IntegerProperty SLOT_1 =
            IntegerProperty.create("slot_1", 0, 5);
    public static final IntegerProperty SLOT_2 =
            IntegerProperty.create("slot_2", 0, 5);
    public static final IntegerProperty SLOT_3 =
            IntegerProperty.create("slot_3", 0, 5);
    public ServerBlock(BlockBehaviour.Properties properties)
    {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SLOT_0, 0)
                .setValue(SLOT_1, 0)
                .setValue(SLOT_2, 0)
                .setValue(SLOT_3, 0));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SLOT_0, SLOT_1, SLOT_2, SLOT_3);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 4),
            Block.box(0, 0, 4, 16, 16, 16),
            Block.box(0, 4, 0, 4, 5, 4)
    );
    private static final VoxelShape SOUTH_SHAPE = rotateShape(Direction.NORTH, Direction.SOUTH, NORTH_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateShape(Direction.NORTH, Direction.WEST, NORTH_SHAPE);
    private static final VoxelShape EAST_SHAPE = rotateShape(Direction.NORTH, Direction.EAST, NORTH_SHAPE);

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SERVER_BLOCK_ENTITY.get(), ServerBlockEntity::tick);
    }

    private static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> serverType, BlockEntityType<E> entityType, BlockEntityTicker<? super E> ticker) {
        return serverType == entityType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!(stack.getItem() instanceof ServerModuleItem) || stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_WRENCH)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int slot = getModuleSlot(state, pos, hit);

        if (slot < 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof ServerBlockEntity server) {
            server.installModule(slot, stack, player);
        }

        return ItemInteractionResult.SUCCESS;
    }
    private int getModuleSlot(
            BlockState state,
            BlockPos pos,
            BlockHitResult hit
    ) {
        Direction facing = state.getValue(FACING);

        double worldX = (hit.getLocation().x - pos.getX()) * 16.0;
        double y = (hit.getLocation().y - pos.getY()) * 16.0;
        double worldZ = (hit.getLocation().z - pos.getZ()) * 16.0;

        double x = switch (facing) {
            case NORTH -> worldX;
            case SOUTH -> 16.0 - worldX;
            case EAST -> worldZ;
            case WEST -> 16.0 - worldZ;
            default -> -1;
        };

        if (x < 0 || x > 4 || y < 4 || y > 5) {
            return -1;
        }

        boolean left = x < 2;
        boolean upper = y >= 4.45;

        if (!upper) {
            return left ? 0 : 1;
        }

        return left ? 2 : 3;
    }

    public static IntegerProperty getSlotProperty(int slot) {
        return switch (slot) {
            case 0 -> SLOT_0;
            case 1 -> SLOT_1;
            case 2 -> SLOT_2;
            case 3 -> SLOT_3;
            default -> throw new IllegalArgumentException("Unknown module slot: " + slot);
        };
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ServerBlockEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof ServerBlockEntity server) {
            drops.addAll(server.getModuleDrops());
        }

        return drops;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            int slot = getModuleSlot(state, pos, hit);
            if (slot >= 0) {
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof ServerBlockEntity server) {
                    server.removeModule(slot, player);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof ServerBlockEntity server) {
                ServerSreen screen = new ServerSreen(pos, server);
                Minecraft.getInstance().setScreen(screen);
                screen.setNodes(server.getNodes());
            }
        }
        return InteractionResult.SUCCESS;
    }
}
