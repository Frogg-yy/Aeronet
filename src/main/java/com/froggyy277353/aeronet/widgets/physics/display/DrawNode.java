package com.froggyy277353.aeronet.widgets.physics.display;

import com.froggyy277353.aeronet.client.gui.CheckBox;
import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.client.gui.NumberField;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public abstract class DrawNode extends Node {
    private static final int FIELD_CHARS = 5;
    private static final int BOX = 10;

    private final String titleKey;
    private final String[] labels;
    private final String fillLabel;
    private final int paramCount;
    private final int fillPort;
    private final int onPort;
    private final int canvasPort;
    private final NumberField[] fields;
    private final boolean[] wasFocused;
    private final float[] params;
    private boolean fill;
    private int on = 255;
    private final NumberField onField;
    private boolean onWasFocused;
    private int widgetX;
    private final CheckBox fillBox;

    protected DrawNode(UUID id, int x, int y, int width,
                       List<NodeValue> inputArray, List<NodeValue> outputArray,
                       String titleKey, String[] labels, float[] defaults, String fillLabel) {
        super(id, x, y, rowCount(labels.length, fillLabel), 1, width,
                PORT_START_OFFSET + rowCount(labels.length, fillLabel) * PORT_SPACING, inputArray, outputArray);
        this.titleKey = titleKey;
        this.labels = labels;
        this.fillLabel = fillLabel;
        this.paramCount = labels.length;
        this.fillPort = fillLabel != null ? paramCount : -1;
        this.onPort = paramCount + (fillLabel != null ? 1 : 0);
        this.canvasPort = onPort + 1;
        this.fields = new NumberField[paramCount];
        this.wasFocused = new boolean[paramCount];
        this.params = new float[paramCount];
        this.fillBox = new CheckBox(0, 0, BOX);
        this.onField = new NumberField(0, 0, FIELD_CHARS, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
        this.onField.setValue(255);
        for (int i = 0; i < paramCount; i++) {
            fields[i] = new NumberField(0, 0, FIELD_CHARS, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
            setParam(i, defaults[i]);
        }
        red = 0.659F;
        green = 0.333F;
        blue = 0.969F;
    }

    private static int rowCount(int params, String fillLabel) {
        return params + (fillLabel != null ? 1 : 0) + 2;
    }

    protected abstract void draw(Canvas.Data canvas, float[] p, boolean fill, int on);

    protected void setParam(int index, float value) {
        params[index] = value;
        fields[index].setValue(Math.round(value));
    }

    @Override
    public ValueType getInputType(int port) {
        if (port == onPort) {
            return ValueType.INT;
        }
        if (port == fillPort) {
            return ValueType.BOOLEAN;
        }
        if (port == canvasPort)
        {
            return ValueType.LIST;
        }
        return ValueType.FLOAT;
    }

    @Override
    public ValueType getOutputType(int port) {
        return ValueType.LIST;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable(titleKey);
        int labelWidth = Math.max(font.width("on"), fillLabel != null ? font.width(fillLabel) : 0);
        for (String label : labels) {
            labelWidth = Math.max(labelWidth, font.width(label));
        }
        int fieldWidth = font.width("W") * FIELD_CHARS + 8;
        width = Math.max(font.width(title) + 4, labelWidth + fieldWidth + 10);

        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);

        widgetX = screenX + 3 + labelWidth + 4;
        for (int row = 0; row <= canvasPort; row++) {
            int rowY = screenY + PORT_START_OFFSET - 3 + row * PORT_SPACING;
            String label = row < paramCount ? labels[row]
                    : row == fillPort ? fillLabel
                    : row == onPort ? "on" : "canvas";
            graphics.drawString(font, label, screenX + 3, rowY + 2, 0xFFFFFF);
            if (isInputPortConnected(row)) {
                continue;
            }
            if (row < paramCount) {
                fields[row].setX(widgetX);
                fields[row].setY(rowY + 2);
                fields[row].render(graphics, mouseX, mouseY, partialTick, font);
                params[row] = fields[row].getValue();
            } else if (row == fillPort) {
                fillBox.setX(widgetX);
                fillBox.setY(rowY + 1);
                fillBox.render(graphics, mouseX, mouseY, partialTick, font);
                fill = fillBox.isChecked();
            } else if (row == onPort) {
                onField.setX(widgetX);
                onField.setY(rowY + 2);
                onField.render(graphics, mouseX, mouseY, partialTick, font);
                on = Math.clamp(Math.round(onField.getValue()), 0, 255);
            }
        }

        for (int i = 0; i < paramCount; i++) {
            boolean focused = fields[i].isFocused();
            if (wasFocused[i] && !focused) {
                changed = true;
            }
            wasFocused[i] = focused;
        }
        logic();
    }

    private static void drawBox(GuiGraphics graphics, int x, int y, boolean checked) {
        GuiUtils.drawOutlinedRect(graphics, x, y, BOX, BOX, 0xFF454545, 0xFF646464);
        if (checked) {
            graphics.fill(x + 3, y + 3, x + BOX - 3, y + BOX - 3, 0xFFFFFFFF);
        }
    }

    @Override
    public void tick() {
        super.tick();
        logic();
    }

    private void logic() {
        float[] p = new float[paramCount];
        for (int i = 0; i < paramCount; i++) {
            p[i] = isInputPortConnected(i) && i < inputArray.size() ? inputArray.get(i).asFloat() : params[i];
        }
        boolean f = fillPort >= 0 && (isInputPortConnected(fillPort) && fillPort < inputArray.size()
                ? inputArray.get(fillPort).asBoolean() : fill);
        int o = on;
        if (isInputPortConnected(onPort) && onPort < inputArray.size()) {
            NodeValue onVal = inputArray.get(onPort);
            if (onVal.type() == ValueType.BOOLEAN) {
                o = onVal.asBoolean() ? 255 : 0;
            } else {
                o = Math.clamp(onVal.asInt(), 0, 255);
            }
        }
        NodeValue source = isInputPortConnected(canvasPort) && canvasPort < inputArray.size()
                ? inputArray.get(canvasPort) : NodeValue.EMPTY;

        Canvas.Data canvas = Canvas.from(source);
        draw(canvas, p, f, o);
        outputArray.set(0, Canvas.toValue(canvas));
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < paramCount; i++) {
            tag.putFloat("p" + i, params[i]);
        }
        tag.putBoolean("fill", fill);
        tag.putInt("on", on);
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        for (int i = 0; i < paramCount; i++) {
            if (tag.contains("p" + i)) {
                setParam(i, tag.getFloat("p" + i));
            }
        }
        if (tag.contains("fill")) {
            fill = tag.getBoolean("fill");
            fillBox.setChecked(fill);
        }
        if (tag.contains("on")) {
            on = tag.getInt("on");
            onField.setValue(on);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, screenWidth, screenHeight);
        for (int i = 0; i < paramCount; i++) {
            if (!isInputPortConnected(i)) {
                fields[i].mouseClicked(mouseX, mouseY, button);
            }
        }
        if (button == 0) {
            if (fillPort >= 0 && !isInputPortConnected(fillPort) && fillBox.mouseClicked(mouseX, mouseY, button)) {
                fill = fillBox.isChecked();
                changed = true;
            }
        }
        onField.mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        for (NumberField field : fields) {
            field.mouseReleased(mouseX, mouseY, button);
        }
        onField.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        for (NumberField field : fields) {
            field.keyPressed(keyCode, scanCode, modifiers);
        }
        onField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        for (NumberField field : fields) {
            field.charTyped(codePoint, modifiers);
        }
        onField.charTyped(codePoint, modifiers);
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
}
