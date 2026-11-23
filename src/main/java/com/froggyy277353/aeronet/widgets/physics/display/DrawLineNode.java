package com.froggyy277353.aeronet.widgets.physics.display;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;

import java.util.List;
import java.util.UUID;

public class DrawLineNode extends DrawNode {
    public DrawLineNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                        List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, width, inputArray, outputArray,
                "aeronet.node.draw_line",
                new String[]{"x0", "y0", "x1", "y1"},
                new float[]{0, 0, 7, 7},
                null);
    }

    @Override
    protected void draw(Canvas.Data canvas, float[] p, boolean fill, int on) {
        Canvas.line(canvas, Math.round(p[0]), Math.round(p[1]), Math.round(p[2]), Math.round(p[3]), on);
    }

    @Override
    public NodeType getType() {
        return NodeType.DRAW_LINE;
    }
}
