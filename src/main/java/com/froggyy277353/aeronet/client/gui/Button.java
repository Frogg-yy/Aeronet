package com.froggyy277353.aeronet.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class Button extends GUIElement {
    private final boolean toggle;
    private final String text;
    private final String textPressed;
    private final int bgColor;
    private final int bgColorDarker;
    private final int textColor;
    private final int outlineColor;
    private boolean isPressed;

    public Button(int x, int y, int width, int height, boolean toggle, String text, String textPressed, int bgColor, int textColor, int outlineColor) {
        super(x, y, width, height);
        this.toggle = toggle;
        this.text = text;
        this.textPressed = textPressed;
        this.bgColor = bgColor;
        this.bgColorDarker = GuiUtils.darken(bgColor);
        this.textColor = textColor;
        this.outlineColor = outlineColor;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Font font) {
        boolean hovered = isOverButton(mouseX, mouseY);
        String label = (isPressed && toggle) ? textPressed : text;

        GuiUtils.drawOutlinedRect(graphics, x, y, width, height, hovered ? bgColorDarker : bgColor, outlineColor);
        graphics.drawCenteredString(font, label, x + width / 2, y + height / 2 - font.lineHeight / 2, textColor);
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void unPress() {
        isPressed = false;
    }

    public boolean isOverButton(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isOverButton((int) mouseX, (int) mouseY)) {
            isPressed = toggle ? !isPressed : true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !toggle) {
            isPressed = false;
        }
        return false;
    }

    public void setState(boolean state) {
        isPressed = state;
    }
}