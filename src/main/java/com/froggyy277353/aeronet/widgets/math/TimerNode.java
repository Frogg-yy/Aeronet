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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class TimerNode extends Node {
    private static final int MAX_DELAY = 12000;

    private final NumberField delayField = new NumberField(0, 0, 0, 8, 12,
            0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private final Deque<NodeValue> pendingValues = new ArrayDeque<>();
    private boolean delayWasFocused;
    private int previousDelay = -1;

    public TimerNode(UUID id, int x, int y, int inputs, int outputs,
                     int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 2, 1, width, 36, inputArray, outputArray);
        red = 0.902F;
        green = 0.494F;
        blue = 0.133F;
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.FLOAT;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                       float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node.timer");
        Component value = Component.translatable("aeronet.node.value");
        Component delay = Component.translatable("aeronet.node.delay");
        width = Math.max(font.width(title) + 4,
                font.width(delay) + delayField.getPreferredWidth(font) + 5);
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);
        graphics.drawString(font, value, screenX + 3, screenY + 13, 0xFFFFFF);
        graphics.drawString(font, delay, screenX + 3, screenY + 24, 0xFFFFFF);
        delayField.setX(screenX + 3 + font.width(delay));
        delayField.setY(screenY + 22);
        if (!isInputPortConnected(1)) delayField.render(graphics, mouseX, mouseY, partialTick, font);

        boolean focused = delayField.isFocused();
        if (delayWasFocused && !focused) changed = true;
        delayWasFocused = focused;
    }

    @Override
    public void tick() {
        super.tick();
        int delay = currentDelay();
        NodeValue input = inputArray.getFirst();

        if (delay != previousDelay) {
            pendingValues.clear();
            previousDelay = delay;
        }
        if (delay == 0) {
            outputArray.set(0, input);
            return;
        }

        pendingValues.addLast(input);
        if (pendingValues.size() > delay) {
            outputArray.set(0, pendingValues.removeFirst());
        }
    }

    private int currentDelay() {
        float value = isInputPortConnected(1) ? inputArray.get(1).asFloat() : delayField.getValue();
        if (!Float.isFinite(value)) return 0;
        return Math.clamp(Math.round(value), 0, MAX_DELAY);
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("delay", delayField.getValue());
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        delayField.setValue(tag.getFloat("delay"));
        previousDelay = -1;
        pendingValues.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        delayField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return delayField.mouseReleased(mouseX, mouseY, button);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return delayField.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override public boolean charTyped(char codePoint, int modifiers) {
        return delayField.charTyped(codePoint, modifiers);
    }
    @Override public NodeType getType() { return NodeType.TIMER; }
    @Override public boolean hasFocusedInput() { return delayField.isFocused(); }
    @Override public void clearInputFocus() { delayField.setFocused(false); }
}
