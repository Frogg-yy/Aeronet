package com.froggyy277353.aeronet.widgets.physics.surface_scanner;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
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

public class ScannerPixelNode extends Node {
    private boolean xwasFocused = false;
    private boolean yWasFocused = false;
    private int xVar;
    private int yVar;
    private ScanCell scan = new ScanCell(false,0,"",false);
    private NumberField xField = new NumberField(0, 0, 8, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private NumberField yField = new NumberField(0, 0, 4, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    public ScannerPixelNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 3, 4, 37+41, PORT_START_OFFSET + 4 * PORT_SPACING, inputArray, outputArray);
        red = 0.957F;
        green = 0.247F;
        blue = 0.369F;
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        Component title = Component.translatable("aeronet.node.scanner_pixel");
        Component scanText = Component.translatable("aeronet.node.scanner_text");
        Component xLabel = Component.translatable("aeronet.node.x");
        Component yLabel = Component.translatable("aeronet.node.y");

        Component[] outLabels = {
                Component.translatable("aeronet.node.scanner.hit"),
                Component.translatable("aeronet.node.scanner.distance"),
                Component.translatable("aeronet.node.scanner.blockId"),
                Component.translatable("aeronet.node.scanner.isShip")
        };

        int leftColumnWidth = Math.max(
                font.width(scanText),
                Math.max(font.width(xLabel) + xField.getPreferredWidth(font), font.width(yLabel) + yField.getPreferredWidth(font))
        ) + 6;

        int rightColumnWidth = 0;
        for (Component outLabel : outLabels) {
            rightColumnWidth = Math.max(rightColumnWidth, font.width(outLabel));
        }

        int contentWidth = leftColumnWidth + rightColumnWidth + 12;
        this.width = Math.max(Math.max(font.width(title) + 4, font.width("WWWWWWWWWW")), contentWidth);

        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);

        int row0Y = screenY + PORT_START_OFFSET - 3;
        int row1Y = screenY + PORT_START_OFFSET - 3 + PORT_SPACING;
        int row2Y = screenY + PORT_START_OFFSET - 3 + 2 * PORT_SPACING;

        graphics.drawString(font, scanText, screenX + 3, row0Y + 2, 0xFFFFFF);

        xField.setX(screenX + 4 + font.width(xLabel));
        xField.setY(row1Y);
        if (!isInputPortConnected(1)) {
            graphics.drawString(font, xLabel, screenX + 3, row1Y + 2, 0xFFFFFF);
            xField.render(graphics, mouseX, mouseY, partialTick, font);
        }

        yField.setX(screenX + 4 + font.width(yLabel));
        yField.setY(row2Y);
        if (!isInputPortConnected(2)) {
            graphics.drawString(font, yLabel, screenX + 3, row2Y + 2, 0xFFFFFF);
            yField.render(graphics, mouseX, mouseY, partialTick, font);
        }

        for (int row = 0; row < outLabels.length; row++) {
            int outRowY = screenY + PORT_START_OFFSET - 3 + row * PORT_SPACING;
            int labelX = screenX + this.width - font.width(outLabels[row]) - 4;
            graphics.drawString(font, outLabels[row], labelX, outRowY + 2, 0xFFFFFF);
        }


        boolean isFocusedNow = xField.isFocused();
        boolean isAngleFocusedNow = yField.isFocused();
        if (xwasFocused && !isFocusedNow) {
            changed = true;
        }
        xwasFocused = isFocusedNow;
        if (yWasFocused && !isAngleFocusedNow) {
            changed = true;
        }
        yWasFocused = isAngleFocusedNow;
        logic();
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic() {
        if (isInputPortConnected(1) && inputArray.size() > 1) {
            xVar = inputArray.get(1).asInt();
        } else {
            xVar = (int) xField.getValue();
        }
        if (isInputPortConnected(2) && inputArray.size() > 2) {
            yVar = inputArray.get(2).asInt();
        } else {
            yVar = (int) yField.getValue();
        }
        if (isInputPortConnected(0) && !inputArray.isEmpty()) {
            scan = GuiUtils.getCell(inputArray.getFirst(), xVar, yVar);
        } else {
            scan = new ScanCell(false, 0.0f, "", false);
        }

        setOutputValue(0, NodeValue.of(scan.hit()));
        setOutputValue(1, NodeValue.of(scan.distance()));
        setOutputValue(2, NodeValue.of(scan.blockId()));
        setOutputValue(3, NodeValue.of(scan.isShip()));
    }


    public void setExtraData(CompoundTag tag) {
        this.xVar = tag.getInt("xVar");
        this.yVar = tag.getInt("yVar");
        xField.setValue(xVar);
        yField.setValue(yVar);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putInt("xVar", xVar);
        tag.putInt("yVar", yVar);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        xField.mouseClicked(mouseX, mouseY, button);
        yField.mouseClicked(mouseX, mouseY, button);
        return result;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        xField.mouseReleased(mouseX, mouseY, button);
        yField.mouseReleased(mouseX, mouseY, button);
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
        xField.keyPressed(keyCode, scanCode, modifiers);
        yField.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        xField.charTyped(codePoint, modifiers);
        yField.charTyped(codePoint, modifiers);
        return false;
    }

    @Override
    public ValueType getInputType(int port) {
        if (port == 0) {
            return ValueType.LIST;
        }
        if (port == 1 || port == 2) {
            return ValueType.INT;
        }
        return ValueType.NULL;
    }
    @Override
    public ValueType getOutputType(int port) {
        return switch (port) {
            case 0, 3 -> ValueType.BOOLEAN;
            case 1 -> ValueType.FLOAT;
            case 2 -> ValueType.STRING;
            default -> ValueType.NULL;
        };
    }


    @Override
    public NodeType getType() {
        return NodeType.SCANNER_PIXEL;
    }
    @Override
    public boolean hasFocusedInput() {
        return xField.isFocused() || yField.isFocused();
    }

    @Override
    public void clearInputFocus() {
        xField.setFocused(false);
        yField.setFocused(false);
    }
}
