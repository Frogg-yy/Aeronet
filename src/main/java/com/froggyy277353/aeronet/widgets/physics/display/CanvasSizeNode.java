
package com.froggyy277353.aeronet.widgets.physics.display;

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

public class CanvasSizeNode extends Node {
    private static final int FIELD_CHARS = 5;
    private static final int MIN_SIZE = 8;
    public static final int MAX_CANVAS_SIZE = 40;

    private final NumberField[] fields = new NumberField[2];
    private final boolean[] wasFocused = new boolean[2];

    private final String[] labels = {"width", "height"};

    private int canvasWidth = 8;
    private int canvasHeight = 8;
    private int widgetX;

    public CanvasSizeNode(
            UUID id,
            int x,
            int y,
            int inputs,
            int outputs,
            int width,
            int height,
            List<NodeValue> inputArray,
            List<NodeValue> outputArray
    ) {
        super(
                id,
                x,
                y,
                0,
                1,
                100,
                48,
                inputArray,
                outputArray
        );

        for (int i = 0; i < fields.length; i++) {
            fields[i] = new NumberField(
                    0, 0, FIELD_CHARS, 12,
                    0xFF454545,
                    0xFFFFFFFF,
                    0xFF646464
            );
        }

        fields[0].setValue(canvasWidth);
        fields[1].setValue(canvasHeight);

        red = 0.659F;
        green = 0.333F;
        blue = 0.969F;

        logic();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            Font font,
            int mouseX,
            int mouseY,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Component title = Component.translatable(
                "aeronet.node.canvas_size"
        );

        int labelWidth = Math.max(
                font.width(labels[0]),
                font.width(labels[1])
        );

        int fieldWidth = font.width("W") * FIELD_CHARS + 8;

        width = Math.max(
                font.width(title) + 4,
                labelWidth + fieldWidth + 12
        );

        super.render(
                graphics, font, mouseX, mouseY,
                partialTick, screenWidth, screenHeight
        );

        graphics.drawString(
                font, title,
                screenX + 2, screenY + 2,
                0xFFFFFF
        );

        widgetX = screenX + 3 + labelWidth + 4;

        for (int i = 0; i < fields.length; i++) {
            int rowY = screenY
                    + PORT_START_OFFSET - 3
                    + i * PORT_SPACING;

            graphics.drawString(
                    font,
                    Component.translatable(
                            "aeronet.node.canvas_size." + labels[i]
                    ),
                    screenX + 3,
                    rowY + 2,
                    0xFFFFFF
            );

            fields[i].setX(widgetX+3);
            fields[i].setY(rowY+1);

            fields[i].render(
                    graphics, mouseX, mouseY, partialTick, font
            );

            boolean focused = fields[i].isFocused();

            if (wasFocused[i] && !focused) {
                normalizeFields();
                changed = true;
            }

            wasFocused[i] = focused;
        }

        canvasWidth = normalizeSize(fields[0].getValue());
        canvasHeight = normalizeSize(fields[1].getValue());

        logic();
    }

    private static int normalizeSize(float value) {
        int rounded = Math.round(value);
        rounded = Math.clamp(rounded, MIN_SIZE, MAX_CANVAS_SIZE);

        return Math.clamp(
                Math.round(rounded / 8.0F) * 8
                ,
                MIN_SIZE,
                MAX_CANVAS_SIZE);
    }

    private void normalizeFields() {
        canvasWidth = normalizeSize(fields[0].getValue());
        canvasHeight = normalizeSize(fields[1].getValue());

        fields[0].setValue(canvasWidth);
        fields[1].setValue(canvasHeight);
    }

    private void logic() {
        canvasWidth = normalizeSize(fields[0].getValue());
        canvasHeight = normalizeSize(fields[1].getValue());

        Canvas.Data canvas = new Canvas.Data(
                canvasWidth,
                canvasHeight
        );

        outputArray.set(0, Canvas.toValue(canvas));
    }

    @Override
    public void tick() {
        super.tick();
        logic();
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.LIST;
    }
    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("canvasWidth", canvasWidth);
        tag.putInt("canvasHeight", canvasHeight);
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        if (tag.contains("canvasWidth")) {
            canvasWidth = normalizeSize(tag.getInt("canvasWidth"));
        }

        if (tag.contains("canvasHeight")) {
            canvasHeight = normalizeSize(tag.getInt("canvasHeight"));
        }

        fields[0].setValue(canvasWidth);
        fields[1].setValue(canvasHeight);

        logic();
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            int screenWidth,
            int screenHeight
    ) {
        boolean result = super.mouseClicked(
                mouseX, mouseY, button,
                screenWidth, screenHeight
        );

        for (NumberField field : fields) {
            field.mouseClicked(mouseX, mouseY, button);
        }

        return result;
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        super.mouseReleased(mouseX, mouseY, button);

        for (NumberField field : fields) {
            field.mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
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
        return NodeType.CANVAS_SIZE;
    }
}
