package com.froggyy277353.aeronet.block.entity;

import com.froggyy277353.aeronet.block.SurfaceScannerBlock;
import com.froggyy277353.aeronet.block.WallRotation;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.network.AnalogDevicesPacket;
import com.froggyy277353.aeronet.network.PacketType;
import com.froggyy277353.aeronet.registry.ModBlockEntities;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.ScanCell;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

import static com.froggyy277353.aeronet.widgets.physics.display.CanvasSizeNode.MAX_CANVAS_SIZE;


public class SurfaceScannerBlockEntity extends AnalogDeviceBE {
    private float targetAngle = 0.0F;
    private float currentAngle = 0.0F;
    private float prevAngle = 0.0F;
    private boolean initialized = false;
    private ScanCell[][] lastSentMatrix = null;
    private static final float DISTANCE_EPSILON = 0.05f;
    private int gridSize = 16;
    private float fov = 45F;
    private int maxDistance = 64;
    private float currentBeamDist = 0.0f;
    private float prevBeamDist = 12.0f;
    private boolean on = false;
    private boolean ignoreTrans = true;

    public SurfaceScannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCANNER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void runTick(Level level, BlockPos pos, BlockState state) {
        tick(level, pos, state, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SurfaceScannerBlockEntity be) {
        if (level.isClientSide) {
            be.prevAngle = be.currentAngle;
            float diff = be.targetAngle - be.currentAngle;
            if (Math.abs(diff) < 0.05f) {
                be.currentAngle = be.targetAngle;
            } else {
                be.currentAngle += diff * 0.25f;
            }
            be.prevBeamDist = be.currentBeamDist;
            if (be.on) {
                float maxVisual = Math.min(be.maxDistance, 12.0f);
                float targetDist = be.traceCenterBeamDistance(maxVisual);
                be.currentBeamDist += (targetDist - be.currentBeamDist) * 0.4f;
            }
        }
        else
        {
            if (level.getGameTime() % 5 == 0 && be.on)
            {
                be.performMatrixScan(be.gridSize, be.fov, be.maxDistance);
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
        tag.putFloat("TargetAngle", this.targetAngle);
        tag.putBoolean("on", on);
        tag.putFloat("fov", fov);
        tag.putInt("maxDist", maxDistance);
        tag.putInt("grid", gridSize);
        tag.putBoolean("ignoreTrans", ignoreTrans);
        network.save(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.targetAngle = tag.getFloat("TargetAngle");
        this.on = tag.getBoolean("on");
        this.gridSize = tag.getInt("grid");
        this.fov = tag.getFloat("fov");
        this.maxDistance = tag.getInt("maxDist");
        this.ignoreTrans = tag.getBoolean("ignoreTrans");
        if (!this.initialized) {
            this.currentAngle = this.targetAngle;
            this.prevAngle = this.targetAngle;
            this.initialized = true;
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
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

    public void receivePacket(AnalogDevicesPacket packet) {
        if (getServerPos() != null && level != null) {
            BlockEntity serverBe = level.getBlockEntity(getServerPos());
            if (serverBe instanceof ServerBlockEntity) {
                if (packet.getType().equals(PacketType.SCANNER)) {
                    if (packet.getValue() instanceof float[] settings && settings.length >= 3) {
                        float newAngle = GuiUtils.limitValue(settings[0], 0, 90);
                        float newFov = GuiUtils.limitValue(settings[1], 1, 120);
                        boolean newIgnoreTrans = settings[3] == 1;
                        boolean newOn = settings[4]==1;
                        int newGridSize = (int) GuiUtils.limitValue(settings[2], 0, MAX_CANVAS_SIZE);
                        if (Math.abs(newAngle - this.targetAngle) > 1e-4f || Math.abs(newFov - this.fov) > 1e-4f || newGridSize != gridSize || newIgnoreTrans != ignoreTrans || newOn != on) {
                            this.targetAngle = newAngle;
                            this.fov = newFov;
                            this.gridSize = newGridSize;
                            this.ignoreTrans = newIgnoreTrans;
                            this.on = newOn;
                            this.lastSentMatrix = null;
                            currentBeamDist = 0.0f;
                            prevBeamDist = 0.0f;

                            setChanged();
                            if (!level.isClientSide) {
                                BlockState state = getBlockState();
                                level.sendBlockUpdated(worldPosition, state, state, 3);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isOn(){return on;}

    public float getInterpolatedAngle(float partialTick) {
        return Mth.lerp(partialTick, this.prevAngle, this.currentAngle);
    }
    public float getTargetAngle() {
        return targetAngle;
    }

    private static final Map<Block, String> BLOCK_ID_CACHE = new IdentityHashMap<>();

    private static String getCachedBlockId(Block block) {
        return BLOCK_ID_CACHE.computeIfAbsent(block, b -> BuiltInRegistries.BLOCK.getKey(b).toString());
    }
    private boolean isMatrixChanged(ScanCell[][] next) {
        if (this.lastSentMatrix == null || this.lastSentMatrix.length != next.length) {
            return true;
        }
        for (int r = 0; r < next.length; r++) {
            ScanCell[] prevRow = this.lastSentMatrix[r];
            ScanCell[] nextRow = next[r];
            if (prevRow.length != nextRow.length) {
                return true;
            }
            for (int c = 0; c < nextRow.length; c++) {
                ScanCell a = prevRow[c];
                ScanCell b = nextRow[c];
                if (a.hit() != b.hit()
                        || a.isShip() != b.isShip()
                        || !a.blockId().equals(b.blockId())
                        || Math.abs(a.distance() - b.distance()) > DISTANCE_EPSILON) {
                    return true;
                }
            }
        }
        return false;
    }


    private Quaternionf getScannerOrientation(BlockState state) {
        Quaternionf q = getScannerOrientationWithoutHead(state);
        q.rotateX((float) Math.toRadians(this.targetAngle));
        return q;
    }
    private static boolean isIgnoredTransparentBlock(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.isAir()) {
            return true;
        }
        if (state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES)) {
            return true;
        }
        if (state.getCollisionShape(level, pos).isEmpty()) {
            return true;
        }
        return false;
    }
    private static class ScannerClipContext extends ClipContext {
        private final boolean ignoreTransparent;

        public ScannerClipContext(Vec3 from, Vec3 to, boolean ignoreTransparent) {
            super(
                    from,
                    to,
                    ignoreTransparent ? ClipContext.Block.VISUAL : ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    CollisionContext.empty()
            );
            this.ignoreTransparent = ignoreTransparent;
        }

        @Override
        public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos) {
            if (this.ignoreTransparent && isIgnoredTransparentBlock(state, level, pos)) {
                return Shapes.empty();
            }
            return super.getBlockShape(state, level, pos);
        }
    }

    public void performMatrixScan(int gridSize, float fovDegrees, double maxDistance) {
        if (this.level == null || this.level.isClientSide) return;
        BlockPos serverPos = getServerPos();
        if (serverPos == null) return;

        BlockState state = this.getBlockState();
        Quaternionf baseOrientation = getScannerOrientation(state);

        Vector3f forward = new Vector3f(0, 0, -1).rotate(baseOrientation).normalize();
        Vector3f right = new Vector3f(1, 0, 0).rotate(baseOrientation).normalize();
        Vector3f up = new Vector3f(0, 1, 0).rotate(baseOrientation).normalize();

        Vec3 localCenter = Vec3.atCenterOf(this.worldPosition);
        Vec3 localRayOrigin = localCenter.add(forward.x * 0.6, forward.y * 0.6, forward.z * 0.6);
        Vec3 globalStart = Sable.HELPER.projectOutOfSubLevel(this.level, localRayOrigin);

        ScanCell[][] matrix = new ScanCell[gridSize][gridSize];
        double halfTan = Math.tan(Math.toRadians(fovDegrees * 0.5));
        Vector3f rayDir = new Vector3f();

        for (int row = 0; row < gridSize; row++) {
            double ny = 1.0 - (2.0 * (row + 0.5) / gridSize);

            for (int col = 0; col < gridSize; col++) {
                double nx = (2.0 * (col + 0.5) / gridSize) - 1.0;

                rayDir.set(forward)
                        .fma((float) (nx * halfTan), right)
                        .fma((float) (ny * halfTan), up)
                        .normalize();

                Vec3 localEnd = localCenter.add(rayDir.x * maxDistance, rayDir.y * maxDistance, rayDir.z * maxDistance);
                Vec3 globalEnd = Sable.HELPER.projectOutOfSubLevel(this.level, localEnd);

                ClipContext context = new ScannerClipContext(globalStart, globalEnd, this.ignoreTrans);
                BlockHitResult hit = this.level.clip(context);


                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos hitPos = hit.getBlockPos();
                    BlockState hitState = this.level.getBlockState(hitPos);
                    float dist = (float) globalStart.distanceTo(hit.getLocation());
                    String blockId = getCachedBlockId(hitState.getBlock());
                    boolean isShip = Sable.HELPER.getContaining(this.level, hitPos) != null;

                    matrix[row][col] = new ScanCell(true, dist, blockId, isShip);
                } else {
                    matrix[row][col] = ScanCell.EMPTY;
                }
            }
        }

        if (isMatrixChanged(matrix)) {
            this.lastSentMatrix = matrix;
            BlockEntity serverBe = level.getBlockEntity(serverPos);
            if (serverBe instanceof ServerBlockEntity serverObj) {
                AnalogDevicesPacket packet = new AnalogDevicesPacket(getDeviceName(), PacketType.SCANNER, packMatrix(gridSize, matrix));
                serverObj.receivePacket(packet);
            }
        }
    }

    public List<Object> packMatrix(int gridSize, ScanCell[][] cells) {
        List<String> palette = new ArrayList<>();
        Map<String, Integer> paletteIndex = new HashMap<>();
        palette.add("minecraft:air");
        paletteIndex.put("minecraft:air", 0);

        List<Object> packed = new ArrayList<>(2 + gridSize * gridSize * 2);
        packed.add(gridSize);
        packed.add(palette);

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                ScanCell cell = cells[r][c];
                if (!cell.hit()) {
                    packed.add(-1.0f);
                    packed.add(0);
                } else {
                    int id = paletteIndex.computeIfAbsent(cell.blockId(), key -> {
                        int idx = palette.size();
                        palette.add(key);
                        return idx;
                    });
                    int encodedId = cell.isShip() ? (id + 100000) : id;

                    packed.add(cell.distance());
                    packed.add(encodedId);
                }
            }
        }
        return packed;
    }

    public int getGridSize() {
        return gridSize;
    }

    public float getFov() {
        return fov;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public float getInterpolatedBeamDist(float partialTick) {
        return Mth.lerp(partialTick, this.prevBeamDist, this.currentBeamDist);
    }

    private float traceCenterBeamDistance(float maxVisualDist) {
        if (this.level == null) return maxVisualDist;

        BlockState state = this.getBlockState();
        Quaternionf orientation = getScannerOrientation(state);

        Vector3f pivotOffset = new Vector3f(8.0f / 16.0f - 0.5f, 5.0f / 16.0f - 0.5f, 3.0f / 16.0f - 0.5f);
        Quaternionf bodyOrientation = getScannerOrientationWithoutHead(state);
        pivotOffset.rotate(bodyOrientation);

        Vec3 localPivot = Vec3.atCenterOf(this.worldPosition).add(pivotOffset.x, pivotOffset.y, pivotOffset.z);
        Vector3f forward = new Vector3f(0, 0, -1).rotate(orientation).normalize();

        Vec3 localStart = localPivot.add(forward.x * 0.25, forward.y * 0.25, forward.z * 0.25);
        Vec3 localEnd = localPivot.add(forward.x * maxVisualDist, forward.y * maxVisualDist, forward.z * maxVisualDist);
        Vec3 globalStart = Sable.HELPER.projectOutOfSubLevel(this.level, localStart);
        Vec3 globalEnd = Sable.HELPER.projectOutOfSubLevel(this.level, localEnd);

        ClipContext context = new ScannerClipContext(globalStart, globalEnd, ignoreTrans);
        BlockHitResult hit = this.level.clip(context);

        if (hit.getType() == HitResult.Type.BLOCK) {
            return (float) globalStart.distanceTo(hit.getLocation()) + 0.25f;
        }
        return maxVisualDist;
    }

    private Quaternionf getScannerOrientationWithoutHead(BlockState state) {
        Direction facing = state.getValue(SurfaceScannerBlock.FACING);
        WallRotation wallRot = state.getValue(SurfaceScannerBlock.ROTATION);
        Quaternionf q = new Quaternionf();

        float wallAngle = switch (wallRot) {
            case UP -> 00f;
            case RIGHT -> 90f;
            case DOWN -> 180f;
            case LEFT -> -90f;
        };

        float floorAngle = switch (wallRot) {
            case UP -> 0f;
            case RIGHT -> -90f;
            case DOWN -> 180f;
            case LEFT -> 90f;
        };

        float ceilingAngle = switch (wallRot) {
            case UP -> 180f;
            case RIGHT -> -90f;
            case DOWN -> 0f;
            case LEFT -> 90f;
        };

        switch (facing) {
            case UP -> q.rotateY((float) Math.toRadians(floorAngle));
            case DOWN -> {
                q.rotateX((float) Math.toRadians(180f));
                q.rotateY((float) Math.toRadians(ceilingAngle));
            }
            case SOUTH -> {
                q.rotateX((float) Math.toRadians(90f));
                q.rotateY((float) Math.toRadians(wallAngle));
            }
            case NORTH -> {
                q.rotateY((float) Math.toRadians(180f));
                q.rotateX((float) Math.toRadians(90f));
                q.rotateY((float) Math.toRadians(wallAngle));
            }
            case WEST -> {
                q.rotateY((float) Math.toRadians(-90f));
                q.rotateX((float) Math.toRadians(90f));
                q.rotateY((float) Math.toRadians(wallAngle));
            }
            case EAST -> {
                q.rotateY((float) Math.toRadians(90f));
                q.rotateX((float) Math.toRadians(90f));
                q.rotateY((float) Math.toRadians(wallAngle));
            }
        }
        return q;
    }
}
