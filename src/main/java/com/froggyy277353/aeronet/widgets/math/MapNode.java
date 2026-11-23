package com.froggyy277353.aeronet.widgets.math;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
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

public class MapNode extends Node {
    private float inMin;
    private float inMax;
    private float outMin;
    private float outMax;
    private boolean inMinWasFocused;
    private boolean inMaxWasFocused;
    private boolean outMinWasFocused;
    private boolean outMaxWasFocused;
    private List<String> strings = List.of(Component.translatable("aeronet.node.value").getString(), Component.translatable("aeronet.node.from_min").getString(), Component.translatable("aeronet.node.from_max").getString(), Component.translatable("aeronet.node.to_min").getString(), Component.translatable("aeronet.node.to_max").getString());
    private NumberField inMinField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField inMaxField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField outMaxField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField outMinField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    public MapNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 5, 1, width, 72, inputArray, outputArray);
        red = 0.247F;
        green = 0.663F;
        blue = 0.961F;
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.FLOAT;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int width, int height)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, width, height);
        this.width = Math.max(font.width(Component.translatable("aeronet.node.map")) + 4, 9 * font.width("W"));
        graphics.drawString(font, Component.translatable("aeronet.node.map"), screenX+2, screenY+2, 0xFFFFFF);
        for (int i = 0; i < strings.size(); i++)
        {
            graphics.drawString(font, strings.get(i), screenX+3, screenY+13+i*11, 0xFFFFFF);
        }
        inMinField.setX(screenX+3+font.width(Component.translatable("aeronet.node.from_min").getString()));
        inMaxField.setX(screenX+3+font.width(Component.translatable("aeronet.node.from_max").getString()));
        outMinField.setX(screenX+3+font.width(Component.translatable("aeronet.node.to_min").getString()));
        outMaxField.setX(screenX+3+font.width(Component.translatable("aeronet.node.to_max").getString()));
        inMinField.setY(screenY+22);
        inMaxField.setY(screenY+33);
        outMinField.setY(screenY+44);
        outMaxField.setY(screenY+55);
        if (!isInputPortConnected(1))
        {
            inMinField.render(graphics, mouseX, mouseY, partialTick, font);
            inMin = inMinField.getValue();
        }
        else
        {
            inMin = 0;
        }
        if (!isInputPortConnected(2))
        {
            inMaxField.render(graphics, mouseX, mouseY, partialTick, font);
            inMax = inMaxField.getValue();
        }
        else
        {
            inMax = 0;
        }
        if (!isInputPortConnected(3))
        {
            outMinField.render(graphics, mouseX, mouseY, partialTick, font);
            outMin = outMinField.getValue();
        }
        else
        {
            outMin = 0;
        }
        if (!isInputPortConnected(4))
        {
            outMaxField.render(graphics, mouseX, mouseY, partialTick, font);
            outMax = outMaxField.getValue();
        }
        else
        {
            outMax = 0;
        }

        boolean inMinFocusedNow = inMinField.isFocused();
        if (inMinWasFocused && !inMinFocusedNow) {
            changed = true;
        }
        inMinWasFocused = inMinFocusedNow;

        boolean inMaxFocusedNow = inMaxField.isFocused();
        if (inMaxWasFocused && !inMaxFocusedNow) {
            changed = true;
        }
        inMaxWasFocused = inMaxFocusedNow;

        boolean outMinFocusedNow = outMinField.isFocused();
        if (outMinWasFocused && !outMinFocusedNow) {
            changed = true;
        }
        outMinWasFocused = outMinFocusedNow;

        boolean outMaxFocusedNow = outMaxField.isFocused();
        if (outMaxWasFocused && !outMaxFocusedNow) {
            changed = true;
        }
        outMaxWasFocused = outMaxFocusedNow;
        logic();
    }

    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("inMin", inMin);
        tag.putFloat("inMax", inMax);
        tag.putFloat("outMin", outMin);
        tag.putFloat("outMax", outMax);
        return tag;
    }
    public void setExtraData(CompoundTag tag) {
        this.inMin = tag.getFloat("inMin");
        this.inMax = tag.getFloat("inMax");
        this.outMin = tag.getFloat("outMin");
        this.outMax = tag.getFloat("outMax");
        inMinField.setValue(inMin);
        inMaxField.setValue(inMax);
        outMinField.setValue(outMin);
        outMaxField.setValue(outMax);
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        float lInMin;
        float lInMax;
        float lOutMin;
        float lOutMax;

        if (isInputPortConnected(1))
        {
            lInMin = inputArray.get(1).asFloat();
        }
        else
        {
            lInMin = inMin;
        }
        if (isInputPortConnected(2))
        {
            lInMax = inputArray.get(2).asFloat();
        }
        else
        {
            lInMax = inMax;
        }
        if (isInputPortConnected(3))
        {
            lOutMin = inputArray.get(3).asFloat();
        }
        else
        {
            lOutMin = outMin;
        }
        if (isInputPortConnected(4))
        {
            lOutMax = inputArray.get(4).asFloat();
        }
        else
        {
            lOutMax = outMax;
        }
        outputArray.set(0, NodeValue.of(GuiUtils.map(inputArray.getFirst().asFloat(), lInMin, lInMax, lOutMin, lOutMax)));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        inMinField.mouseClicked(mouseX, mouseY, button);
        inMaxField.mouseClicked(mouseX, mouseY, button);
        outMinField.mouseClicked(mouseX, mouseY, button);
        outMaxField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        inMinField.mouseReleased(mouseX, mouseY, button);
        inMaxField.mouseReleased(mouseX, mouseY, button);
        outMinField.mouseReleased(mouseX, mouseY, button);
        outMaxField.mouseReleased(mouseX, mouseY, button);
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
        inMinField.keyPressed(keyCode, scanCode, modifiers);
        inMaxField.keyPressed(keyCode, scanCode, modifiers);
        outMinField.keyPressed(keyCode, scanCode, modifiers);
        outMaxField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        inMinField.charTyped(codePoint, modifiers);
        inMaxField.charTyped(codePoint, modifiers);
        outMinField.charTyped(codePoint, modifiers);
        outMaxField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public NodeType getType() {
        return NodeType.MAP;
    }
    @Override
    public boolean hasFocusedInput() {
        return inMinField.isFocused() || inMaxField.isFocused() || outMaxField.isFocused() || outMinField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        inMinField.setFocused(false);
        inMaxField.setFocused(false);
        outMaxField.setFocused(false);
        outMinField.setFocused(false);
    }
}
