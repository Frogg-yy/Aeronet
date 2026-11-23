package com.froggyy277353.aeronet.widgets.physics.surface_scanner;

import com.froggyy277353.aeronet.client.gui.CheckBox;
import com.froggyy277353.aeronet.client.gui.NumberField;
import com.froggyy277353.aeronet.client.gui.TextField;
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

public class SurfaceScannerNode extends Node {
    private String linkName = "";
    private boolean wasFocused = false;
    private boolean ignoreTrans = false;
    private float angle = 0;
    private int fov = 45;
    private int size = 16;
    private boolean on = false;
    private NumberField numberFields[] = {
            new NumberField(0, 0, 4, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684),
            new NumberField(0, 0, 4, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684),
            new NumberField(0, 0, 4, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684)
    };
    private String numberText[] = {
            Component.translatable("aeronet.surfacescanner.angle").getString(),
            Component.translatable("aeronet.surfacescanner.fov").getString(),
            Component.translatable("aeronet.surfacescanner.size").getString()
    };
    private boolean numberFieldWereFocused[]={false, false, false};
    private TextField linkNameField = new TextField(0, 0, 8, 12, 0xFF282828, 0xFFFFFFFF, 0xFF699684);
    private CheckBox ignoreTransBox = new CheckBox(0, 0, 10);
    private CheckBox onBox = new CheckBox(0, 0, 10);
    public SurfaceScannerNode(UUID id, int x, int y, int inputs, int outputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 5, 1, 37+41, PORT_START_OFFSET + 6 * PORT_SPACING, inputArray, outputArray);
        red = 0.298F;
        green = 0.686F;
        blue = 0.314F;
        numberFields[0].setValue(angle);
        numberFields[1].setValue(fov);
        numberFields[2].setValue(size);
        ignoreTransBox.setChecked(ignoreTrans);
        onBox.setChecked(on);
    }
    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        this.width = Math.max(font.width(Component.translatable("aeronet.node.surface_scanner")) + 4, 10 * font.width("W"));
        graphics.drawString(font, Component.translatable("block.aeronet.surface_scanner"), screenX+2, screenY+2, 0xFFFFFF);
        graphics.drawString(font, Component.translatable(  "aeronet.node.name"), screenX+3, screenY+PORT_START_OFFSET + PORT_SPACING * 5, 0XFFFFFF);
        linkNameField.setX(screenX+4+font.width(Component.translatable(  "aeronet.node.name")));
        linkNameField.setY(screenY+PORT_START_OFFSET+PORT_SPACING * 5);
        for (int i = 0; i < 3; i++)
        {
            numberFields[i].setX(screenX+4+font.width(numberText[i]));
            numberFields[i].setY(screenY+PORT_START_OFFSET+PORT_SPACING * i);
        }


        linkNameField.render(graphics, mouseX, mouseY, partialTick, font);
        if (!isInputPortConnected(0))
        {
            graphics.drawString(font, Component.translatable("aeronet.surfacescanner.angle"), screenX+3, screenY+PORT_START_OFFSET, 0xFFFFFF);
            numberFields[0].render(graphics, mouseX, mouseY, partialTick, font);
            angle = numberFields[0].getValue();
        }
        if (!isInputPortConnected(1))
        {
            graphics.drawString(font, Component.translatable("aeronet.surfacescanner.fov"), screenX+3, screenY+PORT_START_OFFSET+PORT_SPACING, 0xFFFFFF);
            numberFields[1].render(graphics, mouseX, mouseY, partialTick, font);
            fov = (int) numberFields[1].getValue();
        }
        if (!isInputPortConnected(2))
        {
            graphics.drawString(font, Component.translatable("aeronet.surfacescanner.size"), screenX+3, screenY+PORT_START_OFFSET+PORT_SPACING * 2, 0xFFFFFF);
            numberFields[2].render(graphics, mouseX, mouseY, partialTick, font);
            size = (int) numberFields[2].getValue();
        }
        if (!isInputPortConnected(3))
        {
            ignoreTransBox.setX(screenX+3+font.width(Component.translatable("aeronet.surfacescanner.ignore_trans")));
            ignoreTransBox.setY(screenY+PORT_START_OFFSET+PORT_SPACING * 3);
            graphics.drawString(font, Component.translatable("aeronet.surfacescanner.ignore_trans"), screenX+3, screenY+PORT_START_OFFSET+PORT_SPACING * 3, 0xFFFFFF);
            ignoreTransBox.render(graphics, mouseX, mouseY, partialTick, font);
            if (ignoreTransBox.isChecked() != ignoreTrans)
            {
                changed = true;
                ignoreTrans = ignoreTransBox.isChecked();
            }
        }
        if (!isInputPortConnected(4))
        {
            onBox.setX(screenX+3+font.width(Component.translatable("aeronet.node.on")));
            onBox.setY(screenY+PORT_START_OFFSET+PORT_SPACING * 4);
            graphics.drawString(font, Component.translatable("aeronet.node.on"), screenX+3, screenY+PORT_START_OFFSET+PORT_SPACING * 4, 0xFFFFFF);
            onBox.render(graphics, mouseX, mouseY, partialTick, font);
            if (onBox.isChecked() != on)
            {
                changed = true;
                on = onBox.isChecked();
            }
        }
        for (int i = 0; i < 3; i++)
        {
            boolean isNumberFocesedNow = numberFields[i].isFocused();
            if (numberFieldWereFocused[i] && !isNumberFocesedNow)
            {
                changed = true;
            }
            numberFieldWereFocused[i] = isNumberFocesedNow;
        }

        boolean isFocusedNow = linkNameField.isFocused();
        linkName = linkNameField.getText();
        if (wasFocused && !isFocusedNow) {
            changed = true;
        }
        wasFocused = isFocusedNow;
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
        if (isInputPortConnected(0) && !inputArray.isEmpty())
        {
            angle = inputArray.getFirst().asFloat();
        }
        if (isInputPortConnected(1) && !inputArray.isEmpty())
        {
            fov = inputArray.get(1).asInt();
        }
        if (isInputPortConnected(2) && !inputArray.isEmpty())
        {
            size = inputArray.get(2).asInt();
        }
        if (isInputPortConnected(3) && !inputArray.isEmpty())
        {
            ignoreTrans = inputArray.get(3).asBoolean();
        }
        if (isInputPortConnected(4) && !inputArray.isEmpty())
        {
            on = inputArray.get(4).asBoolean();
        }
    }

    public String getLinkName(){return linkName;}
    public void setExtraData(CompoundTag tag) {
        this.linkName = tag.getString("linkName");
        this.angle = tag.getFloat("angle");
        this.fov = tag.getInt("fov");
        this.size = tag.getInt("size");
        this.ignoreTrans = tag.getBoolean("ignoreTrans");
        this.on = tag.getBoolean("on");
        linkNameField.setText(linkName);
        numberFields[0].setValue(angle);
        numberFields[1].setValue(fov);
        numberFields[2].setValue(size);
        ignoreTransBox.setChecked(ignoreTrans);
        onBox.setChecked(on);
    }
    public CompoundTag getExtraData()
    {
        CompoundTag tag= new CompoundTag();
        tag.putString("linkName", linkName);
        tag.putFloat("angle", angle);
        tag.putInt("fov", fov);
        tag.putInt("size", size);
        tag.putBoolean("ignoreTrans", ignoreTrans);
        tag.putBoolean("on", on);
        return tag;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int width, int height) {
        boolean result = super.mouseClicked(mouseX, mouseY, button, width, height);
        linkNameField.mouseClicked(mouseX, mouseY, button);
        for (int i = 0; i < 3; i ++)
        {
            numberFields[i].mouseClicked(mouseX, mouseY, button);
        }
        ignoreTransBox.mouseClicked(mouseX, mouseY, button);
        onBox.mouseClicked(mouseX, mouseY, button);
        return result;
    }
    public boolean isOn(){return on;}
    public float getAngle(){
        return angle;}
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        linkNameField.mouseReleased(mouseX, mouseY, button);
        for (int i = 0; i < 3; i ++)
        {
            numberFields[i].mouseReleased(mouseX, mouseY, button);
        }
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
        linkNameField.keyPressed(keyCode, scanCode, modifiers);
        for (int i = 0; i < 3; i ++)
        {
            numberFields[i].keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        super.charTyped(codePoint, modifiers);
        linkNameField.charTyped(codePoint, modifiers);
        for (int i = 0; i < 3; i ++)
        {
            numberFields[i].charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public NodeType getType() {
        return NodeType.SCANNER;
    }
    @Override
    public boolean hasFocusedInput() {
        boolean isNumberFieldFocused = false;
        for (int i = 0; i < 3; i ++)
        {
            isNumberFieldFocused = numberFields[i].isFocused();
            if (isNumberFieldFocused)
            {
                break;
            }
        }
        return linkNameField.isFocused() || isNumberFieldFocused;
    }

    @Override
    public void clearInputFocus() {
        linkNameField.setFocused(false);
        for(int i = 0; i < 3; i++)
        {
            numberFields[i].setFocused(false);
        }
    }
    @Override
    public ValueType getInputType(int port) {
        if (port == 0 || port == 1 || port == 2) {
            return ValueType.FLOAT;
        }
        if (port == 3 || port == 4) {
            return ValueType.BOOLEAN;
        }
        return ValueType.NULL;
    }


    @Override
    public ValueType getOutputType(int port) {
        if (port == 0) {
            return ValueType.LIST;
        }
        return ValueType.NULL;
    }

    public void receiveSignal(Object scan)
    {
        setOutputValue(0, NodeValue.of(scan));
    }

    public float getFOV() {
        return fov;
    }
    public boolean isIgnoreTrans(){return ignoreTrans;}
    public int getSize() {
        return size;
    }
}
