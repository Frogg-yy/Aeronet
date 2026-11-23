package com.froggyy277353.aeronet.widgets.strings;

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


public class StringNode extends Node {
    private boolean wasFocused = false;
    private TextField stringField = new TextField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private String str;
    public StringNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                            List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 1, 46, PORT_START_OFFSET + PORT_SPACING, inputArray, outputArray);
        red = 0.388F;
        green = 0.400F;
        blue = 0.945F;
    }
    @Override public ValueType getInputType(int port) { return ValueType.STRING; }
    @Override public ValueType getOutputType(int port) { return ValueType.STRING; }
    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        setOutputValue(0, NodeValue.of(str));
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        width = Math.max(font.width(Component.translatable("aeronet.node.string").getString()),  13+ 8 * font.width("W"));
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.string"), screenX + 2, screenY + 2, 0xFFFFFF);

        stringField.setX(screenX+2);
        stringField.setY(screenY+PORT_START_OFFSET-1);
        stringField.render(graphics, mouseX, mouseY, partialTick, font);
        str = stringField.getText();
        logic();
        boolean isFocusedNow = stringField.isFocused();
        if (wasFocused && !isFocusedNow) {
            changed = true;
        }
        wasFocused = isFocusedNow;
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("str", str);
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        this.str = tag.getString("str");
        stringField.setText(str);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        stringField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        stringField.mouseReleased(mouseX, mouseY, button);
        return false;
    }
    @Override
    public boolean hasFocusedInput() {
        return stringField.isFocused();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        stringField.keyPressed(keyCode,scanCode,modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        stringField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override public NodeType getType() { return NodeType.STRING; }
}
