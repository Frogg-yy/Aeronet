package com.froggyy277353.aeronet.client;

import com.froggyy277353.aeronet.block.SurfaceScannerBlock;
import com.froggyy277353.aeronet.block.WallRotation;
import com.froggyy277353.aeronet.block.entity.SurfaceScannerBlockEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

public class SurfaceScannerRenderer implements BlockEntityRenderer<SurfaceScannerBlockEntity> {
    private static final double PIVOT_X = 8.0 / 16.0;
    private static final double PIVOT_Y = 5.0 / 16.0;
    private static final double PIVOT_Z = 3.0 / 16.0;


    public SurfaceScannerRenderer(BlockEntityRendererProvider.Context context) {
    }
    public static final class ScannerRenderTypes extends RenderType {
        private ScannerRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                   boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        public static final RenderType SCAN_PYRAMID = create(
                "aeronet_scan_pyramid",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false)
        );
    }

    @Override
    public void render(SurfaceScannerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(SurfaceScannerBlock.FACING);
        WallRotation wallRot = state.getValue(SurfaceScannerBlock.ROTATION);

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        applyBlockOrientation(poseStack, facing, wallRot);
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        float smoothAngle = be.getInterpolatedAngle(partialTick);
        poseStack.mulPose(Axis.XP.rotationDegrees(smoothAngle));
        poseStack.pushPose();
        poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ClientEvents.SCANNER_SENSOR_MODEL);
        RenderType renderType = RenderType.cutoutMipped();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        blockRenderer.getModelRenderer().tesselateBlock(
                be.getLevel(),
                model,
                state,
                be.getBlockPos(),
                poseStack,
                consumer,
                false,
                be.getLevel().getRandom(),
                state.getSeed(be.getBlockPos()),
                packedOverlay,
                ModelData.EMPTY,
                renderType
        );
        poseStack.popPose();

        if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(renderType);
        }
        if (be.isOn()) {
            float beamDist = be.getInterpolatedBeamDist(partialTick);
            renderScanPyramid(poseStack, bufferSource, be.getFov(), beamDist, be.getGridSize());
        }


        poseStack.popPose();
    }

    private void renderScanPyramid(PoseStack poseStack, MultiBufferSource bufferSource,
                                   float fovDegrees, float dist, int gridSize) {
        if (dist <= 0.1f || fovDegrees <= 0.1f) return;

        float halfSize = (float) (dist * Math.tan(Math.toRadians(fovDegrees * 0.5)));

        float zEnd = -dist;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer quadConsumer = bufferSource.getBuffer(ScannerRenderTypes.SCAN_PYRAMID);

        float rStart = 0.522f, gStart = 0.522f, bStart = 1.000f; // #8585ff
        float rEnd   = 0.263f, gEnd   = 0.263f, bEnd   = 0.671f; // #4343ab
        float rLine  = 0.400f, gLine  = 0.400f, bLine  = 0.910f; // #6666e8

        float alphaStart = 0.38f;
        float alphaEnd = 0.0f;

        addGradientTriangle(quadConsumer, matrix, 0, 0, 0, -halfSize, halfSize, zEnd, halfSize, halfSize, zEnd,
                rStart, gStart, bStart, alphaStart, rEnd, gEnd, bEnd, alphaEnd);

        addGradientTriangle(quadConsumer, matrix, 0, 0, 0, halfSize, -halfSize, zEnd, -halfSize, -halfSize, zEnd,
                rStart, gStart, bStart, alphaStart, rEnd, gEnd, bEnd, alphaEnd);

        addGradientTriangle(quadConsumer, matrix, 0, 0, 0, -halfSize, -halfSize, zEnd, -halfSize, halfSize, zEnd,
                rStart, gStart, bStart, alphaStart, rEnd, gEnd, bEnd, alphaEnd);

        addGradientTriangle(quadConsumer, matrix, 0, 0, 0, halfSize, halfSize, zEnd, halfSize, -halfSize, zEnd,
                rStart, gStart, bStart, alphaStart, rEnd, gEnd, bEnd, alphaEnd);


        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        addGradientLine(lineConsumer, poseStack.last(), 0, 0, 0, -halfSize, halfSize, zEnd, rLine, gLine, bLine, 0.75f, 0.05f);
        addGradientLine(lineConsumer, poseStack.last(), 0, 0, 0, halfSize, halfSize, zEnd, rLine, gLine, bLine, 0.75f, 0.05f);
        addGradientLine(lineConsumer, poseStack.last(), 0, 0, 0, halfSize, -halfSize, zEnd, rLine, gLine, bLine, 0.75f, 0.05f);
        addGradientLine(lineConsumer, poseStack.last(), 0, 0, 0, -halfSize, -halfSize, zEnd, rLine, gLine, bLine, 0.75f, 0.05f);

        if (gridSize > 1 && gridSize <= 32) {
            float step = (halfSize * 2.0f) / gridSize;
            for (int i = 0; i <= gridSize; i++) {
                float pos = -halfSize + i * step;
                addGradientLine(lineConsumer, poseStack.last(), pos, -halfSize, zEnd, pos, halfSize, zEnd, rLine, gLine, bLine, 0.18f, 0.18f);
                addGradientLine(lineConsumer, poseStack.last(), -halfSize, pos, zEnd, halfSize, pos, zEnd, rLine, gLine, bLine, 0.18f, 0.18f);
            }
        }
    }
    private void addGradientTriangle(VertexConsumer consumer, Matrix4f matrix,
                                     float x0, float y0, float z0,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float r0, float g0, float b0, float a0,
                                     float r1, float g1, float b1, float a1) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(r0, g0, b0, a0);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r0, g0, b0, a0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r1, g1, b1, a1);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r1, g1, b1, a1);
    }

    private void addDoubleSidedGradientTriangle(VertexConsumer consumer, Matrix4f matrix,
                                                float x0, float y0, float z0,
                                                float x1, float y1, float z1,
                                                float x2, float y2, float z2,
                                                float r0, float g0, float b0, float a0,
                                                float r1, float g1, float b1, float a1) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(r0, g0, b0, a0);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r0, g0, b0, a0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r1, g1, b1, a1);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r1, g1, b1, a1);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r0, g0, b0, a0);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r0, g0, b0, a0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r1, g1, b1, a1);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r1, g1, b1, a1);
    }

    private void addGradientTriangle(VertexConsumer consumer, Matrix4f matrix,
                                     float x0, float y0, float z0,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float r, float g, float b, float aStart, float aEnd) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, aStart);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, aStart);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, aEnd);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, aEnd);
    }

    private void addGradientLine(VertexConsumer consumer, PoseStack.Pose pose,
                                 float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float r, float g, float b, float aStart, float aEnd) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-5f) return;
        dx /= len; dy /= len; dz /= len;

        consumer.addVertex(pose.pose(), x0, y0, z0).setColor(r, g, b, aStart).setNormal(pose, dx, dy, dz);
        consumer.addVertex(pose.pose(), x1, y1, z1).setColor(r, g, b, aEnd).setNormal(pose, dx, dy, dz);
    }

    private void applyBlockOrientation(PoseStack poseStack, Direction facing, WallRotation wallRot) {

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
            case UP -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(floorAngle));
            }
            case DOWN -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(180f));
                poseStack.mulPose(Axis.YP.rotationDegrees(ceilingAngle));
            }
            case SOUTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.YP.rotationDegrees(wallAngle));
            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.YP.rotationDegrees(wallAngle));
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.YP.rotationDegrees(wallAngle));
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.YP.rotationDegrees(wallAngle));
            }
        }
    }
    @Override
    public AABB getRenderBoundingBox(SurfaceScannerBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(16.0);
    }



}
