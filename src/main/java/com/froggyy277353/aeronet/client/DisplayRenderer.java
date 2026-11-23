package com.froggyy277353.aeronet.client;

import com.froggyy277353.aeronet.block.DisplayBlock;
import com.froggyy277353.aeronet.block.entity.DisplayBlockEntity;
import com.froggyy277353.aeronet.block.entity.SurfaceScannerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;


public class DisplayRenderer implements BlockEntityRenderer<DisplayBlockEntity> {
    private static final ResourceLocation WHITE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            DisplayBlockEntity display,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        int[] pixels = display.getPixels();

        if (pixels == null || pixels.length < 64) {
            return;
        }

        Direction facing = display.getBlockState().getValue(DisplayBlock.FACING);

        float rotation = switch (facing) {
            case NORTH -> 0.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 180.0F;
        };

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.translate(-0.5, -0.5, -0.5);

        var consumer = buffers.getBuffer(RenderType.entitySolid(WHITE));
        var matrix = pose.last().pose();

        float frontZ = 3.02F / 16.0F;

        float backZ = frontZ - 1.0F / 16.0F;


        float gridOffsetX = 0.0F;
        float gridOffsetY = 0.0F;
        float gridOffsetZ = 0.56F;

        float gridWidth = 1.0F;
        float gridHeight = 1.0F;

        float pixelDepth = 2.0F / 16.0F;

        for (int index = 0; index < 64; index++) {
            int row = index / 8;
            int column = index % 8;

            float x1 = 1.0F - column / 8.0F * gridWidth + gridOffsetX;
            float x0 = 1.0F - (column + 1) / 8.0F * gridWidth + gridOffsetX;

            float y1 = 1.0F - row / 8.0F * gridHeight + gridOffsetY;
            float y0 = 1.0F - (row + 1) / 8.0F * gridHeight + gridOffsetY;

            frontZ = 3.02F / 16.0F + gridOffsetZ;
            backZ = frontZ - pixelDepth;

            int gray = pixels[index];

            int red = gray;
            int green = gray;
            int blue = gray;

            backZ = frontZ - 2.0F / 16.0F;

            vertex(consumer, matrix, pose, x0, y0, frontZ, red, green, blue, 0, 1, 0, 0, 1);
            vertex(consumer, matrix, pose, x1, y0, frontZ, red, green, blue, 1, 1, 0, 0, 1);
            vertex(consumer, matrix, pose, x1, y1, frontZ, red, green, blue, 1, 0, 0, 0, 1);
            vertex(consumer, matrix, pose, x0, y1, frontZ, red, green, blue, 0, 0, 0, 0, 1);

            vertex(consumer, matrix, pose, x1, y0, backZ, red, green, blue, 0, 1, 0, 0, -1);
            vertex(consumer, matrix, pose, x0, y0, backZ, red, green, blue, 1, 1, 0, 0, -1);
            vertex(consumer, matrix, pose, x0, y1, backZ, red, green, blue, 1, 0, 0, 0, -1);
            vertex(consumer, matrix, pose, x1, y1, backZ, red, green, blue, 0, 0, 0, 0, -1);

            vertex(consumer, matrix, pose, x0, y0, backZ, red, green, blue, 0, 1, -1, 0, 0);
            vertex(consumer, matrix, pose, x0, y0, frontZ, red, green, blue, 1, 1, -1, 0, 0);
            vertex(consumer, matrix, pose, x0, y1, frontZ, red, green, blue, 1, 0, -1, 0, 0);
            vertex(consumer, matrix, pose, x0, y1, backZ, red, green, blue, 0, 0, -1, 0, 0);

            vertex(consumer, matrix, pose, x1, y0, frontZ, red, green, blue, 0, 1, 1, 0, 0);
            vertex(consumer, matrix, pose, x1, y0, backZ, red, green, blue, 1, 1, 1, 0, 0);
            vertex(consumer, matrix, pose, x1, y1, backZ, red, green, blue, 1, 0, 1, 0, 0);
            vertex(consumer, matrix, pose, x1, y1, frontZ, red, green, blue, 0, 0, 1, 0, 0);

            vertex(consumer, matrix, pose, x0, y1, frontZ, red, green, blue, 0, 1, 0, 1, 0);
            vertex(consumer, matrix, pose, x1, y1, frontZ, red, green, blue, 1, 1, 0, 1, 0);
            vertex(consumer, matrix, pose, x1, y1, backZ, red, green, blue, 1, 0, 0, 1, 0);
            vertex(consumer, matrix, pose, x0, y1, backZ, red, green, blue, 0, 0, 0, 1, 0);

            vertex(consumer, matrix, pose, x0, y0, backZ, red, green, blue, 0, 1, 0, -1, 0);
            vertex(consumer, matrix, pose, x1, y0, backZ, red, green, blue, 1, 1, 0, -1, 0);
            vertex(consumer, matrix, pose, x1, y0, frontZ, red, green, blue, 1, 0, 0, -1, 0);
            vertex(consumer, matrix, pose, x0, y0, frontZ, red, green, blue, 0, 0, 0, -1, 0);
        }


        pose.popPose();
    }
    @Override
    public AABB getRenderBoundingBox(DisplayBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(2.0);
    }

    private static void vertex(
            VertexConsumer consumer,
            org.joml.Matrix4f matrix,
            PoseStack pose,
            float x, float y, float z,
            int red, int green, int blue,
            float u, float v,
            float nx, float ny, float nz
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), nx, ny, nz);
    }
}
