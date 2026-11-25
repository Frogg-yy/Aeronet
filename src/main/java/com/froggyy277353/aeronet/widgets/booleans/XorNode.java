package com.froggyy277353.aeronet.widgets.booleans;

import com.froggyy277353.aeronet.client.gui.CheckBox;
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

public class XorNode extends Node {
    private boolean on = false;
    private CheckBox onBox = new CheckBox(0, 0, 10);
    public XorNode(UUID id, int x, int y, int numInputs, int numOutputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 2, 1, width, PORT_START_OFFSET+PORT_SPACING*2, inputArray, outputArray);
        red = 0.392F; green = 0.455F; blue = 0.545F;
        onBox.setChecked(on);
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        this.width = Math.max(font.width(Component.translatable("aeronet.node.xor")) + 4, 14+font.width(Component.translatable("aeronet.node.boolean")));
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.xor"), screenX + 2, screenY + 2, 0xFFFFFF);
        logic();
        if (!isInputPortConnected(1))
        {
            onBox.setX(screenX+3+font.width(Component.translatable("aeronet.node.boolean")));
            onBox.setY(screenY+PORT_START_OFFSET+PORT_SPACING);
            graphics.drawString(font, Component.translatable("aeronet.node.boolean"), screenX+3, screenY+PORT_START_OFFSET+PORT_SPACING, 0xFFFFFF);
            onBox.render(graphics, mouseX, mouseY, partialTick, font);
            if (onBox.isChecked() != on)
            {
                changed = true;
                on = onBox.isChecked();
            }
        }
    }
    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        if (inputArray != null)
        {
            if (isInputPortConnected(1))
            {
                on = inputArray.get(1).asBoolean();
            }
            if (inputArray.getFirst().asBoolean() ^ on)
            {
                outputArray.set(0, NodeValue.of(true));
            }
            else
            {
                outputArray.set(0, NodeValue.of(false));
            }

        }

    }
    public void setExtraData(CompoundTag tag) {
        this.on = tag.getBoolean("on");
        onBox.setChecked(on);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putBoolean("on", on);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        onBox.mouseClicked(mouseX, mouseY, button);
        return result;
    }
    @Override
    public ValueType getInputType(int port) {
        return ValueType.BOOLEAN;
    }
    @Override
    public ValueType getOutputType(int port) {
        return ValueType.BOOLEAN;
    }
    @Override
    public NodeType getType() {
        return NodeType.XOR;
    }
}
