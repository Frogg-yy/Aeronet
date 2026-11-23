package com.froggyy277353.aeronet.client.gui;

import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.network.NetworkManager;
import com.froggyy277353.aeronet.network.servernodespayloads.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;


public class AnalogDevicesScreen extends Screen implements NetworkResultReceiver {
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
    private static final ResourceLocation CURSOR = ResourceLocation.fromNamespaceAndPath("aeronet", "textures/icons/cursor_icon.png");

    private NetworkDevice be;
    private BlockPos pos;
    private boolean isConnected;
    private String networkName = "";
    private String networkPassword = "";
    private String deviceName = "";
    private TextField nameField;
    private TextField passwordField;
    private TextField deviceNameField;
    private Button connectButton;
    private Button disconnectButton;
    private Component networkStatus = Component.empty();
    private int networkStatusColor = 0xFFFF7777;

    public AnalogDevicesScreen(BlockPos pos, NetworkDevice be) {
        super(Component.literal("Server"));
        this.be = be;
        this.pos = pos;
    }

    @Override
    protected void init()
    {
        super.init();
        this.networkName = be.getNetwork().getNetworkName();
        this.networkPassword = be.getNetwork().getNetworkPassword();
        this.deviceName = be.getNetwork().getDeviceName();
        this.isConnected = be.getNetwork().isConnected();
        nameField = new TextField(width/2-(font.width("WWWWWWWWWWWWW")/2), height/2-16, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
        passwordField = new TextField(width/2-(font.width("WWWWWWWWWWWWW")/2), height/2+8, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
        deviceNameField = new TextField(width/2-(font.width("WWWWWWWWWWWWW")/2), height/2+32, 13, 12, 0xFF282828, 0xFFFFFF, 0xFF699684);
        connectButton = new Button(width / 2 - 50, height / 2 + 50, 100, 20, false,
                Component.translatable("aeronet.network.connect").getString(), "",
                0xFF282828, 0xFFFFFF, 0xFF699684);
        disconnectButton = new Button(width / 2 - 50, height / 2 + 50, 100, 20, false,
                Component.translatable("aeronet.network.disconnect").getString(), "",
                0xFF282828, 0xFFFFFF, 0xFF699684);

        nameField.setText(networkName);
        passwordField.setText(networkPassword);
        deviceNameField.setText(deviceName);
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }

    @Override
    public void tick()
    {
        super.tick();
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        isConnected = be.getNetwork().isConnected();
        graphics.fill(0, 0, width, height, 0xAA000000);
        graphics.enableScissor(width/2-100, height/2-100, width/2+100, height/2+100);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1);
        GuiUtils.drawNineSlice(graphics, LEFT_UPPER_CORNER_TEXTURE, UPPER_EDGE_TEXTURE, RIGHT_UPPER_CORNER_TEXTURE, LEFT_EDGE_TEXTURE, CENTER_PIECE_TEXTURE, RIGHT_EDGE_TEXTURE, LEFT_DOWN_CORNER_TEXTURE, DOWN_EDGE_TEXTURE, RIGHT_DOWN_CORNER_TEXTURE, width / 2 - 100, height / 2 - 100, 200, 200, 12);
        graphics.pose().popPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,2);

        nameField.render(graphics, mouseX, mouseY, partialTick, font);
        passwordField.render(graphics, mouseX, mouseY, partialTick, font);
        deviceNameField.render(graphics, mouseX, mouseY, partialTick, font);
        if (!isConnected)
        {
            connectButton.render(graphics, mouseX, mouseY, partialTick, font);
        }
        else
        {
            disconnectButton.render(graphics, mouseX, mouseY, partialTick, font);
        }
        graphics.drawCenteredString(font, Component.translatable("aeronet.networkName"), width/2, height/2-24, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("aeronet.networkPassword"), width/2, height/2, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("aeronet.networkDevice"), width/2, height/2+24, 0xFFFFFF);
        networkName = nameField.getText();
        networkPassword = passwordField.getText();
        deviceName = deviceNameField.getText();
        graphics.drawWordWrap(font, networkStatus, width / 2 - 90, height / 2 + 74,
                180, networkStatusColor);
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 10);
        graphics.blit(CURSOR, mouseX, mouseY, 0, 0, 8, 8, 8, 8);


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiUtils.drawNineSlice(graphics, LEFT_UPPER_CORNER_FOREGROUND_TEXTURE, UPPER_EDGE_FOREGROUND_TEXTURE, RIGHT_UPPER_CORNER_FOREGROUND_TEXTURE, LEFT_EDGE_FOREGROUND_TEXTURE, CENTER_PIECE_FOREGROUND_TEXTURE, RIGHT_EDGE_FOREGROUND_TEXTURE, LEFT_DOWN_CORNER_FOREGROUND_TEXTURE, DOWN_EDGE_FOREGROUND_TEXTURE, RIGHT_DOWN_CORNER_FOREGROUND_TEXTURE, width / 2 - 100, height / 2 - 100, 200, 200, 12);
        graphics.pose().popPose();
        graphics.disableScissor();
        RenderSystem.disableBlend();

    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        nameField.mouseClicked(mouseX, mouseY, button);
        passwordField.mouseClicked(mouseX, mouseY, button);
        deviceNameField.mouseClicked(mouseX, mouseY, button);

        if (!isConnected && connectButton.isOverButton((int) mouseX, (int) mouseY)) {
            connectButton.mouseClicked(mouseX, mouseY, button);
            if (connectButton.isPressed()) {
                connectToNetwork();
                connectButton.unPress();
            }
            return true;
        }

        if (isConnected && disconnectButton.isOverButton((int) mouseX, (int) mouseY)) {
            disconnectButton.mouseClicked(mouseX, mouseY, button);
            if (disconnectButton.isPressed()) {
                disconnectFromNetwork();
                disconnectButton.unPress();
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void connectToNetwork() {
        if (networkName.isBlank() || networkPassword.isBlank()) {
            receiveNetworkResult(pos, NetworkResult.MISSING_NETWORK_DATA);
            return;
        }
        if (deviceName.isBlank()) {
            receiveNetworkResult(pos, NetworkResult.MISSING_DEVICE_NAME);
            return;
        }
        List<ServerBlockEntity> servers = NetworkManager.getInstance().getServersInRadius(pos, 50);

        for (ServerBlockEntity server : servers) {
            if (!networkName.equals(server.getNetWorkName())
                    || !networkPassword.equals(server.getNetWorkPassword())) {
                continue;
            }

            PacketDistributor.sendToServer(new AnalogDeviceNetworkPayload(
                    pos,
                    networkName,
                    networkPassword,
                    deviceName,
                    server.getBlockPos(),
                    NetworkEnum.CONNECT
            ));
            return;
        }
        receiveNetworkResult(pos, NetworkResult.NETWORK_NOT_FOUND);
    }

    private void disconnectFromNetwork() {
        BlockPos serverPos = be.getNetwork().getServerPos();
        if (serverPos == null) {
            receiveNetworkResult(pos, NetworkResult.NETWORK_NOT_FOUND);
            return;
        }
        PacketDistributor.sendToServer(new AnalogDeviceNetworkPayload(
                pos, networkName, networkPassword, deviceName, serverPos, NetworkEnum.DISCONNECT
        ));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        nameField.mouseReleased(mouseX, mouseY, button);
        passwordField.mouseReleased(mouseX, mouseY, button);
        deviceNameField.mouseReleased(mouseX, mouseY, button);
        connectButton.mouseReleased(mouseX, mouseY, button);
        disconnectButton.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        nameField.keyPressed(keyCode, scanCode, modifiers);
        passwordField.keyPressed(keyCode, scanCode, modifiers);
        deviceNameField.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        nameField.charTyped(codePoint, modifiers);
        passwordField.charTyped(codePoint, modifiers);
        deviceNameField.charTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        GLFW.glfwSetInputMode(
                Minecraft.getInstance().getWindow().getWindow(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_NORMAL
        );
        super.onClose();
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void receiveNetworkResult(BlockPos resultPos, NetworkResult result) {
        if (!pos.equals(resultPos)) return;
        networkStatus = Component.translatable(result.translationKey());
        networkStatusColor = result == NetworkResult.CONNECTED || result == NetworkResult.DISCONNECTED
                ? 0xFF75D175 : 0xFFFF7777;
        if (result == NetworkResult.CONNECTED) isConnected = true;
        if (result == NetworkResult.DISCONNECTED) isConnected = false;
        connectButton.unPress();
        disconnectButton.unPress();
    }
}
