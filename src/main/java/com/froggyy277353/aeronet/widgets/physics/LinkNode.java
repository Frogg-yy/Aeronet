package com.froggyy277353.aeronet.widgets.physics;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.client.gui.ServerSreen;
import com.froggyy277353.aeronet.client.gui.TextField;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.network.servernodespayloads.LinkActionPayload;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

public class LinkNode extends Node {
    public static final int MAX_PORTS = 16;
    public static final List<ValueType> TYPES = List.of(ValueType.FLOAT, ValueType.INT,
            ValueType.BOOLEAN, ValueType.STRING, ValueType.LIST);
    private static final ValueType[] VALUE_TYPES = ValueType.values();
    private static final List<String> TYPE_LABELS = List.of("Float", "Int", "Bool", "String", "List");
    private static final Component NETWORK_LABEL = Component.translatable("aeronet.networkName");
    private static final Component PASSWORD_LABEL = Component.translatable("aeronet.networkPassword");
    private static final Map<String, Component> BUTTON_LABELS = Map.of(
            "aeronet.network.connect", Component.translatable("aeronet.network.connect"),
            "aeronet.link.add_input", Component.translatable("aeronet.link.add_input"),
            "aeronet.link.add_output", Component.translatable("aeronet.link.add_output"),
            "aeronet.link.remove_input", Component.translatable("aeronet.link.remove_input"),
            "aeronet.link.remove_output", Component.translatable("aeronet.link.remove_output"));
    private String[] inputLabels = new String[0], outputLabels = new String[0];
    private final List<ValueType> inputTypes = new ArrayList<>();
    private final List<ValueType> outputTypes = new ArrayList<>();
    private final List<UUID> inputKeys = new ArrayList<>(), outputKeys = new ArrayList<>();
    private final TextField networkField = new TextField(0, 0, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private final TextField passwordField = new TextField(0, 0, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
    private String targetName = "", targetPassword = "";
    private UUID peerId;
    private long revision;
    private UUID editor = new UUID(0, 0);
    private String status = "offline";
    private Component statusLabel = Component.translatable("aeronet.link.status.offline");
    private int choosing = -1;

    public LinkNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                    List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 0, 210, 145, List.of(), List.of());
        red = 0.3F; green = 0.65F; blue = 0.8F;
    }

    public String targetName() { return targetName; }
    public String targetPassword() { return targetPassword; }
    public UUID peerId() { return peerId; }
    public List<UUID> inputKeys() { return List.copyOf(inputKeys); }
    public List<UUID> outputKeys() { return List.copyOf(outputKeys); }
    public void pair(String network, String password, UUID peer) {
        targetName = network; targetPassword = password; peerId = peer;
        networkField.setText(network); passwordField.setText(password);
    }
    public boolean status(String value) {
        if (status.equals(value)) return false;
        status = value;
        statusLabel = Component.translatable("aeronet.link.status." + value);
        return true;
    }
    public void clearOutputs() { Collections.fill(outputArray, NodeValue.EMPTY); }
    public boolean editPort(boolean input, boolean add, ValueType type) {
        List<ValueType> ports = input ? inputTypes : outputTypes;
        List<UUID> keys = input ? inputKeys : outputKeys;
        if (add) {
            if (ports.size() >= MAX_PORTS || !TYPES.contains(type)) return false;
            ports.add(type);
            keys.add(UUID.randomUUID());
        } else {
            if (ports.isEmpty()) return false;
            ports.removeLast();
            keys.removeLast();
        }
        revision++;
        editor = id;
        resizePorts();
        return true;
    }
    public int compareSchema(LinkNode other) {
        int c = Long.compare(revision, other.revision);
        return c != 0 ? c : editor.compareTo(other.editor);
    }
    public void mirror(LinkNode other) {
        inputTypes.clear(); inputTypes.addAll(other.outputTypes);
        outputTypes.clear(); outputTypes.addAll(other.inputTypes);
        inputKeys.clear(); inputKeys.addAll(other.outputKeys);
        outputKeys.clear(); outputKeys.addAll(other.inputKeys);
        revision = other.revision; editor = other.editor;
        resizePorts();
    }
    private void resizePorts() {
        numInputs = inputTypes.size(); numOutputs = outputTypes.size();
        setInputArray(inputArray); setOutputArray(outputArray);
        inputLabels = portLabels(inputTypes);
        outputLabels = portLabels(outputTypes);
        width = 210;
        height = Math.max(1, Math.max(numInputs, numOutputs)) * PORT_SPACING + 137;
    }
    private static String[] portLabels(List<ValueType> types) {
        String[] labels = new String[types.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = (i + 1) + ": " + types.get(i);
        return labels;
    }
    @Override public ValueType getInputType(int port) { return inputTypes.get(port); }
    @Override public ValueType getOutputType(int port) { return outputTypes.get(port); }
    @Override public NodeType getType() { return NodeType.LINK; }
    @Override public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("network", targetName); tag.putString("password", targetPassword);
        if (peerId != null) tag.putUUID("peer", peerId);
        tag.putIntArray("inputs", inputTypes.stream().mapToInt(Enum::ordinal).toArray());
        tag.putIntArray("outputs", outputTypes.stream().mapToInt(Enum::ordinal).toArray());
        for (int i = 0; i < inputKeys.size(); i++) tag.putUUID("inputKey" + i, inputKeys.get(i));
        for (int i = 0; i < outputKeys.size(); i++) tag.putUUID("outputKey" + i, outputKeys.get(i));
        tag.putLong("revision", revision); tag.putUUID("editor", editor);
        tag.putString("status", status);
        return tag;
    }
    @Override public void setExtraData(CompoundTag tag) {
        pair(tag.getString("network"), tag.getString("password"), tag.hasUUID("peer") ? tag.getUUID("peer") : null);
        readTypes(inputTypes, tag.getIntArray("inputs"));
        readTypes(outputTypes, tag.getIntArray("outputs"));
        inputKeys.clear(); outputKeys.clear();
        for (int i = 0; i < inputTypes.size(); i++)
            inputKeys.add(tag.hasUUID("inputKey" + i) ? tag.getUUID("inputKey" + i) : UUID.randomUUID());
        for (int i = 0; i < outputTypes.size(); i++)
            outputKeys.add(tag.hasUUID("outputKey" + i) ? tag.getUUID("outputKey" + i) : UUID.randomUUID());
        revision = tag.getLong("revision");
        editor = tag.hasUUID("editor") ? tag.getUUID("editor") : new UUID(0, 0);
        status(tag.contains("status") ? tag.getString("status") : "offline");
        resizePorts();
    }
    private static void readTypes(List<ValueType> list, int[] ids) {
        list.clear();
        for (int type : ids) {
            if (list.size() == MAX_PORTS) break;
            if (type >= 0 && type < VALUE_TYPES.length && TYPES.contains(VALUE_TYPES[type]))
                list.add(VALUE_TYPES[type]);
        }
    }
    private int controlsY() { return screenY + 16 + Math.max(1, Math.max(numInputs, numOutputs)) * PORT_SPACING; }
    @Override public void render(GuiGraphics g, Font font, int mx, int my, float dt, int sw, int sh) {
        super.render(g, font, mx, my, dt, sw, sh);
        g.drawString(font, "Link", screenX + 4, screenY + 2, 0xFFFFFF);
        for (int i = 0; i < numInputs; i++)
            g.drawString(font, inputLabels[i], screenX + 5, screenY + 12 + i * 12, 0xFFFFFF);
        for (int i = 0; i < numOutputs; i++) {
            String label = outputLabels[i];
            g.drawString(font, label, screenX + width - font.width(label) - 5, screenY + 12 + i * 12, 0xFFFFFF);
        }
        int cy = controlsY();
        g.drawString(font, NETWORK_LABEL, screenX + 5, cy, 0xFFFFFF);
        networkField.setX(screenX + 5); networkField.setY(cy + 10);
        networkField.render(g, mx, my, dt, font);
        g.drawString(font, PASSWORD_LABEL, screenX + 5, cy + 24, 0xFFFFFF);
        passwordField.setX(screenX + 5); passwordField.setY(cy + 34);
        passwordField.render(g, mx, my, dt, font);
        drawButton(g, font, "aeronet.network.connect", 5, cy + 49, 200);
        drawButton(g, font, "aeronet.link.add_input", 5, cy + 65, 98);
        drawButton(g, font, "aeronet.link.add_output", 107, cy + 65, 98);
        if (numInputs > 0) drawButton(g, font, "aeronet.link.remove_input", 5, cy + 81, 98);
        if (numOutputs > 0) drawButton(g, font, "aeronet.link.remove_output", 107, cy + 81, 98);
        if (choosing >= 0) {
            for (int i = 0; i < TYPES.size(); i++) {
                GuiUtils.drawOutlinedRect(g, screenX + 5 + i * 40, cy + 98, 40, 14, 0xFF282828, 0xFF699684);
                g.drawString(font, TYPE_LABELS.get(i), screenX + 7 + i * 40, cy + 101, 0xFFFFFF);
            }
        } else g.drawWordWrap(font, statusLabel,
                screenX + 5, cy + 99, 200, status.equals("connected") ? 0x75D175 : 0xFFBB77);
    }
    private void drawButton(GuiGraphics g, Font font, String key, int dx, int sy, int w) {
        GuiUtils.drawOutlinedRect(g, screenX + dx, sy, w, 14, 0xFF282828, 0xFF699684);
        g.drawCenteredString(font, BUTTON_LABELS.get(key), screenX + dx + w / 2, sy + 3, 0xFFFFFF);
    }
    private void send(int action, ValueType type) {
        if (Minecraft.getInstance().screen instanceof ServerSreen screen) {
            PacketDistributor.sendToServer(new LinkActionPayload(screen.getPos(), id, action, type,
                    networkField.getText(), passwordField.getText()));
        }
    }
    @Override public boolean mouseClicked(double mx, double my, int button, int sw, int sh) {
        if (button != 0) return false;
        screenX = sw / 2 + x; screenY = sh / 2 + y;
        boolean field = networkField.mouseClicked(mx, my, button);
        field |= passwordField.mouseClicked(mx, my, button);
        if (field) { isDragging = false; return true; }
        int cy = controlsY();
        double dx = mx - screenX, dy = my - cy;
        if (dx >= 5 && dx < 205) {
            if (dy >= 49 && dy < 63) { send(0, ValueType.FLOAT); choosing = -1; return true; }
            if (dy >= 65 && dy < 79) { choosing = dx < 105 ? 1 : 2; return true; }
            if (dy >= 81 && dy < 95) {
                if (dx < 105 && numInputs > 0) send(3, ValueType.FLOAT);
                if (dx >= 105 && numOutputs > 0) send(4, ValueType.FLOAT);
                choosing = -1; return true;
            }
            if (dy >= 98 && dy < 112 && choosing >= 0) {
                send(choosing, TYPES.get((int) (dx - 5) / 40)); choosing = -1; return true;
            }
        }
        return super.mouseClicked(mx, my, button, sw, sh);
    }
    @Override public boolean hasFocusedInput() { return networkField.isFocused() || passwordField.isFocused(); }
    @Override public void clearInputFocus() { networkField.setFocused(false); passwordField.setFocused(false); }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        return networkField.keyPressed(key, scan, mods) | passwordField.keyPressed(key, scan, mods);
    }
    @Override public boolean charTyped(char c, int mods) {
        return networkField.charTyped(c, mods) | passwordField.charTyped(c, mods);
    }
}
