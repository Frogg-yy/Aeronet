package com.froggyy277353.aeronet.block.entity.server;



import com.froggyy277353.aeronet.block.DisplayBlock;
import com.froggyy277353.aeronet.block.entity.DisplayBlockEntity;
import com.froggyy277353.aeronet.block.entity.NetworkDevice;
import com.froggyy277353.aeronet.block.ServerBlock;
import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.item.ServerModuleItem;
import com.froggyy277353.aeronet.network.*;
import com.froggyy277353.aeronet.network.servernodespayloads.NetworkEnum;
import com.froggyy277353.aeronet.network.servernodespayloads.NetworkResult;
import com.froggyy277353.aeronet.network.servernodespayloads.NodesSyncPayload;
import com.froggyy277353.aeronet.network.servernodespayloads.UpdateConnectionsPayload;
import com.froggyy277353.aeronet.registry.ModBlockEntities;

import com.froggyy277353.aeronet.widgets.*;
import com.froggyy277353.aeronet.widgets.logical.*;
import com.froggyy277353.aeronet.widgets.math.*;
import com.froggyy277353.aeronet.widgets.physics.*;
import com.froggyy277353.aeronet.widgets.physics.display.*;
import com.froggyy277353.aeronet.widgets.physics.display.Canvas;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.SurfaceScannerNode;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.*;


public class ServerBlockEntity extends BlockEntity {
    private String netWorkName = "";
    private String netWorkPassword = "";
    private boolean isConnected = false;
    private List<Node> nodes = new ArrayList<>();
    private List<BlockPos> devices = new ArrayList<>();
    private Connections connections = new Connections();
    private int tickCounter = 0;
    private final ItemStackHandler modules = new ItemStackHandler(4)
    {
        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return  stack.getItem() instanceof ServerModuleItem;
        }
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if (level != null)
            {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    };

    public ServerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SERVER_BLOCK_ENTITY.get(), pos, state);
    }

    public List<Node> getNodes() {
        return nodes;
    }
    public void setNodes(List<Node> nodes)
    {
        this.nodes = nodes;
    }
    public Connections getConnections(){return connections;}
    public void setConnections(Connections connections){this.connections = connections;}

    public static void tick(Level level, BlockPos pos, BlockState state, ServerBlockEntity be) {
        Map<UUID, Node> nodesById = new HashMap<>();
        be.tickCounter++;
        for (Node node : be.nodes) {
            nodesById.put(node.getId(), node);
        }

        be.updateSensorNodes(level);
        ServerLinks.tick(be);
        be.connections.transferValues(nodesById);
        for (Node node : be.nodes)
        {
            node.tick();
            if (node.modeChanged())
            {
                be.connections.removeAllForNode(node.getId());
                PacketDistributor.sendToServer(new UpdateConnectionsPayload(pos, be.connections.getAllConnections()));
            }
        }

        List<NodesSyncPayload.NodeSyncData> syncNodes = new ArrayList<>();

        for (Node node : be.nodes) {

            syncNodes.add(
                    new NodesSyncPayload.NodeSyncData(
                            node.getId(),
                            new ArrayList<>(node.getInputArray()),
                            new ArrayList<>(node.getOutputArray())
                    )
            );
        }

        PacketDistributor.sendToPlayersTrackingChunk(
                (ServerLevel) level,
                new ChunkPos(pos),
                new NodesSyncPayload(pos, syncNodes)
        );

        Set<BlockPos> processedDisplays = new HashSet<>();
        for (BlockPos devicePos : be.devices) {
            if (level.getBlockEntity(devicePos) instanceof NetworkDevice device) {
                String deviceName = device.getNetwork().getDeviceName();
                for (Node node : be.nodes) {
                    if (node instanceof DisplayNode displayNode && displayNode.getLinkName().equals(deviceName)) {
                        if (device instanceof DisplayBlockEntity display && !processedDisplays.contains(devicePos))
                        {
                            sendDisplayPanel(level, be, displayNode, display, processedDisplays);
                        }
                        break;
                    }
                    if (node instanceof AnalogLinkNode analogNode && analogNode.getLinkName() != null && analogNode.getLinkName().equals(deviceName) && analogNode.isSender()) {
                        AnalogDevicesPacket packet = new AnalogDevicesPacket(be.getNetWorkName(), PacketType.ANALOG, analogNode.getValue());
                        device.receivePacket(packet);
                        break;
                    }
                    else
                    {
                        AnalogDevicesPacket packet = new AnalogDevicesPacket(be.getNetWorkName(), PacketType.ANALOG, 0);
                        device.receivePacket(packet);
                    }
                    if (node instanceof ServoNode servoNode && servoNode.getLinkName().equals(deviceName)
                            && servoNode.hasTarget()) {
                        device.receivePacket(new AnalogDevicesPacket(be.getNetWorkName(), PacketType.SERVO,
                                servoNode.getTargetAngle()));
                        break;
                    }
                    if (node instanceof GearshiftNode gearshiftNode && gearshiftNode.getLinkName() != null && gearshiftNode.getLinkName().equals(deviceName)) {
                        AnalogDevicesPacket packet = new AnalogDevicesPacket(be.getNetWorkName(), PacketType.GEARSHIFT, new GearshiftControl(gearshiftNode.isStop(), gearshiftNode.isReversed()));
                        device.receivePacket(packet);
                        break;
                    }
                    if (node instanceof SurfaceScannerNode scannerNode && scannerNode.getLinkName() != null && scannerNode.getLinkName().equals(deviceName))
                    {
                        float[] settings = {scannerNode.getAngle(), scannerNode.getFOV(), scannerNode.getSize(), scannerNode.isIgnoreTrans() ? 1 : 0, scannerNode.isOn() ? 1 : 0};
                        device.receivePacket(new AnalogDevicesPacket(be.getNetWorkName(), PacketType.SCANNER, settings));
                        break;
                    }
                }
            }
        }

        be.setChanged();
    }

    private void updateSensorNodes(Level level) {
        boolean altitudeInstalled = hasModule(ServerModuleType.ALTITUDE);
        boolean gyroscopeInstalled = hasModule(ServerModuleType.GYROSCOPE);
        boolean compassInstalled = hasModule(ServerModuleType.COMPASS);
        boolean gpsInstalled = hasModule(ServerModuleType.GPS);

        float altitude = 0.0F;
        Vector3d rotationDegrees = new Vector3d();
        Vec3 globalPosition = null;
        SubLevel subLevel = null;

        if (altitudeInstalled || compassInstalled || gpsInstalled) {
            globalPosition = Sable.HELPER.projectOutOfSubLevel(
                    level,
                    Vec3.atCenterOf(worldPosition)
            );
        }

        if (altitudeInstalled) {
            altitude = (float) globalPosition.y;
        }

        if (gyroscopeInstalled || compassInstalled) {
            subLevel = Sable.HELPER.getContaining(this);
        }

        if (gyroscopeInstalled) {
            if (subLevel != null) {
                subLevel.logicalPose()
                        .orientation()
                        .getEulerAnglesYXZ(rotationDegrees)
                        .mul(180.0 / Math.PI);
            }
        }

        for (Node node : nodes) {
            if (node instanceof AnalogAltitudeNode) {
                node.setOutputValue(0,
                        altitudeInstalled ? NodeValue.of((float) Math.round(altitude * 1000)/1000) : NodeValue.EMPTY);
            } else if (node instanceof AnalogGyroNode) {
                node.setOutputValue(0,
                        gyroscopeInstalled ? NodeValue.of((float) Math.round(rotationDegrees.x * 1000)/1000) : NodeValue.EMPTY);
                node.setOutputValue(1,
                        gyroscopeInstalled ? NodeValue.of((float) Math.round(rotationDegrees.y * 1000)/1000) : NodeValue.EMPTY);
                node.setOutputValue(2,
                        gyroscopeInstalled ? NodeValue.of((float) Math.round(rotationDegrees.z * 1000)/1000) : NodeValue.EMPTY);
            } else if (node instanceof CompassNode compassNode) {
                updateCompassNode(compassNode, compassInstalled, globalPosition, subLevel);
            } else if (node instanceof GpsNode) {
                for (int output = 0; output < 3; output++) {
                    double coordinate = globalPosition == null ? 0 : switch (output) {
                        case 0 -> globalPosition.x;
                        case 1 -> globalPosition.y;
                        default -> globalPosition.z;
                    };
                    node.setOutputValue(output, gpsInstalled && globalPosition != null
                            ? roundedNodeValue(coordinate) : NodeValue.EMPTY);
                }
            }
        }
    }

    private void updateCompassNode(
            CompassNode node,
            boolean compassInstalled,
            Vec3 globalPosition,
            SubLevel subLevel
    ) {
        Direction facing = getBlockState().getValue(ServerBlock.FACING);
        if (!compassInstalled || globalPosition == null) {
            for (int output = 0; output < 4; output++) {
                node.setOutputValue(output, NodeValue.EMPTY);
            }
            return;
        }

        List<NodeValue> coordinates = node.getInputArray();
        if (coordinates.stream().anyMatch(NodeValue::isEmpty)) {
            for (int output = 0; output < 4; output++) {
                node.setOutputValue(output, NodeValue.EMPTY);
            }
            return;
        }

        Vector3d direction = new Vector3d(
                coordinates.get(0).asFloat() - globalPosition.x,
                coordinates.get(1).asFloat() - globalPosition.y,
                coordinates.get(2).asFloat() - globalPosition.z
        );

        double distance = direction.length();
        Vector3d angles = new Vector3d();
        Vector3d forward = new Vector3d(
                facing.getStepX(),
                facing.getStepY(),
                facing.getStepZ()
        );

        if (distance > 1.0E-6) {
            if (subLevel != null) {
                subLevel.logicalPose().orientation().transformInverse(direction);
            }

            new Quaterniond()
                    .rotationTo(forward, direction.normalize(new Vector3d()))
                    .getEulerAnglesXYZ(angles)
                    .mul(180.0 / Math.PI);
        }

        node.setOutputValue(0, roundedNodeValue(angles.x));
        node.setOutputValue(1, roundedNodeValue(angles.y));
        node.setOutputValue(2, roundedNodeValue(angles.z));
        node.setOutputValue(3, roundedNodeValue(distance));
    }

    private static NodeValue roundedNodeValue(double value) {
        return NodeValue.of((float) (Math.round(value * 1000.0) / 1000.0));
    }

    public boolean hasModule(ServerModuleType type) {
        for (int slot = 0; slot < modules.getSlots(); slot++) {
            ItemStack stack = modules.getStackInSlot(slot);
            if (stack.getItem() instanceof ServerModuleItem module
                    && module.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public void receiveUpdateNodesPacket(NodesData data)
    {
        if (data.nodeType() == NodeType.LINK) {
            for (Node existing : nodes) {
                if (existing.getId().equals(data.id()) && existing instanceof LinkNode) {
                    existing.setX(data.x()); existing.setY(data.y()); setChanged(); return;
                }
            }
            LinkNode link = new LinkNode(data.id(), data.x(), data.y(), 0, 0, 210, 145, List.of(), List.of());
            nodes.add(link); setChanged(); ServerLinks.sync(this, link); return;
        }
        nodes.removeIf(node -> node.getId().equals(data.id()));
        Node node = GuiUtils.createNodeByData(data);
        nodes.add(node);
        setChanged();
    }
    public void receiveDeleteNodePacket(UUID id)
    {
        nodes.removeIf(node -> node.getId().equals(id));
        connections.removeAllForNode(id);
        setChanged();
    }
    public void receiveUpdateConnectionsPacket(List<Connections.Connection> newConnections) {
        connections.setAllConnections(newConnections);
        setChanged();
    }
    public NetworkResult receiveNetworkPayload(String netWorkName, String netWorkPassword, NetworkEnum connection)
    {
        if (connection.equals(NetworkEnum.CONNECT))
        {
            if (netWorkName == null || netWorkName.isBlank()
                    || netWorkPassword == null || netWorkPassword.isBlank()) {
                return NetworkResult.MISSING_NETWORK_DATA;
            }
            this.netWorkName = netWorkName;
            this.netWorkPassword = netWorkPassword;
            if (!NetworkManager.getInstance().register(this)) {
                isConnected = false;
                syncNetworkState();
                return NetworkResult.NETWORK_EXISTS;
            }
            isConnected = true;
            syncNetworkState();
            return NetworkResult.CONNECTED;
        }
        else if (connection.equals(NetworkEnum.DISCONNECT))
        {
            NetworkManager.getInstance().unregister(this);
            isConnected = false;
            syncNetworkState();
            return NetworkResult.DISCONNECTED;
        }
        else
        {
            String oldName = this.netWorkName;
            String oldPassword = this.netWorkPassword;
            NetworkManager.getInstance().unregister(this);
            this.netWorkName = netWorkName;
            this.netWorkPassword = netWorkPassword;
            if (!NetworkManager.getInstance().register(this)) {
                this.netWorkName = oldName;
                this.netWorkPassword = oldPassword;
                NetworkManager.getInstance().register(this);
                syncNetworkState();
                return NetworkResult.NETWORK_EXISTS;
            }
            isConnected = true;
            syncNetworkState();
            return NetworkResult.CONNECTED;
        }
    }

    private void syncNetworkState() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (level != null && !level.isClientSide && isConnected
                && !NetworkManager.getInstance().register(this)) {
            isConnected = false;
            syncNetworkState();
        }
    }
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        NetworkManager.getInstance().unregister(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag nodesTag = new ListTag();
        for (Node node : nodes)
        {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putUUID("id", node.getId());
            nodeTag.putInt("x", node.getX());
            nodeTag.putInt("y", node.getY());
            nodeTag.putInt("width", node.getWidth());
            nodeTag.putInt("height", node.getHeight());
            nodeTag.putInt("inputs", node.getNumInputs());
            nodeTag.putInt("outputs", node.getNumOutputs());
            nodeTag.put("extraData", node.getExtraData());
            nodeTag.putInt("type", node.getType().ordinal());
            nodesTag.add(nodeTag);
        }
        tag.put("nodes", nodesTag);


        ListTag connectionsTag = new ListTag();
        for (Connections.Connection conn : connections.getAllConnections())
        {
            CompoundTag connTag = new CompoundTag();
            connTag.putUUID("fromId", conn.fromId());
            connTag.putInt("fromPort", conn.fromPort());
            connTag.putUUID("toId", conn.toId());
            connTag.putInt("toPort", conn.toPort());
            connectionsTag.add(connTag);
        }
        tag.put("connections", connectionsTag);

        ListTag devicesTag = new ListTag();
        for (BlockPos device : devices)
        {
            CompoundTag deviceTag = new CompoundTag();
            deviceTag.putInt("x", device.getX());
            deviceTag.putInt("y", device.getY());
            deviceTag.putInt("z", device.getZ());
            devicesTag.add(deviceTag);
        }
        tag.put("devices", devicesTag);

        CompoundTag networkTag = new CompoundTag();
        networkTag.putString("name", netWorkName);
        networkTag.putString("password", netWorkPassword);
        networkTag.putBoolean("isConnected", isConnected);
        tag.putInt("tickCounter", tickCounter);
        tag.put("network", networkTag);
        tag.put("modules", modules.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        nodes.clear();
        ListTag nodesTag = tag.getList("nodes", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < nodesTag.size(); i++)
        {
            CompoundTag nodeTag = nodesTag.getCompound(i);
            int numInputs = nodeTag.getInt("inputs");
            int numOutputs = nodeTag.getInt("outputs");

            List<NodeValue> inputValues = new ArrayList<>(Collections.nCopies(numInputs, NodeValue.EMPTY));
            List<NodeValue> outputValues = new ArrayList<>(Collections.nCopies(numOutputs, NodeValue.EMPTY));

            NodesData data = new NodesData(
                    nodeTag.getUUID("id"),
                    nodeTag.getInt("x"),
                    nodeTag.getInt("y"),
                    nodeTag.getInt("width"),
                    nodeTag.getInt("height"),
                    numInputs,
                    numOutputs,
                    inputValues,
                    outputValues,
                    nodeTag.getCompound("extraData"),
                    NodeType.values()[nodeTag.getInt("type")]
            );
            nodes.add(GuiUtils.createNodeByData(data));
        }


        List<Connections.Connection> loadedConnections = new ArrayList<>();
        ListTag connectionsTag = tag.getList("connections", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < connectionsTag.size(); i++)
        {
            CompoundTag connTag = connectionsTag.getCompound(i);
            loadedConnections.add(new Connections.Connection(
                    connTag.getUUID("fromId"),
                    connTag.getInt("fromPort"),
                    connTag.getUUID("toId"),
                    connTag.getInt("toPort")
            ));
        }

        ListTag devicesTag = tag.getList("devices", CompoundTag.TAG_COMPOUND);
        devices.clear();
        for (int i = 0; i < devicesTag.size(); i++)
        {
            CompoundTag deviceTag = devicesTag.getCompound(i);
            BlockPos pos = BlockPos.of(BlockPos.asLong(deviceTag.getInt("x"), deviceTag.getInt("y"), deviceTag.getInt("z")));
            devices.add(pos);
        }

        connections.setAllConnections(loadedConnections);
        CompoundTag networkTag = tag.getCompound("network");
        netWorkName = networkTag.getString("name");
        netWorkPassword = networkTag.getString("password");
        isConnected = networkTag.getBoolean("isConnected");
        tickCounter = tag.getInt("tickCounter");
        if (tag.contains("modules"))
        {
            modules.deserializeNBT(registries, tag.getCompound("modules"));
        }

        if (level != null && level.isClientSide) {
            NetworkManager.getInstance().unregister(this);
            if (isConnected) NetworkManager.getInstance().register(this);
        }

    }



    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setNetworkName(String name) {
        this.netWorkName = name;
        level.sendBlockUpdated(
                worldPosition,
                getBlockState(),
                getBlockState(),
                3
        );
        setChanged();
    }
    public void setNetWorkPassword(String password)
    {
        this.netWorkPassword = password;
        level.sendBlockUpdated(
                worldPosition,
                getBlockState(),
                getBlockState(),
                3
        );
        setChanged();
    }
    public String getNetWorkName()
    {
        return netWorkName;
    }
    public String getNetWorkPassword()
    {
        return netWorkPassword;
    }

    public void connectDevice(BlockPos devicePos)
    {
        if (!devices.contains(devicePos)) devices.add(devicePos);
        setChanged();
    }
    public void disconnectDevice(BlockPos devicePos)
    {
        if (devices.contains(devicePos))
        {
            devices.remove(devicePos);
            setChanged();
        }
    }

    public boolean canUseDeviceName(BlockPos connectingPos, String deviceName, Class<?> deviceType) {
        if (level == null) return false;
        devices.removeIf(pos -> level.getBlockEntity(pos) == null);
        for (BlockPos pos : devices) {
            if (pos.equals(connectingPos)) continue;
            if (!(level.getBlockEntity(pos) instanceof NetworkDevice device)) continue;
            if (!deviceName.equals(device.getNetwork().getDeviceName())) continue;
            if (device.getClass() != deviceType) return false;
        }
        return true;
    }
    public void receivePacket(AnalogDevicesPacket packet)
    {
        if (packet.getType().equals(PacketType.ANALOG))
        {
            for (Node node : nodes)
            {
                if (node instanceof AnalogLinkNode analogNode)
                {
                    if (packet.getSenderId().equals(analogNode.getLinkName()))
                    {
                        analogNode.receiveSignal(((Number) packet.getValue()).floatValue());
                    }
                }
            }
        }
        else if (packet.getType().equals(PacketType.SCANNER))
        {
            for (Node node : nodes)
            {
                if (node instanceof SurfaceScannerNode scannerNode)
                {
                    if (packet.getSenderId().equals(scannerNode.getLinkName()))
                    {
                        scannerNode.receiveSignal(packet.getValue());
                    }
                }
            }
        }
        else if (packet.getType().equals(PacketType.BUTTON))
        {
            for (Node node : nodes)
            {
                if (node instanceof ButtonNode buttonNode)
                {
                    if (packet.getSenderId().equals(buttonNode.getLinkName()))
                    {
                        buttonNode.receiveSignal(((Boolean) packet.getValue()).booleanValue());
                    }
                }
            }
        }
    }
    public boolean isConnected()
    {
        return isConnected;
    }

    public List<BlockPos> getConnectedDevices() {
        return devices;
    }

    public List<ItemStack> getModuleDrops() {
        List<ItemStack> drops = new ArrayList<>();

        for (int slot = 0; slot < modules.getSlots(); slot++) {
            ItemStack module = modules.getStackInSlot(slot);
            if (!module.isEmpty()) {
                drops.add(module.copy());
            }
        }

        return drops;
    }

    public void installModule(int slot, ItemStack held, Player player) {
        if (slot < 0 || slot >= modules.getSlots()
                || !modules.getStackInSlot(slot).isEmpty()
                || !(held.getItem() instanceof ServerModuleItem module)) {
            return;
        }

        modules.setStackInSlot(slot, held.copyWithCount(1));

        int modelValue = switch (module.getType()) {
            case GYROSCOPE -> 1;
            case ALTITUDE -> 2;
            case COMPASS -> 3;
            case GPS -> 4;
            case LINK -> 5;
        };

        if (level != null) {
            level.setBlockAndUpdate(
                    worldPosition,
                    getBlockState().setValue(ServerBlock.getSlotProperty(slot), modelValue)
            );
        }

        if (!player.isCreative()) {
            held.shrink(1);
        }
    }

    public void removeModule(int slot, Player player) {
        if (slot < 0 || slot >= modules.getSlots()) {
            return;
        }

        ItemStack removed = modules.extractItem(slot, 1, false);
        if (removed.isEmpty()) {
            return;
        }

        if (!player.addItem(removed)) {
            player.drop(removed, false);
        }

        if (level != null) {
            level.setBlockAndUpdate(
                    worldPosition,
                    getBlockState().setValue(ServerBlock.getSlotProperty(slot), 0)
            );
        }
    }

    private static Direction getDisplayRight(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    private static void sendDisplayPanel(
            Level level,
            ServerBlockEntity server,
            DisplayNode node,
            DisplayBlockEntity anchor,
            Set<BlockPos> processed
    ) {
        BlockPos anchorPos = anchor.getBlockPos();

        if (processed.contains(anchorPos)) {
            return;
        }

        Direction facing = anchor.getBlockState()
                .getValue(DisplayBlock.FACING);
        Direction right = getDisplayRight(facing);

        Map<BlockPos, DisplayBlockEntity> candidates = new HashMap<>();

        for (BlockPos devicePos : server.devices) {
            if (level.getBlockEntity(devicePos)
                    instanceof DisplayBlockEntity display
                    && display.getNetwork().getNetworkName()
                    .equals(server.getNetWorkName())
                    && display.getBlockState()
                    .getValue(DisplayBlock.FACING) == facing) {
                candidates.put(devicePos.immutable(), display);
            }
        }

        if (!candidates.containsKey(anchorPos)) {
            return;
        }

        Set<BlockPos> group = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(anchorPos);

        Direction[] directions = {
                right,
                right.getOpposite(),
                Direction.UP,
                Direction.DOWN
        };

        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();

            if (!group.add(current)) {
                continue;
            }

            for (Direction direction : directions) {
                BlockPos next = current.relative(direction);

                if (candidates.containsKey(next) && !group.contains(next)) {
                    queue.add(next);
                }
            }
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (BlockPos p : group) {
            int x = p.getX() * right.getStepX()
                    + p.getZ() * right.getStepZ();

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        int columns = maxX - minX + 1;
        int rows = maxY - minY + 1;

        Canvas.Data image = node.getCanvas();

        int imageWidth = image.width();
        int imageHeight = image.height();
        int[] pixels = image.pixels();

        for (BlockPos p : group) {
            int x = p.getX() * right.getStepX()
                    + p.getZ() * right.getStepZ();

            int blockX = x - minX;
            int blockY = maxY - p.getY();

            List<NodeValue> part = new ArrayList<>(64);

            for (int y = 0; y < 8; y++) {
                for (int col = 0; col < 8; col++) {
                    int imageX = blockX * 8 + col;
                    int imageY = blockY * 8 + y;

                    int brightness = (imageX >= 0
                            && imageX < imageWidth
                            && imageY >= 0
                            && imageY < imageHeight)
                            ? pixels[imageY * imageWidth + imageX]
                            : 0;

                    part.add(NodeValue.of(brightness));
                }
            }

            DisplayBlockEntity display = candidates.get(p);

            display.receivePacket(new AnalogDevicesPacket(
                    server.getNetWorkName(),
                    PacketType.DISPLAY,
                    part
            ));
        }

        processed.addAll(group);
    }
}
