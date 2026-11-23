package com.froggyy277353.aeronet.widgets.physics;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class GpsNode extends Node {
    public GpsNode(UUID id, int x, int y, int inputs, int outputs,
                   int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 3, width, 47, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                       float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node.gps");
        width = Math.max(font.width(title) + 4, font.width("X: -000000.000") + 8);
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 2, screenY + 2, 0xFFFFFF);
        for (int i = 0; i < 3; i++) {
            GuiUtils.drawOutlinedRect(graphics, screenX + 3, screenY + 11 + i * 11,
                    width - 6, 10, 0xFF454545, 0xFF646464);
            String axis = switch (i) { case 0 -> "X"; case 1 -> "Y"; default -> "Z"; };
            graphics.drawString(font, axis + ": " + outputArray.get(i).asFloat(),
                    screenX + 5, screenY + 12 + i * 11, 0xFFFFFF);
        }
    }

    @Override public CompoundTag getExtraData() { return new CompoundTag(); }
    @Override public void setExtraData(CompoundTag tag) {}
    @Override public NodeType getType() { return NodeType.GPS; }
}
