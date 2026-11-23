package com.froggyy277353.aeronet.widgets.logical;

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

public class LessNode extends Node {
    private float valueTrue;
    private boolean valueTrueWasFocused;
    private float valueFalse;
    private boolean valueFalseWasFocused;
    private float a;
    private boolean aWasFocused2;
    private float b;
    private boolean bWasFocused3;
    private NumberField valueTrueField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField valueFalseField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField aField = new NumberField(0, 0, 5, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField bField = new NumberField(0, 0, 5, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    public LessNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 4, 1, width, 60, inputArray, outputArray);
        red = 0.902F;
        green = 0.761F;
        blue = 0.161F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int width, int height)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, width, height);
        this.width = Math.max(Math.max(font.width("aeronet.node.less") + 4, font.width("WWWWWWWWWW")), 9 * font.width("W"));
        graphics.drawString(font, Component.translatable("aeronet.node.less"), screenX+2, screenY+2, 0xFFFFFF);

        valueTrueField.setX(screenX + font.width(Component.translatable("aeronet.node.logical.true")) + 4);
        valueTrueField.setY(screenY+11);

        valueFalseField.setX(screenX + font.width(Component.translatable("aeronet.node.logical.false")) + 4);
        valueFalseField.setY(screenY+22);

        aField.setX(screenX + font.width(Component.translatable("aeronet.node.logical.a")) + 4);
        aField.setY(screenY+33);

        bField.setX(screenX + font.width(Component.translatable("aeronet.node.logical.b")) + 4);
        bField.setY(screenY+44);
        if (!isInputPortConnected(0))
        {
            valueTrueField.render(graphics, mouseX, mouseY, partialTick, font);
            valueTrue = valueTrueField.getValue();
        }
        if (!isInputPortConnected(1))
        {
            valueFalseField.render(graphics, mouseX, mouseY, partialTick, font);
            valueFalse = valueFalseField.getValue();
        }
        if (!isInputPortConnected(2))
        {
            aField.render(graphics, mouseX, mouseY, partialTick, font);
            a = aField.getValue();
        }
        if (!isInputPortConnected(3))
        {
            bField.render(graphics, mouseX, mouseY, partialTick, font);
            b = bField.getValue();
        }

        graphics.drawString(font, Component.translatable("aeronet.node.logical.true"), screenX+3, screenY+13, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.false"), screenX+3, screenY+24, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.a"), screenX+3, screenY+35, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.b"), screenX+3, screenY+46, 0xFFFFFF);

        if (valueTrueWasFocused && !valueTrueField.isFocused()) {
            changed = true;
        }
        valueTrueWasFocused = valueTrueField.isFocused();

        if (valueFalseWasFocused && !valueFalseField.isFocused()) {
            changed = true;
        }
        valueFalseWasFocused = valueFalseField.isFocused();

        if (aWasFocused2 && !aField.isFocused()) {
            changed = true;
        }
        aWasFocused2 = aField.isFocused();

        if (bWasFocused3 && !bField.isFocused()) {
            changed = true;
        }
        bWasFocused3 = bField.isFocused();

        logic();
    }

    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("value", valueTrue);
        tag.putFloat("value1", valueFalse);
        tag.putFloat("value2", a);
        tag.putFloat("value3", b);
        return tag;
    }
    public void setExtraData(CompoundTag tag) {
        this.valueTrue = tag.getFloat("value");
        this.valueFalse = tag.getFloat("value1");
        this.a = tag.getFloat("value2");
        this.b = tag.getFloat("value3");
        valueTrueField.setValue(valueTrue);
        valueFalseField.setValue(valueFalse);
        aField.setValue(a);
        bField.setValue(b);
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        float lvalueTrue;
        float lvalueFalse;
        float la;
        float lb;
        if (isInputPortConnected(0))
        {
            lvalueTrue = inputArray.getFirst().asFloat();
        }
        else
        {
            lvalueTrue = valueTrue;
        }
        if (isInputPortConnected(1))
        {
            lvalueFalse = inputArray.get(1).asFloat();
        }
        else
        {
            lvalueFalse = valueFalse;
        }
        if (isInputPortConnected(2))
        {
            la = inputArray.get(2).asFloat();
        }
        else
        {
            la = a;
        }
        if (isInputPortConnected(3))
        {
            lb = inputArray.get(3).asFloat();
        }
        else
        {
            lb = b;
        }
        if (la < lb)
        {
            outputArray.set(0, NodeValue.of(lvalueTrue));
        }
        else
        {
            outputArray.set(0, NodeValue.of(lvalueFalse));
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        valueTrueField.mouseClicked(mouseX, mouseY, button);
        valueFalseField.mouseClicked(mouseX, mouseY, button);
        aField.mouseClicked(mouseX, mouseY, button);
        bField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        valueTrueField.mouseReleased(mouseX, mouseY, button);
        valueFalseField.mouseReleased(mouseX, mouseY, button);
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
        valueTrueField.keyPressed(keyCode, scanCode, modifiers);
        valueFalseField.keyPressed(keyCode, scanCode, modifiers);
        aField.keyPressed(keyCode, scanCode, modifiers);
        bField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        valueTrueField.charTyped(codePoint, modifiers);
        valueFalseField.charTyped(codePoint, modifiers);
        aField.charTyped(codePoint, modifiers);
        bField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public NodeType getType() {
        return NodeType.LESS;
    }
    @Override
    public boolean hasFocusedInput() {
        return valueTrueField.isFocused() || valueFalseField.isFocused() || aField.isFocused() || bField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        valueTrueField.setFocused(false);
        valueFalseField.setFocused(false);
        aField.setFocused(false);
        bField.setFocused(false);
    }
}
