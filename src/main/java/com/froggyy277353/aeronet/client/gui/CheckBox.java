package com.froggyy277353.aeronet.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CheckBox extends GUIElement {
    private boolean checked;
    private final int bgColor;
    private final int checkColor;
    private final int lineColor;
    private boolean hovered = false;

    public CheckBox(int x, int y, int size, int bgColor, int checkColor, int lineColor) {
        super(x, y, size, size);
        this.bgColor = bgColor;
        this.checkColor = checkColor;
        this.lineColor = lineColor;
    }

    public CheckBox(int x, int y, int size) {
        this(x, y, size, 0xFF454545, 0xFFFFFFFF, 0xFF646464);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Font font) {
        hovered = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        int actualBgColor = hovered ? lighten(bgColor) : bgColor;
        int actualLineColor = hovered ? lighten(lineColor) : lineColor;

        GuiUtils.drawOutlinedRect(graphics, x, y, width, height, actualBgColor, actualLineColor);
        if (checked) {
            graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, checkColor);
        }
    }

    private int lighten(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 40);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 40);
        int b = Math.min(255, (color & 0xFF) + 40);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        boolean inside = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        if (inside) {
            checked = !checked;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public void setX(int screenX) {
        x = screenX;
    }

    public void setY(int screenY) {
        y = screenY;
    }
}
