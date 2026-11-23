package com.froggyy277353.aeronet.widgets.physics;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class AnalogGyroNode extends Node {
    private Font font = Minecraft.getInstance().font;
    public AnalogGyroNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0,3, 37+9, 36+27, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;

    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        if (font.width(Component.translatable("block.aeronet.analog_gyro")) + 4 < font.width("WWWWWWWWWW"))
        {
            this.width = font.width("WWWWWWWWWW");
        }
        else
        {
            width = font.width(Component.translatable("block.aeronet.analog_gyro")) + 4;
        }
        graphics.drawString(font, Component.translatable("block.aeronet.analog_gyro"), screenX+2, screenY+2, 0xFFFFFF);
        GuiUtils.drawOutlinedRect(graphics, screenX+3, screenY+11, font.width("WWWWWWWW"), 10, 0xFF454545, 0xFF646464);
        GuiUtils.drawOutlinedRect(graphics, screenX+3, screenY+22, font.width("WWWWWWWW"), 10, 0xFF454545, 0xFF646464);
        GuiUtils.drawOutlinedRect(graphics, screenX+3, screenY+33, font.width("WWWWWWWW"), 10, 0xFF454545, 0xFF646464);
        graphics.drawString(font, String.valueOf(outputArray.getFirst().asFloat()), screenX+5, screenY+13, 0xFFFFFF);
        graphics.drawString(font, String.valueOf(outputArray.get(1).asFloat()), screenX+5, screenY+22, 0xFFFFFF);
        graphics.drawString(font, String.valueOf(outputArray.get(2).asFloat()), screenX+5, screenY+33, 0xFFFFFF);
        logic();
    }

    @Override
    public void tick()
    {
        super.tick();
        logic();
    }

    public void logic()
    {

    }

    public void setExtraData(CompoundTag tag) {

    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);

        return result;
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
    public NodeType getType() {
        return NodeType.GYRO;
    }
}
