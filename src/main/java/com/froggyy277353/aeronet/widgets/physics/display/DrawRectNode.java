package com.froggyy277353.aeronet.widgets.physics.display;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class DrawRectNode extends DrawNode {
    public DrawRectNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                        List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, width, inputArray, outputArray,
                "aeronet.node.draw_rect",
                new String[]{"x", "y",   Component.translatable("aeronet.node.canvas_size.width").getString(), Component.translatable("aeronet.node.canvas_size.height").getString()},
                new float[]{0, 0, 3, 3},
                Component.translatable("aeronet.node.draw.fill").getString());
    }

    @Override
    protected void draw(Canvas.Data canvas, float[] p, boolean fill, int on) {
        Canvas.rect(canvas, Math.round(p[0]), Math.round(p[1]), Math.round(p[2]), Math.round(p[3]), fill, on);
    }

    @Override
    public NodeType getType() {
        return NodeType.DRAW_RECT;
    }
}
