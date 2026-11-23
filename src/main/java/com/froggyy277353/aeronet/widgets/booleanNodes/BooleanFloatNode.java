package com.froggyy277353.aeronet.widgets.booleanNodes;

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

public class BooleanFloatNode extends Node {
    private float falseFloat = 0.0F;
    private float trueFloat = 1.0F;
    private NumberField numberFields[] = {
            new NumberField(0, 0, 4, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684),
            new NumberField(0, 0, 4, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684)
    };
    private String numberText[] = {
            Component.translatable("aeronet.node.logical.true").getString(),
            Component.translatable("aeronet.node.logical.false").getString()
    };
    public BooleanFloatNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                            List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 3, 1, 46, PORT_SPACING * 3 + PORT_START_OFFSET, inputArray, outputArray);
        red = 0.392F; green = 0.455F; blue = 0.545F;

        numberFields[0].setValue(trueFloat);
        numberFields[1].setValue(falseFloat);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        this.width = Math.max(font.width(Component.translatable("aeronet.node.boolean_float")) + 4, 10 * font.width("W"));
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, Component.translatable("aeronet.node.boolean_float"), screenX + 2, screenY + 2, 0xFFFFFF);

        for (int i = 0; i < 2; i++)
        {
            if (!isInputPortConnected(i))
            {
                graphics.drawString(font, numberText[i], screenX+4, screenY+PORT_START_OFFSET+PORT_SPACING*i, 0xFFFFFF);
                numberFields[i].setX(screenX+4+font.width(numberText[i]));
                numberFields[i].setY(screenY+PORT_START_OFFSET+PORT_SPACING*i);
                numberFields[i].render(graphics, mouseX, mouseY, partialTick, font);
            }
        }
        graphics.drawString(font, Component.translatable("aeronet.node.boolean"), screenX+4, screenY+PORT_SPACING+PORT_SPACING*2, 0xFFFFFF);
        trueFloat = numberFields[0].getValue();
        falseFloat = numberFields[1].getValue();
        logic();
    }
    @Override
    public void tick()
    {
        super.tick();
        logic();
    }
    private void logic()
    {
        if (isInputPortConnected(0))
        {
            trueFloat = inputArray.getFirst().asFloat();
        }
        if (isInputPortConnected(1))
        {
            falseFloat = inputArray.get(1).asFloat();
        }
        if (inputArray.get(2).asBoolean())
        {
            outputArray.set(0, NodeValue.of(trueFloat));
        }
        else
        {
            outputArray.set(0, NodeValue.of(falseFloat));
        }
    }


    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("trueFloat", trueFloat);
        tag.putFloat("falseFloat", falseFloat);
        return tag;
    }

    @Override
    public void setExtraData(CompoundTag tag) {
        this.trueFloat = tag.getFloat("trueFloat");
        this.falseFloat = tag.getFloat("falseFloat");
        numberFields[0].setValue(trueFloat);
        numberFields[1].setValue(falseFloat);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        numberFields[0].mouseClicked(mouseX, mouseY, button);
        numberFields[1].mouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        numberFields[0].mouseReleased(mouseX, mouseY, button);
        numberFields[1].mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public ValueType getInputType(int port) {
        if (port == 0 || port == 1)
        {
            return ValueType.FLOAT;
        }
        return ValueType.BOOLEAN;
    }
    @Override
    public ValueType getOutputType(int port)
    {
        return ValueType.FLOAT;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        numberFields[0].keyPressed(keyCode, scanCode, modifiers);
        numberFields[1].keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        numberFields[0].charTyped(codePoint, modifiers);
        numberFields[1].charTyped(codePoint, modifiers);
        return false;
    }

    @Override public NodeType getType() { return NodeType.BOOLEAN_FLOAT; }
}
