package com.froggyy277353.aeronet.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class NumberField extends GUIElement {
    private StringBuilder text = new StringBuilder();
    private boolean focused = false;
    private final int maxLength;
    private final int displayChars;
    private int cursorTick = 0;
    private int cursorPos = 0;
    private int selectionAnchor = 0;
    private int scrollOffset = 0;
    private Font lastFont;

    private final int bgColor;
    private final int textColor;
    private final int lineColor;
    private boolean hovered = false;
    private static final int PADDING = 4;

    public NumberField(int x, int y, int maxLength, int height, int bgColor, int textColor, int lineColor) {
        this(x, y, maxLength, maxLength > 0 ? maxLength : 6, height, bgColor, textColor, lineColor);
    }

    public NumberField(int x, int y, int maxLength, int displayChars, int height, int bgColor, int textColor, int lineColor) {
        super(x, y, 0, height);
        this.maxLength = maxLength;
        this.displayChars = Math.max(1, displayChars);
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.lineColor = lineColor;
    }

    public String getText() {
        return text.toString();
    }

    public float getValue() {
        String value = text.toString();
        if (value.isEmpty() || value.equals("-") || value.equals(".") || value.equals("-.")) {
            return 0;
        }
        try {
            float parsed = Float.parseFloat(value);
            return Float.isFinite(parsed) ? parsed : 0.0F;
        } catch (NumberFormatException ignored) {
            return 0.0F;
        }
    }

    public void setText(String value) {
        if (value == null) {
            text = new StringBuilder();
        } else if (maxLength > 0 && value.length() > maxLength) {
            text = new StringBuilder(value.substring(0, maxLength));
        } else {
            text = new StringBuilder(value);
        }
        cursorPos = text.length();
        selectionAnchor = cursorPos;
        scrollOffset = 0;
    }

    public void setValue(float value) {
        setText(new java.math.BigDecimal(Float.toString(Float.isFinite(value) ? value : 0.0F))
                .stripTrailingZeros().toPlainString());
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (!focused) {
            selectionAnchor = cursorPos;
        }
    }

    private boolean hasSelection() {
        return cursorPos != selectionAnchor;
    }

    private String getSelectedText() {
        int start = Math.min(cursorPos, selectionAnchor);
        int end = Math.max(cursorPos, selectionAnchor);
        return text.substring(start, end);
    }

    private void deleteSelection() {
        if (!hasSelection()) {
            return;
        }
        int start = Math.min(cursorPos, selectionAnchor);
        int end = Math.max(cursorPos, selectionAnchor);
        text.delete(start, end);
        cursorPos = start;
        selectionAnchor = start;
    }

    private void insertFiltered(String raw) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        deleteSelection();

        for (int i = 0; i < raw.length(); i++) {
            if (maxLength > 0 && text.length() >= maxLength) {
                break;
            }
            char c = raw.charAt(i);
            if (Character.isDigit(c)) {
                text.insert(cursorPos, c);
                cursorPos++;
            } else if ((c == '.' || c == ',') && text.indexOf(".") < 0) {
                text.insert(cursorPos, '.');
                cursorPos++;
            } else if (c == '-' && cursorPos == 0 && text.indexOf("-") < 0) {
                text.insert(0, '-');
                cursorPos++;
            }
        }
        selectionAnchor = cursorPos;
    }

    private void moveCursor(int newPos, boolean select) {
        cursorPos = Mth.clamp(newPos, 0, text.length());
        if (!select) {
            selectionAnchor = cursorPos;
        }
    }

    private void updateScroll(Font font, int innerWidth) {
        String fullText = text.toString();
        scrollOffset = Mth.clamp(scrollOffset, 0, fullText.length());

        if (cursorPos < scrollOffset) {
            scrollOffset = cursorPos;
        } else {
            String visibleFromScroll = fullText.substring(scrollOffset);
            String clipped = font.plainSubstrByWidth(visibleFromScroll, innerWidth);
            int maxVisibleIndex = scrollOffset + clipped.length();
            if (cursorPos > maxVisibleIndex) {
                String beforeCursor = fullText.substring(0, cursorPos);
                String tail = font.plainSubstrByWidth(beforeCursor, innerWidth, true);
                scrollOffset = cursorPos - tail.length();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Font font) {
        this.lastFont = font;
        width = getPreferredWidth(font);

        hovered = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        int actualBgColor = hovered ? lighten(bgColor) : bgColor;
        int actualLineColor = hovered ? lighten(lineColor) : lineColor;

        GuiUtils.drawOutlinedRect(graphics, x, y + 1, width, height - 2, actualBgColor, actualLineColor);

        int innerWidth = Math.max(1, width - PADDING);
        updateScroll(font, innerWidth);

        String fullText = text.toString();
        String visibleText = font.plainSubstrByWidth(fullText.substring(scrollOffset), innerWidth);
        int textX = x + PADDING - 2;
        int textY = y + (height - font.lineHeight) / 2 + 1;

        if (focused && hasSelection()) {
            int selStart = Math.max(scrollOffset, Math.min(cursorPos, selectionAnchor));
            int selEnd = Math.min(scrollOffset + visibleText.length(), Math.max(cursorPos, selectionAnchor));
            if (selEnd > selStart) {
                int startX = textX + font.width(fullText.substring(scrollOffset, selStart));
                int endX = textX + font.width(fullText.substring(scrollOffset, selEnd));
                graphics.fill(startX, y + 2, endX, y + height - 2, 0x803399FF);
            }
        }

        graphics.drawString(font, visibleText, textX, textY, textColor);

        if (focused && (cursorTick / 6) % 2 == 0) {
            int clampedCursor = Mth.clamp(cursorPos - scrollOffset, 0, visibleText.length());
            int cursorX = Math.min(x + width - 2, textX + font.width(visibleText.substring(0, clampedCursor)));
            graphics.fill(cursorX, y + 2, cursorX + 1, y + height - 2, 0xFFFFFFFF);
        }
    }

    public int getPreferredWidth(Font font) {
        return font.width("W") * displayChars + PADDING * 2;
    }

    private int lighten(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 40);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 40);
        int b = Math.min(255, (color & 0xFF) + 40);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void tick() {
        cursorTick++;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        boolean inside = mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        focused = inside;
        if (inside && lastFont != null) {
            int relX = (int) mouseX - (x + PADDING - 2);
            String visible = text.substring(scrollOffset);
            String trimmed = lastFont.plainSubstrByWidth(visible, Math.max(0, relX));
            moveCursor(scrollOffset + trimmed.length(), Screen.hasShiftDown());
            cursorTick = 0;
        }
        return inside;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) {
            return false;
        }
        cursorTick = 0;

        if (Screen.isSelectAll(keyCode)) {
            selectionAnchor = 0;
            cursorPos = text.length();
            return true;
        }
        if (Screen.isCopy(keyCode)) {
            if (hasSelection()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getSelectedText());
            }
            return true;
        }
        if (Screen.isCut(keyCode)) {
            if (hasSelection()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(getSelectedText());
                deleteSelection();
            }
            return true;
        }
        if (Screen.isPaste(keyCode)) {
            insertFiltered(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }

        boolean shift = Screen.hasShiftDown();

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            moveCursor(cursorPos - 1, shift);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            moveCursor(cursorPos + 1, shift);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            moveCursor(0, shift);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            moveCursor(text.length(), shift);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            focused = false;
            selectionAnchor = cursorPos;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (cursorPos > 0) {
                text.deleteCharAt(cursorPos - 1);
                moveCursor(cursorPos - 1, false);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (cursorPos < text.length()) {
                text.deleteCharAt(cursorPos);
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!focused) {
            return false;
        }
        cursorTick = 0;
        insertFiltered(String.valueOf(codePoint));
        return true;
    }
    public void setX(int screenX) {
        x = screenX;
    }
    public void setY(int screenY) {
        y = screenY;
    }
}