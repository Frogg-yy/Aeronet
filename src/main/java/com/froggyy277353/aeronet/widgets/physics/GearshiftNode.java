package com.froggyy277353.aeronet.widgets.physics;

import com.froggyy277353.aeronet.client.gui.CheckBox;
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

public class GearshiftNode extends Node {
    private String linkName = "";
    private boolean wasFocused = false;
    private boolean reversed;
    private boolean stop;
    private TextField linkNameField = new TextField(0, 0, 13, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private CheckBox reversedBox = new CheckBox(0, 0, 10);
    private CheckBox onBox = new CheckBox(0, 0, 10);
    public GearshiftNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 2, 0, 37+9, PORT_START_OFFSET + PORT_SPACING * 3, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        if (font.width(Component.translatable("block.aeronet.analog_gearshift")) + 4 < font.width("WWWWWWWWWW"))
        {
            this.width = font.width("WWWWWWWWWW");
        }
        else
        {
            width = font.width(Component.translatable("block.aeronet.analog_gearshift")) + 4;
        }
        graphics.drawString(font, Component.translatable("block.aeronet.analog_gearshift"), screenX+2, screenY, 0xFFFFFF);
        linkNameField.setX(screenX+3);
        linkNameField.setY(screenY+PORT_START_OFFSET+PORT_SPACING * 2);
        linkNameField.render(graphics, mouseX, mouseY, partialTick, font);
        if (!isInputPortConnected(0))
        {
            graphics.drawString(font, Component.translatable("aeronet.node.on"),screenX+4, screenY+PORT_START_OFFSET, 0xFFFFFF);
            onBox.setX(screenX+4+font.width(Component.translatable("aeronet.node.on").getString()));
            onBox.setY(screenY+PORT_START_OFFSET);
            onBox.render(graphics, mouseX, mouseY, partialTick, font);
            stop = onBox.isChecked();
        }
        if (!isInputPortConnected(1))
        {
            graphics.drawString(font, Component.translatable("aeronet.gearshift.reverse"),screenX+4, screenY+PORT_START_OFFSET+PORT_SPACING, 0xFFFFFF);
            reversedBox.setX(screenX+4+font.width(Component.translatable( "aeronet.gearshift.reverse").getString()));
            reversedBox.setY(screenY+PORT_START_OFFSET+PORT_SPACING);
            reversedBox.render(graphics, mouseX, mouseY, partialTick, font);
            reversed = reversedBox.isChecked();
        }
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

    @Override
    public ValueType getInputType(int port) {
        return ValueType.BOOLEAN;
    }

    public void logic() {
        if (isInputPortConnected(0) && !inputArray.isEmpty()) {
            stop = inputArray.getFirst().asBoolean();
        } else {
            stop = onBox.isChecked();
        }

        if (isInputPortConnected(1) && inputArray.size() > 1) {
            reversed = inputArray.get(1).asBoolean();
        } else {
            reversed = reversedBox.isChecked();
        }
    }

    public String getLinkName(){return linkName;}
    @Override
    public void setExtraData(CompoundTag tag) {
        this.linkName = tag.getString("linkName");
        this.stop = tag.getBoolean("stop");
        this.reversed = tag.getBoolean("reversed");

        linkNameField.setText(linkName);
        onBox.setChecked(stop);
        reversedBox.setChecked(reversed);
    }
    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("linkName", linkName);
        tag.putBoolean("stop", stop);
        tag.putBoolean("reversed", reversed);
        return tag;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        linkNameField.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            if (!isInputPortConnected(0) && onBox.mouseClicked(mouseX, mouseY, button)) {
                stop = onBox.isChecked();
                changed = true;
            }
            if (!isInputPortConnected(1) && reversedBox.mouseClicked(mouseX, mouseY, button)) {
                reversed = reversedBox.isChecked();
                changed = true;
            }
        }
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
    public boolean isStop()
    {
        return stop;
    }
    public boolean isReversed()
    {
        return reversed;
    }
    @Override
    public NodeType getType() {
        return NodeType.GEARSHIFT;
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