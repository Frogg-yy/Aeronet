package com.froggyy277353.aeronet.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class GUIElement {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    public GUIElement(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Font font)
    {

    }

    public void tick()
    {

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        return false;
    }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public boolean isFocused() {
        return false;
    }

    public void setX(int i) {
        x = i;
    }
    public void setY(int i) {
        y = i;
    }
}
