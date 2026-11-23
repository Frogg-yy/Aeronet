package com.froggyy277353.aeronet.client;

import com.froggyy277353.aeronet.block.entity.ServoBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ServoRenderer extends KineticBlockEntityRenderer<ServoBlockEntity> {
    public ServoRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override
    protected void renderSafe(ServoBlockEntity be, float partialTicks, PoseStack pose,
                              MultiBufferSource buffers, int light, int overlay) {
        Direction.Axis axis = be.getBlockState().getValue(BlockStateProperties.AXIS);
        for (Direction face : Direction.values()) {
            if (face.getAxis() != axis) continue;
            double angle = be.hasSource() && face == be.getSourceFacing()
                    ? AnimationTickHolder.getRenderTime(be.getLevel()) * be.getSpeed() * 0.3
                    : be.getRenderAngle(partialTicks);
            angle += getRotationOffsetForPosition(be, be.getBlockPos(), axis);
            var shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), face);
            kineticRotationTransform(shaft, be, axis, (float) Math.toRadians(angle % 360), light);
            shaft.renderInto(pose, buffers.getBuffer(RenderType.solid()));
        }
    }
}
