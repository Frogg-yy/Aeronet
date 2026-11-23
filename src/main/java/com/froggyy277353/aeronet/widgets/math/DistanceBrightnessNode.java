package com.froggyy277353.aeronet.widgets.math;

import com.froggyy277353.aeronet.client.gui.CheckBox;
import com.froggyy277353.aeronet.client.gui.NumberField;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.UUID;

public class DistanceBrightnessNode extends Node {
    private static final int FIELD_CHARS = 5;
    private static final int BOX_SIZE = 10;

    private static final int DIST_PORT = 0;
    private static final int MAX_DIST_PORT = 1;
    private static final int MAX_BRIGHT_PORT = 2;
    private static final int INVERT_PORT = 3;
    private static final int TOTAL_INPUTS = 4;

    private final NumberField[] fields = new NumberField[3];
    private final boolean[] wasFocused = new boolean[3];
    private final float[] params = new float[]{0, 32, 255};
    private final String[] fieldLabels = {"dist", "max", "val"};

    private boolean invert = false;
    private final CheckBox invertBox = new CheckBox(0, 0, BOX_SIZE);

    public DistanceBrightnessNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                                  List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, TOTAL_INPUTS, 1, width,
                PORT_START_OFFSET + TOTAL_INPUTS * PORT_SPACING, inputArray, outputArray);

        for (int i = 0; i < fields.length; i++) {
            fields[i] = new NumberField(0, 0, FIELD_CHARS, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
            fields[i].setValue(Math.round(params[i]));
        }

        red = 0.247F;
        green = 0.663F;
        blue = 0.961F;
    }

    @Override
    public ValueType getInputType(int port) {
        if (port == INVERT_PORT) {
            return ValueType.BOOLEAN;
        }
        if (port >= 0 && port < TOTAL_INPUTS) {
            return ValueType.FLOAT;
        }
        return ValueType.NULL;
    }

    @Override
    public boolean acceptsInputType(int port, ValueType type) {
        if (port == INVERT_PORT) {
            return type == ValueType.BOOLEAN || type == ValueType.INT || type == ValueType.FLOAT;
        }
        if (port >= 0 && port < TOTAL_INPUTS) {
            return type == ValueType.FLOAT || type == ValueType.INT;
        }
        return false;
    }

    @Override
    public ValueType getOutputType(int port) {
        return port == 0 ? ValueType.INT : ValueType.NULL;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node.distance_brightness");

        int labelWidth = font.width("inv");
        for (String label : fieldLabels) {
            labelWidth = Math.max(labelWidth, font.width(label));
        }
        int fieldWidth = font.width("W") * FIELD_CHARS + 8;
        this.width = Math.max(font.width(title) + 4, labelWidth + fieldWidth + 10);

        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);

        int widgetX = screenX + 3 + labelWidth + 4;

        for (int row = 0; row < TOTAL_INPUTS; row++) {
            int rowY = screenY + PORT_START_OFFSET - 3 + row * PORT_SPACING;

            if (row == INVERT_PORT) {
                graphics.drawString(font, "inv", screenX + 3, rowY + 2, 0xFFFFFF);
                if (!isInputPortConnected(INVERT_PORT)) {
                    invertBox.setX(widgetX);
                    invertBox.setY(rowY + 1);
                    invertBox.render(graphics, mouseX, mouseY, partialTick, font);
                    invert = invertBox.isChecked();
                }
            } else {
                graphics.drawString(font, fieldLabels[row], screenX + 3, rowY + 2, 0xFFFFFF);
                if (!isInputPortConnected(row)) {
                    fields[row].setX(widgetX);
                    fields[row].setY(rowY + 2);
                    fields[row].render(graphics, mouseX, mouseY, partialTick, font);
                    params[row] = fields[row].getValue();
                }
            }
        }

        for (int i = 0; i < fields.length; i++) {
            boolean focused = fields[i].isFocused();
            if (wasFocused[i] && !focused) {
                changed = true;
            }
            wasFocused[i] = focused;
        }

        logic();
    }

    @Override
    public void tick() {
        super.tick();
        logic();
    }

    private void logic() {
        float dist = readFloat(DIST_PORT, params[0]);
        float maxDist = Math.max(0.001f, readFloat(MAX_DIST_PORT, params[1]));
        int maxBrightness = Mth.clamp(Math.round(readFloat(MAX_BRIGHT_PORT, params[2])), 0, 255);

        boolean isInverted = invert;
        if (isInputPortConnected(INVERT_PORT) && INVERT_PORT < inputArray.size()) {
            NodeValue invVal = inputArray.get(INVERT_PORT);
            isInverted = invVal.type() == ValueType.BOOLEAN ? invVal.asBoolean() : invVal.asFloat() != 0.0f;
        }

        int brightness = 0;
        if (dist >= 0.0f && maxBrightness > 0) {
            float ratio = Mth.clamp(dist / maxDist, 0.0f, 1.0f);
            float factor = isInverted ? ratio : (1.0f - ratio);
            brightness = Math.round(factor * maxBrightness);
        }

        setOutputValue(0, NodeValue.of(brightness));
    }

    private float readFloat(int port, float fallback) {
        if (isInputPortConnected(port) && port < inputArray.size()) {
            return inputArray.get(port).asFloat();
        }
        return fallback;
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < params.length; i++) {
            tag.putFloat("p" + i, params[i]);
        }
        tag.putBoolean("invert", invert);
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        for (int i = 0; i < params.length; i++) {
            if (tag.contains("p" + i)) {
                params[i] = tag.getFloat("p" + i);
                fields[i].setValue(Math.round(params[i]));
            }
        }
        if (tag.contains("invert")) {
            invert = tag.getBoolean("invert");
            invertBox.setChecked(invert);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, screenWidth, screenHeight);
        for (int i = 0; i < fields.length; i++) {
            if (!isInputPortConnected(i)) {
                fields[i].mouseClicked(mouseX, mouseY, button);
            }
        }
        if (button == 0 && !isInputPortConnected(INVERT_PORT) && invertBox.mouseClicked(mouseX, mouseY, button)) {
            invert = invertBox.isChecked();
            changed = true;
        }
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        for (NumberField field : fields) {
            field.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        for (NumberField field : fields) {
            field.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        for (NumberField field : fields) {
            field.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public boolean hasFocusedInput() {
        for (NumberField field : fields) {
            if (field.isFocused()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearInputFocus() {
        for (NumberField field : fields) {
            field.setFocused(false);
        }
    }

    @Override
    public NodeType getType() {
        return NodeType.DISTANCE_BRIGHTNESS;
    }
}
