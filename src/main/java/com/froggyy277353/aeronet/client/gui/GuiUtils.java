package com.froggyy277353.aeronet.client.gui;

import com.froggyy277353.aeronet.network.NodesData;
import com.froggyy277353.aeronet.widgets.*;
import com.froggyy277353.aeronet.widgets.booleans.BooleanFloatNode;
import com.froggyy277353.aeronet.widgets.booleans.NotNode;
import com.froggyy277353.aeronet.widgets.logical.*;
import com.froggyy277353.aeronet.widgets.math.*;
import com.froggyy277353.aeronet.widgets.physics.*;
import com.froggyy277353.aeronet.widgets.physics.display.*;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.*;
import com.froggyy277353.aeronet.widgets.strings.ConcatStringNode;
import com.froggyy277353.aeronet.widgets.strings.ItemConstantNode;
import com.froggyy277353.aeronet.widgets.strings.StringNode;
import com.froggyy277353.aeronet.widgets.strings.ViewStringNode;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GuiUtils {
    public static String getLongestString(List<String> list) {
        String longest = "";

        for (String string : list) {
            if (string.length() > longest.length()) {
                longest = string;
            }
        }

        return longest;
    }
    public static String getPixelLongestString(List<String> list, Font font) {
        String longest = "";

        for (String string : list) {
            if (font.width(string) > font.width(longest)) {
                longest = string;
            }
        }
        return longest;
    }

    public static void drawOutlinedRect(GuiGraphics graphics, int x, int y, int width, int height, int bgColor, int lineColor)
    {
        graphics.fill(x, y, x+width, y+height, bgColor);
        graphics.fill(x, y, x+width, y+1, lineColor);
        graphics.fill(x, y, x+1, y+height, lineColor);
        graphics.fill(x, y+height-1, x+width, y+height, lineColor);
        graphics.fill(x+width-1, y, x+width, y+height, lineColor);
    }
    public static void drawItem(GuiGraphics graphics, String path, int x, int y, int scale)
    {
        graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(path))), x, y);
    }

    public static void drawButton(GuiGraphics graphics, int x, int y, int width, int height, String text, int bgColor, int lineColor, int textColor, boolean hovered, Font font)
    {
        drawOutlinedRect(graphics, x, y, width, height, bgColor, lineColor);
        graphics.drawString(font, text, x+2, y+2, textColor);
    }

    public static int divide(int a, int b) {
        if (b == 0) {
            return 0;
        }

        int result = a / b;

        if (result > 1024) {
            return 1024;
        }

        if (result < -1024) {
            return -1024;
        }

        return result;
    }
    public static int lighten(int color) {
            int a = (color >> 24) & 0xFF;
            int r = Math.min(255, ((color >> 16) & 0xFF) + 40);
            int g = Math.min(255, ((color >> 8) & 0xFF) + 40);
            int b = Math.min(255, (color & 0xFF) + 40);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    public static int darken(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, ((color >> 16) & 0xFF) - 40);
        int g = Math.max(0, ((color >> 8) & 0xFF) - 40);
        int b = Math.max(0, (color & 0xFF) - 40);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        if (inMax == inMin) {
            return outMin;
        }
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public static int limitValue(int value)
    {
        if (value > 1024)
        {
            return 1024;
        }
        else if (value < -1024)
        {
            return -1024;
        }
        else
        {
            return value;
        }
    }
    public static Node createNodeByData(NodesData data) {
        Node node = data.nodeType().create(data);
        node.setExtraData(data.extraData());
        return node;
    }

    public static void drawThreeSlice(GuiGraphics graphics, ResourceLocation left, ResourceLocation center, ResourceLocation right, int x, int y, int width, int textureHeight, int border) {

        int centerWidth = width - border * 2;

        graphics.blit(
                left,
                x,
                y,
                0, 0,
                border, textureHeight,
                border, textureHeight
        );

        graphics.blit(
                center,
                x + border,
                y,
                0, 0,
                centerWidth, textureHeight,
                border, textureHeight
        );

        graphics.blit(
                right,
                x + width - border,
                y,
                0, 0,
                border, textureHeight,
                border, textureHeight
        );
    }

    public static void drawNineSlice(GuiGraphics graphics, ResourceLocation upperLeft, ResourceLocation upperEdge, ResourceLocation upperRight, ResourceLocation leftEdge, ResourceLocation center, ResourceLocation rightEdge, ResourceLocation lowerLeft, ResourceLocation lowerEdge, ResourceLocation lowerRight, int x, int y, int width, int height, int border) {

        int centerWidth = width - border * 2;
        int centerHeight = height - border * 2;

        graphics.blit(
                upperLeft,
                x,
                y,
                0, 0,
                border, border,
                border, border
        );

        graphics.blit(
                upperEdge,
                x + border,
                y,
                0, 0,
                centerWidth, border,
                border, border
        );

        graphics.blit(
                upperRight,
                x + width - border,
                y,
                0, 0,
                border, border,
                border, border
        );

        graphics.blit(
                leftEdge,
                x,
                y + border,
                0, 0,
                border, centerHeight,
                border, border
        );

        graphics.blit(
                center,
                x + border,
                y + border,
                0, 0,
                centerWidth, centerHeight,
                border, border
        );

        graphics.blit(
                rightEdge,
                x + width - border,
                y + border,
                0, 0,
                border, centerHeight,
                border, border
        );

        graphics.blit(
                lowerLeft,
                x,
                y + height - border,
                0, 0,
                border, border,
                border, border
        );

        graphics.blit(
                lowerEdge,
                x + border,
                y + height - border,
                0, 0,
                centerWidth, border,
                border, border
        );

        graphics.blit(
                lowerRight,
                x + width - border,
                y + height - border,
                0, 0,
                border, border,
                border, border
        );
    }

    public static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            graphics.fill(x0, y0, x0 + 2, y0 + 2, color);

            if (x0 == x1 && y0 == y1)
                break;

            int e2 = err * 2;

            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }

            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public static void drawOutlinedList(GuiGraphics graphics, Font font, int x, int y, List<String> text, int bgColor, int lineColor, int textColor)
    {
        int width = font.width(getPixelLongestString(text, font));
        int height = text.size() * font.lineHeight;
        drawOutlinedRect(graphics, x, y, width+3, height+3, bgColor, lineColor);
        for (int i = 0 ; i < text.size(); i ++)
        {
           graphics.drawString(font, text.get(i), x+2, y+2+i*font.lineHeight, textColor);
        }

    }
    public static ScanCell getCell(NodeValue input, int x, int y) {
        if (input.isEmpty() || input.getType() != ValueType.LIST) return ScanCell.EMPTY;
        List<NodeValue> data = input.asList();
        if (data.size() < 2) return ScanCell.EMPTY;

        int gridSize = data.get(0).asInt();
        if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) return ScanCell.EMPTY;

        int offset = 2 + (y * gridSize + x) * 2;
        if (offset + 1 >= data.size()) return ScanCell.EMPTY;

        float dist = data.get(offset).asFloat();
        if (dist < 0) return ScanCell.EMPTY;

        int encodedId = data.get(offset + 1).asInt();
        boolean isShip = encodedId >= 100000;
        int paletteIdx = isShip ? (encodedId - 100000) : encodedId;

        List<NodeValue> palette = data.get(1).asList();
        String blockId = (paletteIdx >= 0 && paletteIdx < palette.size())
                ? palette.get(paletteIdx).asString()
                : "minecraft:air";

        return new ScanCell(true, dist, blockId, isShip);
    }
    public static float limitValue(float value, float min, float max)
    {
        if (value > max)
        {
            return max;
        }
        else if (value < min)
        {
            return min;
        }
        else
        {
            return value;
        }
    }
}
