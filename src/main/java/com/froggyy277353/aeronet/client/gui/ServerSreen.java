package com.froggyy277353.aeronet.client.gui;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.client.keybind.ModKeyBindings;
import com.froggyy277353.aeronet.network.NodesData;
import com.froggyy277353.aeronet.network.servernodespayloads.*;
import com.froggyy277353.aeronet.widgets.*;
import com.froggyy277353.aeronet.widgets.logical.*;
import com.froggyy277353.aeronet.widgets.math.*;
import com.froggyy277353.aeronet.widgets.physics.*;
import com.froggyy277353.aeronet.widgets.physics.display.*;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class ServerSreen extends Screen implements NetworkResultReceiver {
    private static final ResourceLocation LEFT_UPPER_CORNER_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/left_upper_corner.png");
    private static final ResourceLocation RIGHT_UPPER_CORNER_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/right_upper_corner.png");
    private static final ResourceLocation LEFT_DOWN_CORNER_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/left_down_corner.png");
    private static final ResourceLocation RIGHT_DOWN_CORNER_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/right_down_corner.png");
    private static final ResourceLocation LEFT_EDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/left_edge.png");
    private static final ResourceLocation RIGHT_EDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/right_edge.png");
    private static final ResourceLocation UPPER_EDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/upper_edge.png");
    private static final ResourceLocation DOWN_EDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/down_edge.png");
    private static final ResourceLocation CENTER_PIECE_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/background/center_piece.png");
    private static final ResourceLocation LEFT_UPPER_CORNER_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/left_upper_corner_foreground.png");
    private static final ResourceLocation RIGHT_UPPER_CORNER_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/right_upper_corner_foreground.png");
    private static final ResourceLocation LEFT_DOWN_CORNER_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/left_down_corner_foreground.png");
    private static final ResourceLocation RIGHT_DOWN_CORNER_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/right_down_corner_foreground.png");
    private static final ResourceLocation LEFT_EDGE_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/left_edge_foreground.png");
    private static final ResourceLocation RIGHT_EDGE_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/right_edge_foreground.png");
    private static final ResourceLocation UPPER_EDGE_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/upper_edge_foreground.png");
    private static final ResourceLocation DOWN_EDGE_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/down_edge_foreground.png");
    private static final ResourceLocation CENTER_PIECE_FOREGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/foreground/center_piece_foreground.png");
    private static final ResourceLocation LEFT_UPPER_CORNER_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/left_upper_corner_settings.png");
    private static final ResourceLocation RIGHT_UPPER_CORNER_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/right_upper_corner_settings.png");
    private static final ResourceLocation LEFT_DOWN_CORNER_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/left_down_corner_settings.png");
    private static final ResourceLocation RIGHT_DOWN_CORNER_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/right_down_corner_settings.png");
    private static final ResourceLocation LEFT_EDGE_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/left_edge_settings.png");
    private static final ResourceLocation RIGHT_EDGE_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/right_edge_settings.png");
    private static final ResourceLocation UPPER_EDGE_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/upper_edge_settings.png");
    private static final ResourceLocation DOWN_EDGE_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/down_edge_settings.png");
    private static final ResourceLocation CENTER_PIECE_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/gui/control_center/settings/center_piece_settings.png");
    private static final ResourceLocation FIND_ICON = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/icons/find_icon.png");
    private static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/icons/aeronet_logo.png");
    private static final ResourceLocation CURSOR = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/icons/cursor_icon.png");

    public ServerSreen(BlockPos pos, ServerBlockEntity be) {
        super(Component.literal("Server"));
        this.be = be;
        this.pos = pos;
    }
    private ServerBlockEntity be;
    private BlockPos pos;
    private List<Node> nodes = new ArrayList<>();
    private Connections connections = new Connections();
    private boolean isRight = false;
    private boolean isLeft = false;
    private AddNodesMenu addNodesMenu;
    private TextField netWorkNameField;
    private TextField netWorkPasswordField;
    private UUID pendingFromNodeId = null;
    private int pendingFromPort = -1;
    private boolean isConnected;
    private Button connectionButton;
    private Button disconnectionButton;
    private Button networkButton;
    private String networkName;
    private String networkPassword;
    private Component networkStatus = Component.empty();
    private int networkStatusColor = 0xFFFF7777;
    private double zoom = 1.0;
    private double panX;
    private double panY;
    private boolean panning;

    public double canvasX(double screenX) {
        return width / 2 + (screenX - width / 2 - panX) / zoom;
    }

    public double canvasY(double screenY) {
        return height / 2 + (screenY - height / 2 - panY) / zoom;
    }

    private boolean overCanvas(double x, double y) {
        return x >= width / 2 - 188 && x < width / 2 + 188
                && y >= height / 2 - 138 && y < height / 2 + 138;
    }


    @Override
    protected void init()
    {
        super.init();
        nodes = be.getNodes();
        connections = be.getConnections();
        isConnected = be.isConnected();
        addNodesMenu = new AddNodesMenu(font);
        addNodesMenu.setScreen(this);
        addNodesMenu.setFont(font);
        networkName = be.getNetWorkName();
        networkPassword = be.getNetWorkPassword();
        netWorkNameField = new TextField(width/2-190, height/2-115+font.lineHeight, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
        netWorkPasswordField = new TextField(width/2-190, height/2-90+font.lineHeight, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
        connectionButton = new Button(width/2-190, height/2-69, 100, 20, false, Component.translatable("aeronet.network.connect").getString(), "", 0xFF282828, 0xFFFFFF, 0xFF699684);
        disconnectionButton = new Button(width/2-190, height/2-69, 100, 20, false, Component.translatable("aeronet.network.disconnect").getString(),"", 0xFF282828, 0xFFFFFF, 0xFF699684);
        networkButton = new Button(width/2-190, height/2-140, Math.max(font.width(Component.translatable("aeronet.network.open_network")), font.width(Component.translatable("aeronet.network.close_network")))+2, 20, true, Component.translatable("aeronet.network.open_network").getString(), Component.translatable("aeronet.network.close_network").getString(), 0xFF282828, 0xFFFFFF, 0xFF699684);

        netWorkNameField.setText(networkName);
        netWorkPasswordField.setText(networkPassword);
        for (Node node : nodes) {
            node.setScreenX(width / 2 + node.getX());
            node.setScreenY(height / 2 + node.getY());
        }

        GLFW.glfwSetInputMode(
                Minecraft.getInstance().getWindow().getWindow(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_HIDDEN
        );
    }

    @Override
    public void tick()
    {
        super.tick();
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        isConnected = be.isConnected();
        networkName = netWorkNameField.getText();
        networkPassword = netWorkPasswordField.getText();
        graphics.fill(0, 0, width, height, 0xAA000000);
        graphics.enableScissor(width/2-197, height/2-147, width/2+197, height/2+147);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1);
        GuiUtils.drawNineSlice(graphics, LEFT_UPPER_CORNER_TEXTURE, UPPER_EDGE_TEXTURE, RIGHT_UPPER_CORNER_TEXTURE, LEFT_EDGE_TEXTURE, CENTER_PIECE_TEXTURE, RIGHT_EDGE_TEXTURE, LEFT_DOWN_CORNER_TEXTURE, DOWN_EDGE_TEXTURE, RIGHT_DOWN_CORNER_TEXTURE, width / 2 - 200, height / 2-150, 400, 300, 12);
        graphics.pose().popPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,2);

        int canvasMouseX = (int) Math.floor(canvasX(mouseX));
        int canvasMouseY = (int) Math.floor(canvasY(mouseY));
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2 + panX, height / 2 + panY, 0);
        graphics.pose().scale((float) zoom, (float) zoom, 1);
        graphics.pose().translate(-width / 2, -height / 2, 0);
        Map<UUID, Node> nodesById = nodes.stream()
                .collect(Collectors.toMap(Node::getId, node -> node));
        connections.updateConnectedPorts(nodesById);
        for (Node node : nodes) {
            node.setScreenX(width / 2 + node.getX());
            node.setScreenY(height / 2 + node.getY());
        }
        ConnectionsHandler.render(graphics, connections, nodes,
            canvasLeft(), canvasTop(), canvasRight(), canvasBottom());
        if (pendingFromNodeId != null)
        {
            for (Node node : nodes)
            {
                if (node.getId().equals(pendingFromNodeId))
                {
                    int x0 = node.getScreenX() + node.getWidth();
                    int y0 = node.getScreenY() + pendingFromPort * Node.PORT_SPACING + Node.PORT_START_OFFSET + 2;
                    int color = 0xFF646464;
                    for (Node target : nodes) {
                        int port = target.getInputPortAt(canvasMouseX, canvasMouseY, width, height);
                        if (port >= 0 && !Connections.isValid(new Connections.Connection(
                                pendingFromNodeId, pendingFromPort, target.getId(), port), nodesById)) {
                            color = ConnectionsHandler.INVALID_LINE_COLOR;
                            break;
                        }
                    }
                    GuiUtils.drawLine(graphics, x0, y0, canvasMouseX, canvasMouseY, color);
                    break;
                }
            }
        }
        int nodeZ = 0;
        for (Node node : nodes)
        {
            if (!isNodeVisible(node)) {
                continue;
            }
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, nodeZ);
            nodeZ += 2;

            node.render(graphics, font, canvasMouseX, canvasMouseY, partialTick, width, height);

            graphics.flush();
            graphics.pose().popPose();

            if (node.changed())
            {
                CompoundTag tag = node.getExtraData();
                NodesData data = new NodesData(node.getId(), node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getNumInputs(), node.getNumOutputs(), node.getInputArray(), node.getOutputArray(), tag, node.getType());
                PacketDistributor.sendToServer(new UpdateNodesPayload(pos, data));
            }
            if (node.modeChanged())
            {
                connections.removeAllForNode(node.getId());
                PacketDistributor.sendToServer(new UpdateConnectionsPayload(pos, connections.getAllConnections()));
            }
        }




        graphics.pose().popPose();
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        addNodesMenu.render(graphics, mouseX, mouseY, partialTick, width, height);
        if (addNodesMenu.getIsChanged())
        {
            Node node = nodes.getLast();
            CompoundTag tag = node.getExtraData();
            NodesData data = new NodesData(node.getId(), node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getNumInputs(), node.getNumOutputs(), node.getInputArray(), node.getOutputArray(), tag, node.getType());
            PacketDistributor.sendToServer(new UpdateNodesPayload(pos, data));
            addNodesMenu.setIsChanged(false);
        }

        networkButton.render(graphics, mouseX, mouseY, partialTick, font);
        if (networkButton.isPressed())
        {
            netWorkNameField.render(graphics, mouseX, mouseY, partialTick, font);
            netWorkPasswordField.render(graphics, mouseX, mouseY, partialTick, font);
            if (!isConnected)
            {
                connectionButton.render(graphics, mouseX, mouseY, partialTick, font);
            }
            else
            {
                disconnectionButton.render(graphics, mouseX, mouseY, partialTick, font);
            }

            graphics.drawString(font, Component.translatable("aeronet.networkName"), width/2-190, height/2-115, 0xFFFFFF);
            graphics.drawString(font, Component.translatable("aeronet.networkPassword"), width/2-190, height/2-90, 0xFFFFFF);
            graphics.drawWordWrap(font, networkStatus, width / 2 - 190, height / 2 - 44,
                    190, networkStatusColor);
        }

        graphics.flush();
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 250);
        graphics.blit(CURSOR, mouseX, mouseY, 0, 0, 8, 8, 8, 8);

//        graphics.drawString(font,"X:" + String.valueOf(mouseX), mouseX+10, mouseY, 0xFFFFFF);
//        graphics.drawString(font,"Y:" + String.valueOf(mouseY), mouseX, mouseY+10, 0xFFFFFF);
//        graphics.drawString(font,"widthX:" + String.valueOf(width/2+mouseX), mouseX, mouseY+20, 0xFFFFFF);
//        graphics.drawString(font,"heightY:" + String.valueOf(height/2+mouseY), mouseX, mouseY+30, 0xFFFFFF);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.disableScissor();

        GuiUtils.drawNineSlice(graphics, LEFT_UPPER_CORNER_FOREGROUND_TEXTURE, UPPER_EDGE_FOREGROUND_TEXTURE, RIGHT_UPPER_CORNER_FOREGROUND_TEXTURE, LEFT_EDGE_FOREGROUND_TEXTURE, CENTER_PIECE_FOREGROUND_TEXTURE, RIGHT_EDGE_FOREGROUND_TEXTURE, LEFT_DOWN_CORNER_FOREGROUND_TEXTURE, DOWN_EDGE_FOREGROUND_TEXTURE, RIGHT_DOWN_CORNER_FOREGROUND_TEXTURE, width / 2 - 200, height / 2-150, 400, 300, 12);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && overCanvas(mouseX, mouseY)
                && !addNodesMenu.isOverMenu((int) mouseX, (int) mouseY)) {
            panning = true;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) nodes.forEach(Node::clearInputFocus);
        addNodesMenu.mouseClicked(mouseX, mouseY, button);
        networkButton.mouseClicked(mouseX, mouseY, button);
        if (networkButton.isPressed()) {
            netWorkPasswordField.mouseClicked(mouseX, mouseY, button);
            netWorkNameField.mouseClicked(mouseX, mouseY, button);
            if (netWorkNameField.isFocused() || netWorkPasswordField.isFocused()) return true;
            if (!isConnected && connectionButton.isOverButton((int) mouseX, (int) mouseY)) {
                connectionButton.mouseClicked(mouseX, mouseY, button);
                if (!connectionButton.isPressed()) return true;
                if (netWorkNameField.getText().isBlank() || netWorkPasswordField.getText().isBlank()) {
                    receiveNetworkResult(pos, NetworkResult.MISSING_NETWORK_DATA);
                    connectionButton.unPress();
                    return true;
                }
                PacketDistributor.sendToServer(new NetworkPayload(pos, netWorkNameField.getText(),
                        netWorkPasswordField.getText(), NetworkEnum.CONNECT));
                connectionButton.unPress();
                return true;
            }
            if (isConnected && disconnectionButton.isOverButton((int) mouseX, (int) mouseY)) {
                disconnectionButton.mouseClicked(mouseX, mouseY, button);
                if (!disconnectionButton.isPressed()) return true;
                PacketDistributor.sendToServer(new NetworkPayload(pos, networkName,
                        networkPassword, NetworkEnum.DISCONNECT));
                disconnectionButton.unPress();
                return true;
            }
        } else {
            netWorkNameField.setFocused(false);
            netWorkPasswordField.setFocused(false);
        }
        if (!addNodesMenu.isOverMenu((int) mouseX, (int) mouseY))
        {
            if (!overCanvas(mouseX, mouseY)) return false;
            mouseX = canvasX(mouseX);
            mouseY = canvasY(mouseY);
            if (button == 0)
            {
                for (Node node : nodes)
                {
                    int outPort = node.getOutputPortAt(mouseX, mouseY, width, height);
                    if (outPort != -1)
                    {
                        pendingFromNodeId = node.getId();
                        pendingFromPort = outPort;
                        return true;
                    }

                    int inPort = node.getInputPortAt(mouseX, mouseY, width, height);
                    if (inPort != -1)
                    {
                        List<Connections.Connection> existing = new ArrayList<>(connections.getConnectionsTo(node.getId()));
                        for (Connections.Connection c : existing)
                        {
                            if (c.toPort() == inPort)
                            {
                                connections.removeConnection(c.fromId(), c.fromPort(), c.toId(), c.toPort());
                                pendingFromNodeId = c.fromId();
                                pendingFromPort = c.fromPort();
                            }
                        }
                        return true;
                    }
                }

                boolean clickedOnNode = false;
                for (Node node : nodes)
                {
                    if (node.mouseClicked(mouseX, mouseY, button, width, height))
                    {
                        for (Node other : nodes) {
                            other.setSelected(other == node);
                        }
                        clickedOnNode = true;
                        break;
                    }
                }

                if (!clickedOnNode) {
                    for (Node node : nodes) {
                        node.setSelected(false);
                    }
                }
            }
            else
            {
                for (Node node : nodes)
                {
                    node.mouseClicked(mouseX, mouseY, button, width, height);
                }
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                isRight = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            panning = false;
            return true;
        }
        connectionButton.mouseReleased(mouseX, mouseY, button);
        disconnectionButton.mouseReleased(mouseX, mouseY, button);
        netWorkPasswordField.mouseReleased(mouseX, mouseY, button);
        networkButton.mouseReleased(mouseX, mouseY, button);
        netWorkNameField.mouseReleased(mouseX, mouseY, button);
        mouseX = canvasX(mouseX);
        mouseY = canvasY(mouseY);
        if (pendingFromNodeId != null)
        {
            for (Node node : nodes)
            {
                int inPort = node.getInputPortAt(mouseX, mouseY, width, height);
                if (inPort != -1)
                {
                    List<Connections.Connection> existing = new ArrayList<>(connections.getConnectionsTo(node.getId()));
                    for (Connections.Connection c : existing)
                    {
                        if (c.toPort() == inPort)
                        {
                            connections.removeConnection(c.fromId(), c.fromPort(), c.toId(), c.toPort());
                        }
                    }
                    Connections.Connection newConn = new Connections.Connection(
                            pendingFromNodeId, pendingFromPort, node.getId(), inPort);
                    Map<UUID, Node> nodesById = nodes.stream()
                            .collect(Collectors.toMap(Node::getId, n -> n));

                    if (Connections.isValid(newConn, nodesById)) {
                        connections.addConnection(pendingFromNodeId, pendingFromPort, node.getId(), inPort);
                        CompoundTag tag = node.getExtraData();
                        NodesData data = new NodesData(node.getId(), node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getNumInputs(), node.getNumOutputs(), node.getInputArray(), node.getOutputArray(), tag, node.getType());
                        PacketDistributor.sendToServer(new UpdateNodesPayload(pos, data));
                        break;
                    }
                }
            }
            pendingFromNodeId = null;
            pendingFromPort = -1;
            PacketDistributor.sendToServer(new UpdateConnectionsPayload(pos, connections.getAllConnections()));
        }

        for (Node node : nodes)
        {
            node.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (panning && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            panX += dragX;
            panY += dragY;
            return true;
        }
        mouseX = canvasX(mouseX);
        mouseY = canvasY(mouseY);
        dragX /= zoom;
        dragY /= zoom;
        for (Node node : nodes)
        {
            node.mouseDragged(mouseX, mouseY, button, dragX, dragY, width, height);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!overCanvas(mouseX, mouseY) || addNodesMenu.isOverMenu((int) mouseX, (int) mouseY))
            return false;

        double cMouseX = canvasX(mouseX);
        double cMouseY = canvasY(mouseY);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).mouseScrolled(cMouseX, cMouseY, scrollX, scrollY)) {
                return true;
            }
        }

        double oldZoom = zoom;
        zoom = Math.clamp(zoom * Math.pow(1.15, scrollY), 0.25, 3.0);
        panX = mouseX - width / 2 - (mouseX - width / 2 - panX) * zoom / oldZoom;
        panY = mouseY - height / 2 - (mouseY - height / 2 - panY) * zoom / oldZoom;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (addNodesMenu.keyPressed(keyCode, scanCode, modifiers)) return true;

        boolean editing = netWorkNameField.isFocused()
                || netWorkPasswordField.isFocused()
                || nodes.stream().anyMatch(Node::hasFocusedInput);
        if (editing) {
            netWorkPasswordField.keyPressed(keyCode, scanCode, modifiers);
            netWorkNameField.keyPressed(keyCode, scanCode, modifiers);
            for (Node node : nodes) {
                node.keyPressed(keyCode, scanCode, modifiers);
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }
        if (ModKeyBindings.FRAME_NODE.matches(keyCode, scanCode)) {
            for (Node node : nodes) {
                if (!node.isSelected()) continue;
                zoom = Math.clamp(Math.min(336.0 / Math.max(1, node.getWidth()),
                        236.0 / Math.max(1, node.getHeight())), 0.25, 3.0);
                panX = -(node.getX() + node.getWidth() / 2.0) * zoom;
                panY = -(node.getY() + node.getHeight() / 2.0) * zoom;
                break;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DELETE || ModKeyBindings.DELETE_NODE.matches(keyCode, scanCode)) {
            nodes.removeIf(node -> {
                if (node.isSelected()) {
                    connections.removeAllForNode(node.getId());
                    PacketDistributor.sendToServer(new NodeDeletePayload(pos, node.getId()));
                    return true;
                }
                return false;
            });
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_D && Screen.hasShiftDown() && !Screen.hasControlDown() && !Screen.hasAltDown()) {
            Node selectedNode = null;
            for (Node node : nodes) {
                if (node.isSelected()) {
                    selectedNode = node;
                    break;
                }
            }

            if (selectedNode != null) {
                UUID newId = UUID.randomUUID();
                int numInputs = selectedNode.getNumInputs();
                int numOutputs = selectedNode.getNumOutputs();

                List<NodeValue> newInputs = new ArrayList<>(Collections.nCopies(numInputs, NodeValue.EMPTY));
                List<NodeValue> newOutputs = new ArrayList<>(Collections.nCopies(numOutputs, NodeValue.EMPTY));
                NodesData copyData = new NodesData(
                        newId,
                        selectedNode.getX() + 12,
                        selectedNode.getY() + 12,
                        selectedNode.getWidth(),
                        selectedNode.getHeight(),
                        numInputs,
                        numOutputs,
                        newInputs,
                        newOutputs,
                        selectedNode.getExtraData().copy(),
                        selectedNode.getType()
                );

                Node duplicated = GuiUtils.createNodeByData(copyData);
                if (duplicated != null) {
                    selectedNode.setSelected(false);
                    duplicated.setSelected(true);

                    nodes.add(duplicated);
                    PacketDistributor.sendToServer(new UpdateNodesPayload(pos, copyData));
                }
            }
            return true;
        }


        for (Node node : nodes) {
            node.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    private boolean isNodeVisible(Node node) {
        int left = (int) Math.floor(width / 2.0 + panX
                + (node.getX() + width / 2.0 - width / 2.0) * zoom);
        int top = (int) Math.floor(height / 2.0 + panY
                + (node.getY() + height / 2.0 - height / 2.0) * zoom);
        int right = left + (int) Math.ceil(node.getWidth() * zoom);
        int bottom = top + (int) Math.ceil(node.getHeight() * zoom);
        return right >= width / 2 - 188 && left <= width / 2 + 188
                && bottom >= height / 2 - 138 && top <= height / 2 + 138;
    }

    private int canvasLeft() {
        return (int) Math.floor(width / 2.0 + (width / 2.0 - 188 - width / 2.0 - panX) / zoom);
    }

    private int canvasTop() {
        return (int) Math.floor(height / 2.0 + (height / 2.0 - 138 - height / 2.0 - panY) / zoom);
    }

    private int canvasRight() {
        return (int) Math.ceil(width / 2.0 + (width / 2.0 + 188 - width / 2.0 - panX) / zoom);
    }

    private int canvasBottom() {
        return (int) Math.ceil(height / 2.0 + (height / 2.0 + 138 - height / 2.0 - panY) / zoom);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (addNodesMenu.charTyped(codePoint, modifiers)) return true;
        netWorkPasswordField.charTyped(codePoint, modifiers);
        netWorkNameField.charTyped(codePoint, modifiers);
        for (Node node : nodes)
        {
            node.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        GLFW.glfwSetInputMode(
                Minecraft.getInstance().getWindow().getWindow(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_NORMAL
        );
        be.setNodes(nodes);
        be.setConnections(connections);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public BlockPos getPos(){return pos;}

    public void createNode(NodesData data)
    {
        nodes.add(GuiUtils.createNodeByData(data));
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void receiveLinkSync(NodesData data) {
        if (nodes != be.getNodes() || connections != be.getConnections())
            com.froggyy277353.aeronet.network.ServerLinks.applyClient(nodes, connections, data);
    }

    @Override
    public void receiveNetworkResult(BlockPos resultPos, NetworkResult result) {
        if (!pos.equals(resultPos)) return;
        networkStatus = Component.translatable(result.translationKey());
        networkStatusColor = result == NetworkResult.CONNECTED || result == NetworkResult.DISCONNECTED
                ? 0xFF75D175 : 0xFFFF7777;
        if (result == NetworkResult.CONNECTED) isConnected = true;
        if (result == NetworkResult.DISCONNECTED) isConnected = false;
        connectionButton.unPress();
        disconnectionButton.unPress();
    }
}
