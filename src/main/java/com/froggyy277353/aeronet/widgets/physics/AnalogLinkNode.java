package com.froggyy277353.aeronet.widgets.physics;

import com.froggyy277353.aeronet.client.gui.Button;
import com.froggyy277353.aeronet.client.gui.TextField;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;



public class AnalogLinkNode extends Node {
    private String linkName = "";
    private boolean wasFocused = false;
    private boolean sender = false;
    private float value;
    private boolean lastSender;
    private TextField linkNameField = new TextField(0, 0, 8, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private Button modeToggle;
    public AnalogLinkNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 1, 37+9, 36, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;
        lastSender = sender;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        if (font.width(Component.translatable("aeronet.node.analog_link")) + 4 < font.width("WWWWWWWWWW"))
        {
            this.width = font.width("WWWWWWWWWW");
        }
        else
        {
            width = font.width(Component.translatable("aeronet.node.analog_link")) + 4;
        }
        graphics.drawString(font, Component.translatable("aeronet.node.analog_link"), screenX+2, screenY+2, 0xFFFFFF);
        linkNameField.setX(screenX+1);
        linkNameField.setY(screenY+11);
        linkNameField.render(graphics, mouseX, mouseY, partialTick, font);
        if (modeToggle == null) {
            modeToggle = new Button(0, 0, Math.max(font.width(Component.translatable("aeronet.network.sender")) + 2,
                    font.width(Component.translatable("aeronet.network.receiver")) + 2), 12, true,
                    Component.translatable("aeronet.network.sender").getString(),
                    Component.translatable("aeronet.network.receiver").getString(), 0xFF282828, 0xFFFFFF, 0xFF699684);
            modeToggle.setState(sender);
        }
        modeToggle.setX(screenX+3);
        modeToggle.setY(screenY+22);
        modeToggle.render(graphics, mouseX, mouseY, partialTick, font);
        sender = modeToggle.isPressed();
        boolean isFocusedNow = linkNameField.isFocused();
        linkName = linkNameField.getText();
        if (sender != lastSender) {
            modeChanged = true;
            lastSender = sender;
        }

        if (sender)
        {
            setInputsOutputs(1, 0);
        }
        else
        {
            setInputsOutputs(0, 1);
        }
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
        if (isInputPortConnected(0) && !inputArray.isEmpty())
        {
            value = inputArray.getFirst().asFloat();
        }
        else
        {
            value = 0;
        }
    }
    public void receiveSignal(float signal) {
            if (outputArray != null && numOutputs > 0)
            {
                setOutputValue(0, NodeValue.of(signal));
            }
    }

    public boolean isSender()
    {
        return sender;
    }
    public String getLinkName(){return linkName;}
    public void setExtraData(CompoundTag tag) {
        this.linkName = tag.getString("linkName");
        this.sender = tag.getBoolean("sender");
        setInputsOutputs(sender ? 1 : 0, sender ? 0 : 1);
        lastSender = sender;
        if (modeToggle != null) {
            modeToggle.setState(sender);
        }
        linkNameField.setText(linkName);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putString("linkName", linkName);
        tag.putBoolean("sender", sender);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        linkNameField.mouseClicked(mouseX, mouseY, button);
        if (modeToggle != null) modeToggle.mouseClicked(mouseX, mouseY, button);
        return result;
    }
    public float getValue(){return value;}
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (modeToggle != null) modeToggle.mouseReleased(mouseX, mouseY, button);
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
    public NodeType getType() {
        return NodeType.ANALOG_LINK;
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
