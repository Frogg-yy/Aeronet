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
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DrawScanNode extends Node {
    private static final int FIELD_CHARS = 5;
    private static final int SCAN_PORT = 0;
    private static final int X_PORT = 1;
    private static final int Y_PORT = 2;
    private static final int ROT_PORT = 3;
    private static final int DIST_PORT = 4;
    private static final int ON_PORT = 5;
    private static final int FILTER_PORT = 6;
    private static final int CANVAS_PORT = 7;
    private static final int TOTAL_INPUTS = 8;

    private final NumberField[] fields = new NumberField[5]; // x, y, rot, dist, on
    private final boolean[] wasFocused = new boolean[5];
    private final float[] params = new float[]{0, 0, 0, 32, 255};
    private final String[] fieldLabels = {"x", "y", "rot", "dist", "on"};


    public DrawScanNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                        List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, TOTAL_INPUTS, 1, width,
                PORT_START_OFFSET + TOTAL_INPUTS * PORT_SPACING, inputArray, outputArray);

        for (int i = 0; i < fields.length; i++) {
            fields[i] = new NumberField(0, 0, FIELD_CHARS, 12, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
            fields[i].setValue(Math.round(params[i]));
        }

        // Фиолетовый цвет нод отрисовки (#a855f7)
        red = 0.659F;
        green = 0.333F;
        blue = 0.969F;
    }

    @Override
    public ValueType getInputType(int port) {
        return switch (port) {
            case SCAN_PORT, CANVAS_PORT -> ValueType.LIST;
            case FILTER_PORT -> ValueType.STRING;
            case ON_PORT -> ValueType.INT;
            case X_PORT, Y_PORT, ROT_PORT, DIST_PORT -> ValueType.FLOAT;
            default -> ValueType.NULL;
        };
    }

    @Override
    public boolean acceptsInputType(int port, ValueType type) {
        return switch (port) {
            case SCAN_PORT, CANVAS_PORT -> type == ValueType.LIST;
            case FILTER_PORT -> type == ValueType.STRING || type == ValueType.LIST;
            case ON_PORT -> type == ValueType.INT || type == ValueType.FLOAT || type == ValueType.BOOLEAN;
            case X_PORT, Y_PORT, ROT_PORT, DIST_PORT -> type == ValueType.FLOAT || type == ValueType.INT;
            default -> false;
        };
    }

    @Override
    public ValueType getOutputType(int port) {
        return port == 0 ? ValueType.LIST : ValueType.NULL;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node.draw_scan");
        Component scanLabel = Component.translatable("aeronet.node.scanner_text");
        Component filterLabel = Component.translatable("aeronet.node.scanner.blockId");

        int labelWidth = Math.max(Math.max(font.width(scanLabel), font.width(filterLabel)), font.width("canvas"));
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

            if (row == SCAN_PORT) {
                graphics.drawString(font, scanLabel, screenX + 3, rowY + 2, 0xFFFFFF);
            } else if (row == FILTER_PORT) {
                graphics.drawString(font, filterLabel, screenX + 3, rowY + 2, 0xFFFFFF);
            } else if (row == CANVAS_PORT) {
                graphics.drawString(font, "canvas", screenX + 3, rowY + 2, 0xFFFFFF);
            } else {
                int fieldIdx = row - 1;
                graphics.drawString(font, fieldLabels[fieldIdx], screenX + 3, rowY + 2, 0xFFFFFF);
                if (!isInputPortConnected(row)) {
                    fields[fieldIdx].setX(widgetX);
                    fields[fieldIdx].setY(rowY + 2);
                    fields[fieldIdx].render(graphics, mouseX, mouseY, partialTick, font);
                    params[fieldIdx] = fields[fieldIdx].getValue();
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

    private Set<String> getAllowedBlocksFilter() {
        if (!isInputPortConnected(FILTER_PORT) || FILTER_PORT >= inputArray.size()) {
            return null;
        }
        NodeValue filterVal = inputArray.get(FILTER_PORT);
        if (filterVal.isEmpty()) {
            return null;
        }

        if (filterVal.type() == ValueType.STRING) {
            String singleId = filterVal.asString();
            return singleId.isEmpty() ? null : Set.of(singleId);
        }

        if (filterVal.type() == ValueType.LIST) {
            List<NodeValue> list = filterVal.asList();
            if (list.isEmpty()) {
                return null;
            }
            Set<String> allowed = new HashSet<>(list.size());
            for (NodeValue item : list) {
                if (!item.isEmpty()) {
                    String id = item.asString();
                    if (!id.isEmpty()) {
                        allowed.add(id);
                    }
                }
            }
            return allowed.isEmpty() ? null : allowed;
        }

        return null;
    }

    private void logic() {
        int offsetX = Math.round(readFloat(X_PORT, params[0]));
        int offsetY = Math.round(readFloat(Y_PORT, params[1]));
        float rotDegrees = readFloat(ROT_PORT, params[2]);
        float maxDist = Math.max(0.1f, readFloat(DIST_PORT, params[3]));

        int maxBrightness = Math.round(params[4]);
        if (isInputPortConnected(ON_PORT) && ON_PORT < inputArray.size()) {
            NodeValue onVal = inputArray.get(ON_PORT);
            if (onVal.type() == ValueType.BOOLEAN) {
                maxBrightness = onVal.asBoolean() ? 255 : 0;
            } else {
                maxBrightness = onVal.asInt();
            }
        }
        maxBrightness = Mth.clamp(maxBrightness, 0, 255);

        NodeValue canvasSource = isInputPortConnected(CANVAS_PORT) && CANVAS_PORT < inputArray.size()
                ? inputArray.get(CANVAS_PORT) : NodeValue.EMPTY;
        Canvas.Data canvas = Canvas.from(canvasSource);

        if (maxBrightness > 0 && isInputPortConnected(SCAN_PORT) && !inputArray.isEmpty()) {
            NodeValue scanInput = inputArray.get(SCAN_PORT);
            if (!scanInput.isEmpty() && scanInput.type() == ValueType.LIST) {
                List<NodeValue> data = scanInput.asList();
                if (data.size() >= 2) {
                    int gridSize = data.get(0).asInt();
                    List<NodeValue> palette = data.get(1).asList();
                    Set<String> allowedBlocks = getAllowedBlocksFilter();

                    float center = (gridSize - 1) * 0.5f;
                    double rad = Math.toRadians(rotDegrees);
                    float cos = (float) Math.cos(rad);
                    float sin = (float) Math.sin(rad);

                    for (int dstRow = 0; dstRow < gridSize; dstRow++) {
                        float dy = dstRow - center;
                        for (int dstCol = 0; dstCol < gridSize; dstCol++) {
                            float dx = dstCol - center;

                            int srcCol = Math.round(center + dx * cos + dy * sin);
                            int srcRow = Math.round(center - dx * sin + dy * cos);

                            if (srcCol < 0 || srcCol >= gridSize || srcRow < 0 || srcRow >= gridSize) {
                                continue;
                            }

                            int offset = 2 + (srcRow * gridSize + srcCol) * 2;
                            if (offset + 1 >= data.size()) {
                                continue;
                            }

                            float dist = data.get(offset).asFloat();
                            if (dist >= 0.0f) {
                                if (allowedBlocks != null) {
                                    int encodedId = data.get(offset + 1).asInt();
                                    int paletteIdx = encodedId >= 100000 ? (encodedId - 100000) : encodedId;
                                    String blockId = (paletteIdx >= 0 && paletteIdx < palette.size())
                                            ? palette.get(paletteIdx).asString()
                                            : "minecraft:air";
                                    if (!allowedBlocks.contains(blockId)) {
                                        continue;
                                    }
                                }

                                float factor = 1.0f - Mth.clamp(dist / maxDist, 0.0f, 1.0f);
                                int brightness = Math.round(factor * maxBrightness);
                                if (brightness > 0) {
                                    Canvas.set(canvas, offsetX + dstCol, offsetY + dstRow, brightness);
                                }
                            }
                        }
                    }
                }
            }
        }
        outputArray.set(0, Canvas.toValue(canvas));
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
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, screenWidth, screenHeight);
        for (int i = 0; i < fields.length; i++) {
            if (!isInputPortConnected(i + 1)) {
                fields[i].mouseClicked(mouseX, mouseY, button);
            }
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
        return NodeType.DRAW_SCAN;
    }
}