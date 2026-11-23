package com.froggyy277353.aeronet.widgets.logical;

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

public class StringEqualsNode extends Node {
    private String a;
    private boolean aWasFocused2;
    private String b;
    private boolean bWasFocused3;
    private TextField aField = new TextField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private TextField bField = new TextField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    public StringEqualsNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 2, 1, width, PORT_START_OFFSET + 2 * PORT_SPACING, inputArray, outputArray);
        red = 0.902F;
        green = 0.761F;
        blue = 0.161F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int width, int height)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, width, height);
        this.width = Math.max(Math.max(font.width("aeronet.node.string_equals") + 4, font.width("WWWWWWWWWW")), 9 * font.width("W"));
        graphics.drawString(font, Component.translatable("aeronet.node.string_equals"), screenX+2, screenY+2, 0xFFFFFF);


        aField.setX(screenX + font.width(Component.translatable("aeronet.node.logical.a")) + 4);
        aField.setY(screenY+11);

        bField.setX(screenX + font.width(Component.translatable("aeronet.node.logical.b")) + 4);
        bField.setY(screenY+22);
        if (!isInputPortConnected(0))
        {
            aField.render(graphics, mouseX, mouseY, partialTick, font);
            a = aField.getText();
        }
        if (!isInputPortConnected(1))
        {
            bField.render(graphics, mouseX, mouseY, partialTick, font);
            b = bField.getText();
        }

        graphics.drawString(font, Component.translatable("aeronet.node.logical.a"), screenX+3, screenY+11, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.b"), screenX+3, screenY+22, 0xFFFFFF);

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

    @Override public ValueType getInputType(int port) { return ValueType.STRING; }
    @Override public ValueType getOutputType(int port) { return ValueType.BOOLEAN; }


    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("value2", a);
        tag.putString("value3", b);
        return tag;
    }
    public void setExtraData(CompoundTag tag) {
        this.a = tag.getString("value2");
        this.b = tag.getString("value3");
        aField.setText(a);
        bField.setText(b);
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        if (isInputPortConnected(0))
        {
            a = inputArray.getFirst().asString();
        }
        if (isInputPortConnected(1))
        {
            b = inputArray.get(1).asString();
        }
        if (a.equals(b) && !a.isEmpty() && !b.isEmpty())
        {
            outputArray.set(0, NodeValue.of(true));
        }
        else
        {
            outputArray.set(0, NodeValue.of(false));
        }
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
        return NodeType.STRING_EQUALS;
    }
    @Override
    public boolean hasFocusedInput() {
        return aField.isFocused() || bField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        aField.setFocused(false);
        bField.setFocused(false);
    }
}
