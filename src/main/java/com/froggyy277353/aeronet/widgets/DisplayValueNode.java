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

public class DisplayValueNode extends Node {

    public DisplayValueNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                            List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 0, 46, 36, inputArray, outputArray);
        red = 0.608F;
        green = 0.349F;
        blue = 0.714F;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        if (inputArray.getFirst().value() != null)
        {
            width = Math.max(font.width(Component.translatable("aeronet.node.display_value")) + 4, font.width(inputArray.getFirst().value().toString())+4);
        }
        else
        {
            width = font.width(Component.translatable("aeronet.node.display_value")) + 4;
        }
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.display_value"), screenX + 2, screenY + 2, 0xFFFFFF);
        if (inputArray.getFirst().value() != null)
        {
            graphics.drawString(font, inputArray.getFirst().value().toString(), screenX+3, screenY+13, 0xFFFFFF);
        }
        else
        {
            graphics.drawString(font, "null", screenX+3, screenY+13, 0xFFFFFF);
        }
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public ValueType getInputType(int port) {
        return ValueType.FLOAT;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override public NodeType getType() { return NodeType.DISPLAY_VALUE; }
}