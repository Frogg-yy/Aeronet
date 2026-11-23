package com.froggyy277353.aeronet.widgets;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class ArrayUnpackNode extends Node {
    private int size;

    public ArrayUnpackNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                           List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 1, 54, 12 + PORT_SPACING, inputArray, outputArray);
        red = 0.392F; green = 0.455F; blue = 0.545F;
    }

    @Override public ValueType getInputType(int port) { return ValueType.LIST; }
    @Override public ValueType getOutputType(int port) { return ValueType.BOOLEAN; }
    @Override public boolean acceptsInputType(int port, ValueType type) { return type == ValueType.LIST; }
    @Override public NodeValue convertInputValue(int port, NodeValue value) {
        return value.type() == ValueType.LIST ? value : NodeValue.EMPTY;
    }

    @Override
    public void tick() {
        logic();
    }
    public void setExtraData(CompoundTag tag) {
        size = tag.getInt("size");
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putInt("size", size);
        return tag;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        width = Math.max(54, font.width(Component.translatable("aeronet.node.array_unpack")) + 4);
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.array_unpack"), screenX + 2, screenY + 2, 0xFFFFFF);
        logic();
    }
    private void logic()
    {
        List<NodeValue> values = isInputPortConnected(0) && inputArray.getFirst().type() == ValueType.LIST ? inputArray.getFirst().asList() : List.of();
        size = values.size();
        if (size <= 0)
        {
            setInputsOutputs(1, 1);
            height = 12 + PORT_SPACING;
        }
        else
        {
            setInputsOutputs(1, size);
            height = 12 + size * PORT_SPACING;
        }
        outputArray = values;
    }
    @Override public NodeType getType() { return NodeType.ARRAY_UNPACK; }
}