package com.froggyy277353.aeronet.widgets.booleans;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class NotNode extends Node {

    public NotNode(UUID id, int x, int y, int numInputs, int numOutputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 1, width, height, inputArray, outputArray);
        red = 0.392F; green = 0.455F; blue = 0.545F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        this.width = Math.max(font.width(Component.translatable("aeronet.node.not")) + 4, 10 * font.width("W"));
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.not"), screenX + 2, screenY + 2, 0xFFFFFF);
        logic();
    }
    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        if (inputArray != null)
        {
            outputArray.set(0, NodeValue.of(!inputArray.getFirst().asBoolean()));
        }

    }
    @Override
    public ValueType getInputType(int port) {
        return ValueType.BOOLEAN;
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.BOOLEAN;
    }
    @Override
    public NodeType getType() {
        return NodeType.NOT;
    }

}
