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

public class DivideNode extends Node {
    private  NumberField aField = new NumberField(0, 0, 0, 8, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
    private NumberField bField = new NumberField(0, 0, 0, 8, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
    private boolean aWasFocused = false;
    private boolean bWasFocused = false;
    private float a;
    private float b;
    public DivideNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 2, 1, width, 36, inputArray, outputArray);
        red = 0.247F;
        green = 0.663F;
        blue = 0.961F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int width, int height) {
        super.render(graphics, font, mouseX, mouseY, partialTick, width, height);
        this.width = Math.max(font.width("aeronet.node.divide") + 4, 9 * font.width("W"));
        graphics.drawString(font, Component.translatable("aeronet.node.divide"), screenX+2, screenY+2, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.a"), screenX+3, screenY+13, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.b"), screenX+3, screenY+24, 0xFFFFFF);
        aField.setX(screenX+3+font.width(Component.translatable("aeronet.node.logical.a")));
        bField.setX(screenX+3+font.width(Component.translatable("aeronet.node.logical.b")));
        aField.setY(screenY+11);
        bField.setY(screenY+22);
        if (!isInputPortConnected(0))
        {
            aField.render(graphics, mouseX, mouseY, partialTick, font);
        }
        if (!isInputPortConnected(1))
        {
            bField.render(graphics, mouseX, mouseY, partialTick, font);
        }
        boolean minFocusedNow = aField.isFocused();
        if (aWasFocused && !minFocusedNow) {
            changed = true;
        }
        aWasFocused = minFocusedNow;

        boolean maxFocusedNow = bField.isFocused();
        if (bWasFocused && !maxFocusedNow) {
            changed = true;
        }
        bWasFocused = maxFocusedNow;
        logic();
    }
    @Override
    public void tick() {
        super.tick();
        logic();
    }
    private void logic()
    {
        float la;
        float lb;
        if (isInputPortConnected(0))
        {
            la = inputArray.getFirst().asFloat();
        }
        else
        {
            la = aField.getValue();
        }
        if (isInputPortConnected(1))
        {
            lb = inputArray.get(1).asFloat();
        }
        else
        {
            lb = bField.getValue();
        }
        outputArray.set(0, NodeValue.of(lb != 0.0F ? la / lb : 0.0F));

    }
    public void setExtraData(CompoundTag tag) {
        this.a = tag.getFloat("value1");
        this.b = tag.getFloat("value2");
        aField.setValue(a);
        bField.setValue(b);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putFloat("value1", aField.getValue());
        tag.putFloat("value2", bField.getValue());
        return tag;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        aField.mouseClicked(mouseX, mouseY, button);
        bField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        aField.mouseReleased(mouseX, mouseY, button);
        bField.mouseReleased(mouseX, mouseY, button);
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
        aField.keyPressed(keyCode, scanCode, modifiers);
        bField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        aField.charTyped(codePoint, modifiers);
        bField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public NodeType getType() {
        return NodeType.DIVIDE;
    }
    @Override
    public boolean hasFocusedInput() {
        return aField.isFocused() || bField.isFocused();
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.FLOAT;
    }
    @Override
    public void clearInputFocus() {
        aField.setFocused(false);
        bField.setFocused(false);
    }
}
