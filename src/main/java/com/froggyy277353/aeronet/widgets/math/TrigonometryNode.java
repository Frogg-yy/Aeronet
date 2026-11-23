package com.froggyy277353.aeronet.widgets.math;

import com.froggyy277353.aeronet.client.gui.NumberField;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class TrigonometryNode extends Node {
    private final NodeType type;
    private final NumberField valueField = new NumberField(0, 0, 0, 8, 12,
            0xFF454545, 0xFFFFFFFF, 0xFF646464);
    private boolean wasFocused;

    public TrigonometryNode(UUID id, int x, int y, int inputs, int outputs,
                            int width, int height, List<NodeValue> inputArray,
                            List<NodeValue> outputArray, NodeType type) {
        super(id, x, y, 1, 1, width, 25, inputArray, outputArray);
        this.type = type;
        red = 0.247F;
        green = 0.663F;
        blue = 0.961F;
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.FLOAT;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                       float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node." + type.name().toLowerCase());
        width = Math.max(font.width(title) + 4, valueField.getPreferredWidth(font) + 6);
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);
        valueField.setX(screenX + 3);
        valueField.setY(screenY + 11);
        if (!isInputPortConnected(0)) valueField.render(graphics, mouseX, mouseY, partialTick, font);
        boolean focused = valueField.isFocused();
        if (wasFocused && !focused) changed = true;
        wasFocused = focused;
        logic();
    }

    @Override
    public void tick() {
        super.tick();
        logic();
    }

    private void logic() {
        double value = isInputPortConnected(0) ? inputArray.getFirst().asFloat() : valueField.getValue();
        double result = switch (type) {
            case SIN -> Math.sin(Math.toRadians(value));
            case COS -> Math.cos(Math.toRadians(value));
            case TAN -> Math.tan(Math.toRadians(value));
            case ASIN -> Math.toDegrees(Math.asin(value));
            case ACOS -> Math.toDegrees(Math.acos(value));
            case ATAN -> Math.toDegrees(Math.atan(value));
            default -> 0;
        };
        outputArray.set(0, Double.isFinite(result) ? NodeValue.of((float) result) : NodeValue.EMPTY);
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("value", valueField.getValue());
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        valueField.setValue(tag.getFloat("value"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        valueField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return valueField.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return valueField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return valueField.charTyped(codePoint, modifiers);
    }

    @Override public NodeType getType() { return type; }
    @Override public boolean hasFocusedInput() { return valueField.isFocused(); }
    @Override public void clearInputFocus() { valueField.setFocused(false); }
}
