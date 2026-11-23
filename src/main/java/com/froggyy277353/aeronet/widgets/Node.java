package com.froggyy277353.aeronet.widgets;

import com.froggyy277353.aeronet.client.gui.GUIElement;
import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public abstract class Node
{
    protected final ResourceLocation NODE_LEFT_UPPER_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_upper_left_corner.png");
    protected final ResourceLocation NODE_RIGHT_UPPER_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_upper_right_corner.png");
    protected final ResourceLocation NODE_LEFT_DOWN_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_down_left_corner.png");
    protected final ResourceLocation NODE_RIGHT_DOWN_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_down_right_corner.png");
    protected final ResourceLocation NODE_UPPER_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_upper_edge.png");
    protected final ResourceLocation NODE_DOWN_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_down_edge.png");
    protected final ResourceLocation NODE_LEFT_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_left_edge.png");
    protected final ResourceLocation NODE_RIGHT_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_right_edge.png");
    protected final ResourceLocation NODE_CENTER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_center.png");
    protected final ResourceLocation NODE_OUTLINE_LEFT_UPPER_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_upper_left_corner_outline.png");
    protected final ResourceLocation NODE_OUTLINE_RIGHT_UPPER_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_upper_right_corner_outline.png");
    protected final ResourceLocation NODE_OUTLINE_LEFT_DOWN_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_down_left_corner_outline.png");
    protected final ResourceLocation NODE_OUTLINE_RIGHT_DOWN_CORNER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_down_right_corner_outline.png");
    protected final ResourceLocation NODE_OUTLINE_UPPER_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_upper_edge_outline.png");
    protected final ResourceLocation NODE_OUTLINE_DOWN_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_down_edge_outline.png");
    protected final ResourceLocation NODE_OUTLINE_LEFT_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_left_edge_outline.png");
    protected final ResourceLocation NODE_OUTLINE_RIGHT_EDGE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_right_edge_outline.png");
    protected final ResourceLocation NODE_OUTLINE_CENTER = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/node/node_center_outline.png");
    protected final ResourceLocation INPUT_OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/icons/output_input_icon.png");

    protected int x;
    protected int y;
    protected int mouseXOffset;
    protected int mouseYOffset;
    protected int width;
    protected int height;
    protected UUID id;
    protected int numInputs;
    protected int numOutputs;
    protected List<NodeValue> inputArray;
    protected List<NodeValue> outputArray;
    protected boolean isDragging = false;
    protected int screenX;
    protected int screenY;
    protected float red;
    protected float green;
    protected float blue;
    protected boolean changed;
    protected int lastX;
    protected int lastY;
    protected boolean selected;
    protected boolean modeChanged;

    public static final int PORT_SPACING = 12;
    public static final int PORT_START_OFFSET = 12;

    public Node(UUID id, int x, int y, int numInputs, int numOutputs, int width, int height, List<NodeValue> inputArray, List<NodeValue> outputArray)
    {
        this.id = id;
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        setOutputArray(outputArray);
        setInputArray(inputArray);
        this.height = height;
        this.width = width;
        this.changed = false;
    }

    public void setInputsOutputs(int numInputs, int numOutputs) {
        if (this.numInputs == numInputs && this.numOutputs == numOutputs) {
            return;
        }

        this.numInputs = numInputs;
        this.numOutputs = numOutputs;

        this.inputArray = resizeValues(this.inputArray, numInputs);
        this.outputArray = resizeValues(this.outputArray, numOutputs);
    }

    public List<NodeValue> getInputArray()
    {
        return inputArray;
    }
    public List<NodeValue> getOutputArray()
    {
        return outputArray;
    }
    public int getNumInputs()
    {
        return numInputs;
    }
    public int getNumOutputs()
    {
        return numOutputs;
    }

    public ValueType getInputType(int port) {
        return ValueType.FLOAT;
    }

    public ValueType getOutputType(int port) {
        return ValueType.FLOAT;
    }

    public boolean acceptsInputType(int port, ValueType type) {
        ValueType expected = getInputType(port);
        if (expected == type) {
            return true;
        }
        if ((expected == ValueType.FLOAT && type == ValueType.INT)
                || (expected == ValueType.INT && type == ValueType.FLOAT)) {
            return true;
        }
        if (expected == ValueType.BOOLEAN && (type == ValueType.INT || type == ValueType.FLOAT)) {
            return true;
        }
        return false;
    }

    public NodeValue convertInputValue(int port, NodeValue value) {
        if (value == null || value.isEmpty()) {
            return NodeValue.EMPTY;
        }
        ValueType expected = getInputType(port);
        if (expected == ValueType.BOOLEAN && (value.type() == ValueType.INT || value.type() == ValueType.FLOAT)) {
            return NodeValue.of(value.asFloat() != 0.0f);
        }
        if (expected == ValueType.INT && value.type() == ValueType.FLOAT) {
            return NodeValue.of(Math.round(value.asFloat()));
        }
        if (expected == ValueType.FLOAT && value.type() == ValueType.INT) {
            return NodeValue.of((float) value.asInt());
        }
        return value;
    }



    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight)
    {
        screenX = screenWidth / 2 + x;
        screenY = screenHeight / 2 + y;
        //graphics.drawString(font, id.toString().substring(0, 5), screenX, screenY-10, 0xFFFFFF);
        GuiUtils.drawNineSlice(graphics, NODE_LEFT_UPPER_CORNER, NODE_UPPER_EDGE, NODE_RIGHT_UPPER_CORNER, NODE_LEFT_EDGE, NODE_CENTER, NODE_RIGHT_EDGE, NODE_LEFT_DOWN_CORNER, NODE_DOWN_EDGE, NODE_RIGHT_DOWN_CORNER, screenX, screenY, width, height, 12);
        graphics.setColor(red, green, blue, 1.0F);
        GuiUtils.drawThreeSlice(graphics, NODE_LEFT_UPPER_CORNER, NODE_UPPER_EDGE, NODE_RIGHT_UPPER_CORNER,screenX, screenY, width, 12, 12);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (selected)
        {
            GuiUtils.drawNineSlice(graphics, NODE_OUTLINE_LEFT_UPPER_CORNER, NODE_OUTLINE_UPPER_EDGE, NODE_OUTLINE_RIGHT_UPPER_CORNER, NODE_OUTLINE_LEFT_EDGE, NODE_OUTLINE_CENTER, NODE_OUTLINE_RIGHT_EDGE, NODE_OUTLINE_LEFT_DOWN_CORNER, NODE_OUTLINE_DOWN_EDGE, NODE_OUTLINE_RIGHT_DOWN_CORNER, screenX, screenY, width, height, 12);
        }
        if (numInputs > 0)
        {
            for (int i = 0; i < numInputs; i++)
            {
                graphics.blit(INPUT_OUTPUT_ICON, screenX-2, screenY+i*PORT_SPACING+PORT_START_OFFSET, 0, 0, 5, 5, 5, 5);
            }

        }
        if (numOutputs > 0)
        {
            for (int i = 0; i < numOutputs; i++)
            {
                graphics.blit(INPUT_OUTPUT_ICON, screenX+width-3, screenY+i*PORT_SPACING+PORT_START_OFFSET, 0, 0, 5, 5, 5, 5);
            }

        }

    }
    public void tick()
    {

    }

    public int getScreenX(){return screenX;}
    public int getScreenY(){return screenY;}

    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (button != 0) {
            return false;
        }

        int screenX = screenWidth / 2 + x;
        int screenY = screenHeight / 2 + y;

        if (mouseX >= screenX && mouseX <= screenX + width &&
                mouseY >= screenY && mouseY <= screenY + height) {

            isDragging = true;
            mouseXOffset = (int) mouseX - screenX;
            mouseYOffset = (int) mouseY - screenY;

            return true;
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int screenWidth, int screenHeight) {
            if (!isDragging || button != 0) {
                return false;
            }

            x = (int) mouseX - mouseXOffset - screenWidth / 2;
            y = (int) mouseY - mouseYOffset - screenHeight / 2;

            if (x != lastX || y != lastY) {
                changed = true;
                lastX = x;
                lastY = y;
            }


        return true;
    }


    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    public boolean hasFocusedInput() { return false; }

    public void clearInputFocus() {}

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public UUID getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x){this.x = x;}
    public void setY(int y){this.y = y;}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }
    public void setExtraData(CompoundTag tag) {
    }
    public abstract NodeType getType();

    public void setInputArray(List<NodeValue> inputArray) {
        this.inputArray = resizeValues(inputArray, numInputs);
    }

    public void setOutputArray(List<NodeValue> outputArray) {
        this.outputArray = resizeValues(outputArray, numOutputs);
    }

    private static List<NodeValue> resizeValues(List<NodeValue> values, int size) {
        List<NodeValue> result = new ArrayList<>(size);
        for (int port = 0; port < size; port++) {
            result.add(port < values.size() ? values.get(port) : NodeValue.EMPTY);
        }
        return result;
    }
    public void setOutputValue(int index, NodeValue value)
    {
        outputArray.set(index, value);
    }
    public void setInputValue(int index, NodeValue value)
    {
        inputArray.set(index, value);
    }

    public int getOutputPortX(int screenWidth) {
        return screenWidth / 2 + x + width - 2;
    }

    public int getOutputPortY(int screenHeight, int index) {
        return screenHeight / 2 + y + index * PORT_SPACING + PORT_START_OFFSET;
    }

    public int getInputPortX(int screenWidth) {
        return screenWidth / 2 + x - 2;
    }

    public int getInputPortY(int screenHeight, int index) {
        return screenHeight / 2 + y + index * PORT_SPACING + PORT_START_OFFSET;
    }

    public int getOutputPortAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        int px = getOutputPortX(screenWidth);
        for (int i = 0; i < numOutputs; i++) {
            int py = getOutputPortY(screenHeight, i);
            if (mouseX >= px && mouseX <= px + 5 && mouseY >= py && mouseY <= py + 5) {
                return i;
            }
        }
        return -1;
    }

    public int getInputPortAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        int px = getInputPortX(screenWidth);
        for (int i = 0; i < numInputs; i++) {
            int py = getInputPortY(screenHeight, i);
            if (mouseX >= px && mouseX <= px + 5 && mouseY >= py && mouseY <= py + 5) {
                return i;
            }
        }
        return -1;
    }
    public boolean modeChanged() {
        if (modeChanged) {
            modeChanged = false;
            return true;
        }
        return false;
    }
    public boolean changed()
    {
        if (changed)
        {
            changed = false;
            return true;
        }
        return false;

    }
    protected Set<Integer> connectedInputPorts = new HashSet<>();
    protected Set<Integer> connectedOutputPorts = new HashSet<>();

    public void setConnectedPorts(Set<Integer> connectedInputs, Set<Integer> connectedOutputs) {
        this.connectedInputPorts = connectedInputs;
        this.connectedOutputPorts = connectedOutputs;
    }

    public boolean isInputPortConnected(int index) {
        return connectedInputPorts.contains(index);
    }
    public int getLastInputPortConnected(){
        return connectedInputPorts.size();
    }

    public boolean isOutputPortConnected(int index) {
        return connectedOutputPorts.contains(index);
    }
    protected boolean checkFocusLost(GUIElement field, boolean wasFocusedPrev) {
        boolean isFocusedNow = field.isFocused();
        return wasFocusedPrev && !isFocusedNow;
    }
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setScreenX(int screenX) {
        this.screenX = screenX;
    }

    public void setScreenY(int screenY) {
        this.screenY = screenY;
    }
}
