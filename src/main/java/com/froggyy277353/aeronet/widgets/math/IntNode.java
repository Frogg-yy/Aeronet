package com.froggyy277353.aeronet.widgets.math;

import com.froggyy277353.aeronet.client.gui.NumberField;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;



public class IntNode extends Node {
    private float value;
    private boolean wasFocused = false;
    private NumberField valueField = new NumberField(0, 0, 0, 8, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
    public IntNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 1, 37+9, 24, inputArray, outputArray);
        red = 0.851F;
        green = 0.169F;
        blue = 0.447F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        width = Math.max(font.width(Component.translatable("aeronet.node.int")) + 4, font.width("W") * 5 + 12);
        graphics.drawString(font, Component.translatable("aeronet.node.int"), screenX+2, screenY+2, 0xFFFFFF);
        valueField.setX(screenX+2);
        valueField.setY(screenY+11);
        valueField.render(graphics, mouseX, mouseY, partialTick, font);
        value = valueField.getValue();
        boolean isFocusedNow = valueField.isFocused();
        if (wasFocused && !isFocusedNow) {
            changed = true;
        }
        wasFocused = isFocusedNow;
    }

    @Override
    public void tick()
    {
        super.tick();
        outputArray.set(0, NodeValue.of(value));
    }

    public void setExtraData(CompoundTag tag) {
        this.value = tag.getFloat("value");
        valueField.setValue(value);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putFloat("value", value);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        valueField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        valueField.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY, screenX, screenY);
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        valueField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        valueField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public NodeType getType() {
        return NodeType.INT;
    }
    @Override
    public boolean hasFocusedInput() {
        return valueField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        valueField.setFocused(false);
    }
}
