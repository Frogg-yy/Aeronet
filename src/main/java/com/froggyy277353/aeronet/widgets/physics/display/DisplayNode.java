package com.froggyy277353.aeronet.widgets.physics.display;

import com.froggyy277353.aeronet.client.gui.TextField;
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

public class DisplayNode extends Node {
    public static final int PIXEL_COUNT = 64;
    private final TextField linkNameField = new TextField(0, 0, 13, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private String linkName = "";
    private boolean wasFocused;

    public DisplayNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                       List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 0, 46, 36, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;
    }

    @Override
    public ValueType getInputType(int port) { return ValueType.LIST; }

    @Override
    public NodeValue convertInputValue(int port, NodeValue value) {
        return value.type() == ValueType.LIST ? value : NodeValue.EMPTY;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        width = Math.max(font.width(Component.translatable("block.aeronet.display")) + 4,
                linkNameField.getPreferredWidth(font) + 6);
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("block.aeronet.display"), screenX + 2, screenY + 2, 0xFFFFFF);
        linkNameField.setX(screenX + 3);
        linkNameField.setY(screenY + 11);
        linkNameField.render(graphics, mouseX, mouseY, partialTick, font);
        linkName = linkNameField.getText();
        boolean focused = linkNameField.isFocused();
        if (wasFocused && !focused) changed = true;
        wasFocused = focused;
    }

    public String getLinkName() { return linkName; }

    public Canvas.Data getCanvas() {
        if (!isInputPortConnected(0)
                || inputArray.isEmpty()
                || inputArray.getFirst().type() != ValueType.LIST) {
            return new Canvas.Data(8, 8);
        }

        return Canvas.from(inputArray.getFirst());
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("linkName", linkName);
        return tag;
    }
    @Override
    public boolean acceptsInputType(int port, ValueType type) {
        return type == getInputType(port);
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        linkName = tag.getString("linkName");
        linkNameField.setText(linkName);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        linkNameField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        linkNameField.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        linkNameField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        linkNameField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override public NodeType getType() { return NodeType.DISPLAY; }
    @Override public boolean hasFocusedInput() { return linkNameField.isFocused(); }
    @Override public void clearInputFocus() { linkNameField.setFocused(false); }
}