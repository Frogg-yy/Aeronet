package com.froggyy277353.aeronet.client;

import com.froggyy277353.aeronet.block.ButtonBlock;
import com.froggyy277353.aeronet.block.entity.ButtonBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.Map;

public class ButtonRenderer implements BlockEntityRenderer<ButtonBlockEntity> {
    private static final float ANIM_DURATION_MS = 250.0f;

    private static class AnimState {
        float progress = 0.0f;
        long lastTimeMs = Util.getMillis();
    }

    private final Map<BlockPos, AnimState> states = new HashMap<>();

    public ButtonRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ButtonBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(ButtonBlock.FACING);

        AnimState anim = states.computeIfAbsent(be.getBlockPos(), k -> new AnimState());
        long now = Util.getMillis();
        float delta = (now - anim.lastTimeMs) / ANIM_DURATION_MS;
        anim.lastTimeMs = now;

        if (be.isPressed()) {
            anim.progress = Math.min(1.0f, anim.progress + delta);
        } else {
            anim.progress = Math.max(0.0f, anim.progress - delta);
        }

        float smooth = anim.progress * anim.progress * (3.0f - 2.0f * anim.progress);
        float yOffset = Mth.lerp(smooth, 1.0f / 16.0f, 0.0f);

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case UP -> {}
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.translate(0, yOffset, 0);

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ClientEvents.BUTTON_MODEL);
        RenderType renderType = RenderType.cutoutMipped();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), consumer, state, model,
                1.0F, 1.0F, 1.0F, packedLight, packedOverlay, ModelData.EMPTY, renderType
        );

        poseStack.popPose();
    }
}
