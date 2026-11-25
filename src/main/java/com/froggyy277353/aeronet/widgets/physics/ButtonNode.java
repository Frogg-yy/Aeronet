package com.froggyy277353.aeronet.widgets.physics;

import com.froggyy277353.aeronet.client.gui.Button;
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


public class ButtonNode extends Node {
    private String linkName = "";
    private boolean wasFocused = false;
    private TextField linkNameField = new TextField(0, 0, 8, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    public ButtonNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 1, 37+9, PORT_SPACING+PORT_SPACING, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        this.width = Math.max(font.width(Component.translatable("aeronet.node.button")) + 4, font.width("WWWWWWWWWW"));
        graphics.drawString(font, Component.translatable("aeronet.node.button"), screenX+2, screenY+2, 0xFFFFFF);
        linkNameField.setX(screenX+1);
        linkNameField.setY(screenY+11);
        linkNameField.render(graphics, mouseX, mouseY, partialTick, font);
        boolean isFocusedNow = linkNameField.isFocused();
        linkName = linkNameField.getText();
        if (wasFocused && !isFocusedNow) {
            changed = true;
        }
        wasFocused = isFocusedNow;
        logic();
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {

    }
    public void receiveSignal(boolean state) {
            if (outputArray != null && numOutputs > 0)
            {
                setOutputValue(0, NodeValue.of(state));
            }
    }

    public String getLinkName(){return linkName;}
    public void setExtraData(CompoundTag tag) {
        this.linkName = tag.getString("linkName");
        linkNameField.setText(linkName);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putString("linkName", linkName);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        linkNameField.mouseClicked(mouseX, mouseY, button);
        return result;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        linkNameField.mouseReleased(mouseX, mouseY, button);
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
        linkNameField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        linkNameField.charTyped(codePoint, modifiers);
        return false;
    }
    @Override
    public ValueType getInputType(int port) {
        return ValueType.NULL;
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.BOOLEAN;
    }

    @Override
    public NodeType getType() {
        return NodeType.BUTTON;
    }
    @Override
    public boolean hasFocusedInput() {
        return linkNameField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        linkNameField.setFocused(false);
    }
}
