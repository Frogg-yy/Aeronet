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

public class ImpulseNode extends Node {
    private int tickCounter;
    private int oldTickCounter;
    private float a;
    private float b;
    private float aDelay;
    private float bDelay;
    private boolean aWasFocused;
    private boolean bWasFocused;
    private boolean aDelayWasFocused;
    private boolean bDelayWasFocused;
    private boolean resetTimer;
    private NumberField aField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField bField = new NumberField(0, 0, 0, 8, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField aDelayField = new NumberField(0, 0, 5, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private NumberField bDelayField = new NumberField(0, 0, 5, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    public ImpulseNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 4, 1, 37+18, 60, inputArray, outputArray);
        red = 0.902F;
        green = 0.494F;
        blue = 0.133F;
    }

    @Override
    public ValueType getOutputType(int port) {
        return ValueType.FLOAT;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        this.width = Math.max(font.width("aeronet.node.impulse") + 4, 9 * font.width("W"));
        graphics.drawString(font, Component.translatable("aeronet.node.impulse"), screenX+2, screenY+2, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.a"), screenX+3, screenY+13, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.logical.b"), screenX+3, screenY+24, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.delay_a"), screenX+3, screenY+35, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("aeronet.node.delay_b"), screenX+3, screenY+46, 0xFFFFFF);
        aField.setX(screenX+4+font.width(Component.translatable("aeronet.node.logical.a").getString()));
        bField.setX(screenX+4+font.width(Component.translatable("aeronet.node.logical.b").getString()));
        aDelayField.setX(screenX+4+font.width(Component.translatable("aeronet.node.delay_a").getString()));
        bDelayField.setX(screenX+4+font.width(Component.translatable("aeronet.node.delay_b").getString()));
        aField.setY(screenY+11);
        bField.setY(screenY+22);
        aDelayField.setY(screenY+33);
        bDelayField.setY(screenY+44);

        if (!isInputPortConnected(0))
        {
            aField.render(graphics, mouseX, mouseY, partialTick, font);
            a = aField.getValue();
        }
        if (!isInputPortConnected(1))
        {
            bField.render(graphics, mouseX, mouseY, partialTick, font);
            b = bField.getValue();
        }
        if (!isInputPortConnected(2))
        {
            aDelayField.render(graphics, mouseX, mouseY, partialTick, font);
            aDelay = aDelayField.getValue();
        }
        if (!isInputPortConnected(3))
        {
            bDelayField.render(graphics, mouseX, mouseY, partialTick, font);
            bDelay = bDelayField.getValue();
        }
        logic();
        if (aWasFocused && !aField.isFocused()) {
            changed = true;
            resetTimer = true;
        }
        aWasFocused = aField.isFocused();
        if (bWasFocused && !bField.isFocused()) {
            changed = true;
            resetTimer = true;
        }
        bWasFocused = bField.isFocused();
        if (aDelayWasFocused && !aDelayField.isFocused()) {
            changed = true;
            resetTimer = true;
        }
        aDelayWasFocused = aDelayField.isFocused();
        if (bDelayWasFocused && !bDelayField.isFocused()) {
            changed = true;
            resetTimer = true;
        }
        bDelayWasFocused = bDelayField.isFocused();
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    public void logic()
    {
        tickCounter++;
        float la;
        float lb;
        float laDelay;
        float lbDelay;
        if (isInputPortConnected(0))
        {
            la = inputArray.getFirst().asFloat();
        }
        else
        {
            la = a;
        }
        if (isInputPortConnected(1))
        {
            lb = inputArray.get(1).asFloat();
        }
        else
        {
            lb = b;
        }
        if (isInputPortConnected(2))
        {
            laDelay = inputArray.get(2).asFloat();
        }
        else
        {
            laDelay = aDelay;
        }
        if (isInputPortConnected(3))
        {
            lbDelay = inputArray.get(3).asFloat();
        }
        else
        {
            lbDelay = bDelay;
        }
        if (resetTimer)
        {
            oldTickCounter = tickCounter;
            resetTimer = false;
            outputArray.set(0, NodeValue.of(la));
        }

        int elapsed = tickCounter - oldTickCounter;

        if (elapsed < laDelay) {
            outputArray.set(0, NodeValue.of(la));
        } else if (elapsed < laDelay + lbDelay) {
            outputArray.set(0, NodeValue.of(lb));
        } else {
            oldTickCounter = tickCounter;
        }
    }

    public void setExtraData(CompoundTag tag) {
        tickCounter = tag.getInt("counter");
        a = tag.getFloat("value");
        b = tag.getFloat("value1");
        aDelay = tag.getFloat("delay");
        bDelay = tag.getFloat("valueDelay");
        aField.setValue(a);
        bField.setValue(b);
        aDelayField.setValue(aDelay);
        bDelayField.setValue(bDelay);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putInt("counter", tickCounter);
        tag.putFloat("value", a);
        tag.putFloat("value1", b);
        tag.putFloat("delay", aDelay);
        tag.putFloat("valueDelay", bDelay);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        aField.mouseClicked(mouseX, mouseY, button);
        bField.mouseClicked(mouseX, mouseY, button);
        aDelayField.mouseClicked(mouseX, mouseY, button);
        bDelayField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        aField.mouseReleased(mouseX, mouseY, button);
        bField.mouseReleased(mouseX, mouseY, button);
        aDelayField.mouseReleased(mouseX, mouseY, button);
        bDelayField.mouseReleased(mouseX, mouseY, button);
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
        aDelayField.keyPressed(keyCode, scanCode, modifiers);
        bDelayField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        aField.charTyped(codePoint, modifiers);
        bField.charTyped(codePoint, modifiers);
        aDelayField.charTyped(codePoint, modifiers);
        bDelayField.charTyped(codePoint, modifiers);
        return false;
    }


    @Override
    public NodeType getType() {
        return NodeType.IMPULSE;
    }

    @Override
    public boolean hasFocusedInput() {
        return aField.isFocused() || bField.isFocused() || aDelayField.isFocused() || bDelayField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        aField.setFocused(false);
        bField.setFocused(false);
        aDelayField.setFocused(false);
        bDelayField.setFocused(false);
    }
}
