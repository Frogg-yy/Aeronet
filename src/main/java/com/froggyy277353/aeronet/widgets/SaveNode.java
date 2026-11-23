package com.froggyy277353.aeronet.widgets;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SaveNode extends Node {
    private int size = 0;
    public SaveNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                         List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 1, 48, 12 + PORT_SPACING, inputArray, outputArray);
        red = 0.392F; green = 0.455F; blue = 0.545F;
    }

    @Override public ValueType getInputType(int port) { return ValueType.BOOLEAN; }
    @Override public ValueType getOutputType(int port) { return ValueType.LIST; }

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
        width = Math.max(48, font.width(Component.translatable("aeronet.node.array_pack")) + 4);
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.array_pack"), screenX + 2, screenY + 2, 0xFFFFFF);
        logic();
    }

    private void logic()
    {
        size = 1+getLastInputPortConnected();
        setInputsOutputs(size, 1);
        height = 12 + size * PORT_SPACING;
        ArrayList<NodeValue> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            boolean value = isInputPortConnected(i) && !inputArray.get(i).isEmpty() && inputArray.get(i).asBoolean();
            result.add(NodeValue.of(value));
        }
        outputArray.set(0, NodeValue.of(result));
    }

    @Override public NodeType getType() { return NodeType.SAVE; }
}