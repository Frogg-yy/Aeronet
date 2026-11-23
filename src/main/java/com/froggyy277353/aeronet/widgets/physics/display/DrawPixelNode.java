package com.froggyy277353.aeronet.widgets.physics.display;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.UUID;

public class DrawPixelNode extends DrawNode {
    public DrawPixelNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                         List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, width, inputArray, outputArray,
                "aeronet.node.draw_pixel",
                new String[]{"x", "y"},
                new float[]{0, 0},
                null);
    }

    @Override
    protected void draw(Canvas.Data canvas, float[] p, boolean fill, int on) {
        Canvas.set(canvas, Math.round(p[0]), Math.round(p[1]), on);
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        super.setExtraData(tag);
        if (!tag.contains("p0") && tag.contains("value1")) {
            setParam(0, tag.getInt("value1"));
        }
        if (!tag.contains("p1") && tag.contains("value2")) {
            setParam(1, tag.getInt("value2"));
        }
    }

    @Override
    public NodeType getType() {
        return NodeType.DRAW_PIXEL;
    }
}
