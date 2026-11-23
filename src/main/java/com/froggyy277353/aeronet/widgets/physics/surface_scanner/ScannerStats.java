package com.froggyy277353.aeronet.widgets.physics.surface_scanner;

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

public class ScannerStats extends Node {
    public ScannerStats(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 3, 37 + 41, PORT_START_OFFSET + 3 * PORT_SPACING, inputArray, outputArray);
        red = 0.957F;
        green = 0.247F;
        blue = 0.369F;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node.scanner_stats");
        Component scanText = Component.translatable("aeronet.node.scanner_text");

        Component[] outLabels = {
                Component.translatable("aeronet.node.scanner.min_dist"),
                Component.translatable("aeronet.node.scanner.hit_count"),
                Component.translatable("aeronet.node.scanner.isShip")
        };

        int leftColumnWidth = font.width(scanText);
        int rightColumnWidth = 0;
        for (Component outLabel : outLabels) {
            rightColumnWidth = Math.max(rightColumnWidth, font.width(outLabel));
        }

        int contentWidth = leftColumnWidth + rightColumnWidth + 12;
        this.width = Math.max(Math.max(font.width(title) + 4, font.width("WWWWWWWWWW")), contentWidth);

        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);

        int row0Y = screenY + PORT_START_OFFSET - 3;
        graphics.drawString(font, scanText, screenX + 3, row0Y + 2, 0xFFFFFF);

        for (int row = 0; row < outLabels.length; row++) {
            int outRowY = screenY + PORT_START_OFFSET - 3 + row * PORT_SPACING;
            int labelX = screenX + this.width - font.width(outLabels[row]) - 4;
            graphics.drawString(font, outLabels[row], labelX, outRowY + 2, 0xFFFFFF);
        }

        logic();
    }

    @Override
    public void tick() {
        super.tick();
        logic();
    }

    private void logic() {
        float minDist = -1.0f;
        int hitCount = 0;
        boolean shipDetected = false;

        if (isInputPortConnected(0) && !inputArray.isEmpty()) {
            NodeValue input = inputArray.getFirst();
            if (!input.isEmpty() && input.getType() == ValueType.LIST) {
                List<NodeValue> data = input.asList();
                if (data.size() >= 2) {
                    int gridSize = data.get(0).asInt();
                    int totalCells = gridSize * gridSize;

                    for (int i = 0; i < totalCells; i++) {
                        int offset = 2 + i * 2;
                        if (offset + 1 >= data.size()) {
                            break;
                        }

                        float dist = data.get(offset).asFloat();
                        if (dist >= 0.0f) {
                            hitCount++;
                            if (minDist < 0.0f || dist < minDist) {
                                minDist = dist;
                            }

                            int encodedId = data.get(offset + 1).asInt();
                            if (encodedId >= 100000) {
                                shipDetected = true;
                            }
                        }
                    }
                }
            }
        }

        setOutputValue(0, NodeValue.of(minDist));
        setOutputValue(1, NodeValue.of((float) hitCount));
        setOutputValue(2, NodeValue.of(shipDetected));
    }

    @Override
    public void setExtraData(CompoundTag tag) {
    }

    @Override
    public CompoundTag getExtraData() {
        return new CompoundTag();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        return super.mouseClicked(mouseX, mouseY, button, width, height);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
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
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public ValueType getInputType(int port) {
        if (port == 0) {
            return ValueType.LIST;
        }
        return ValueType.NULL;
    }

    @Override
    public ValueType getOutputType(int port) {
        return switch (port) {
            case 0, 1 -> ValueType.FLOAT;
            case 2 -> ValueType.BOOLEAN;
            default -> ValueType.NULL;
        };
    }

    @Override
    public NodeType getType() {
        return NodeType.SCANNER_STATS;
    }
}
