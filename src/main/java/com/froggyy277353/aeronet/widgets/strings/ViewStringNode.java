package com.froggyy277353.aeronet.widgets.strings;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.UUID;

public class ViewStringNode extends Node {
    public ViewStringNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                      List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 1, 0, 46, PORT_START_OFFSET + PORT_SPACING, inputArray, outputArray);
        red = 0.388F;
        green = 0.400F;
        blue = 0.945F;
    }
    @Override public ValueType getInputType(int port) { return ValueType.STRING; }
    @Override public ValueType getOutputType(int port) { return ValueType.STRING; }
    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        width = Math.max(font.width(Component.translatable("aeronet.node.view_string").getString())+3,  3+8 * font.width("W"));
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.view_string"), screenX + 2, screenY + 2, 0xFFFFFF);
        Component text = Component.literal("");
        if (!inputArray.isEmpty())
        {
            text = Component.literal(inputArray.getFirst().asString());
        }
        int maxWidth = 8*font.width("W");

        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), screenX+2, screenY+PORT_START_OFFSET + i * font.lineHeight, 0xFFFFFF, true);
        }
        int totalTextHeight = lines.size() * font.lineHeight;
        height = PORT_SPACING + PORT_START_OFFSET + totalTextHeight;
    }

    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }


    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override public NodeType getType() { return NodeType.VIEW_STRING; }
}
