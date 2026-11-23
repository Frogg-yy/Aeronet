package com.froggyy277353.aeronet.network;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import com.froggyy277353.aeronet.block.entity.server.ServerModuleType;
import com.froggyy277353.aeronet.network.servernodespayloads.*;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.physics.LinkNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

public final class ServerLinks {
    private ServerLinks() {}
    private static ServerBlockEntity find(ServerBlockEntity owner, String name, String password) {
        return NetworkManager.getInstance().findServer(owner, name, password);
    }
    private static LinkNode node(ServerBlockEntity server, UUID id) {
        if (id == null) return null;
        for (Node n : server.getNodes()) if (id.equals(n.getId()) && n instanceof LinkNode link) return link;
        return null;
    }
    public static void action(ServerBlockEntity owner, LinkActionPayload p) {
        LinkNode link = node(owner, p.nodeId());
        if (link == null) return;
        if (!owner.hasModule(ServerModuleType.LINK)) { report(owner, link, "module_missing"); return; }
        if (p.action() == 0) {
            if (!owner.isConnected()) { report(owner, link, "network_required"); return; }
            if (p.network().isBlank() || p.password().isBlank()) { report(owner, link, "credentials"); return; }
            ServerBlockEntity target = find(owner, p.network(), p.password());
            if (target == null) { report(owner, link, "not_found"); return; }
            if (!target.hasModule(ServerModuleType.LINK)) { report(owner, link, "module_missing"); return; }
            LinkNode peer = node(target, link.peerId());
            if (peer == null || !link.getId().equals(peer.peerId())) {
                // Reuse an unpaired reciprocal node if the user already created one.
                peer = target.getNodes().stream().filter(n -> n instanceof LinkNode)
                        .map(n -> (LinkNode) n)
                        .filter(n -> n.peerId() == null && n.targetName().equals(owner.getNetWorkName()))
                        .findFirst().orElse(null);
                if (peer == null) {
                    peer = new LinkNode(UUID.randomUUID(),0,
                            target.getNodes().stream().mapToInt(n -> n.getY() + n.getHeight() + 20).max().orElse(0),
                            0, 0, 210, 145, List.of(), List.of());
                    target.getNodes().add(peer);
                }
                mirror(target, peer, link);
            }
            link.pair(p.network(), p.password(), peer.getId());
            peer.pair(owner.getNetWorkName(), owner.getNetWorkPassword(), link.getId());
            reconcile(owner, link, target, peer);
            link.status("connected"); peer.status("connected");
            sync(owner, link); sync(target, peer);
        } else if (p.action() >= 1 && p.action() <= 4) {
            ServerBlockEntity target = find(owner, link.targetName(), link.targetPassword());
            LinkNode peer = validPeer(owner, link, target);
            if (peer != null) reconcile(owner, link, target, peer);
            if (link.editPort(p.action() == 1 || p.action() == 3, p.action() <= 2, p.valueType())) {
                prune(owner); sync(owner, link);
                if (peer != null) { mirror(target, peer, link); prune(target); sync(target, peer); }
            }
        }
    }
    private static LinkNode validPeer(ServerBlockEntity owner, LinkNode link, ServerBlockEntity target) {
        if (!owner.isConnected() || !owner.hasModule(ServerModuleType.LINK)
                || target == null || !target.hasModule(ServerModuleType.LINK)) return null;
        LinkNode peer = node(target, link.peerId());
        return peer != null && link.getId().equals(peer.peerId())
                && owner.getNetWorkName().equals(peer.targetName())
                && owner.getNetWorkPassword().equals(peer.targetPassword()) ? peer : null;
    }
    private static void reconcile(ServerBlockEntity owner, LinkNode link, ServerBlockEntity target, LinkNode peer) {
        int comparison = link.compareSchema(peer);
        if (comparison > 0) { mirror(target, peer, link); prune(target); sync(target, peer); }
        if (comparison < 0) { mirror(owner, link, peer); prune(owner); sync(owner, link); }
    }
    private static void mirror(ServerBlockEntity owner, LinkNode receiving, LinkNode sending) {
        var inputs = receiving.inputKeys(); var outputs = receiving.outputKeys();
        receiving.mirror(sending);
        pruneChangedPorts(owner.getConnections(), receiving, inputs, outputs);
    }
    public static void pruneChangedPorts(com.froggyy277353.aeronet.widgets.Connections connections,
                                         LinkNode node, List<UUID> inputs, List<UUID> outputs) {
        var newInputs = node.inputKeys(); var newOutputs = node.outputKeys();
        connections.getAllConnections().removeIf(c ->
                (c.fromId().equals(node.getId()) && changedKey(outputs, newOutputs, c.fromPort()))
                        || (c.toId().equals(node.getId()) && changedKey(inputs, newInputs, c.toPort())));
    }
    private static boolean changedKey(List<UUID> oldKeys, List<UUID> newKeys, int index) {
        return index < 0 || index >= oldKeys.size() || index >= newKeys.size()
                || !oldKeys.get(index).equals(newKeys.get(index));
    }
    public static void tick(ServerBlockEntity owner) {
        boolean enabled = owner.isConnected() && owner.hasModule(ServerModuleType.LINK);
        // Reconciliation changes port schemas, never the node list.
        for (Node n : owner.getNodes()) {
            if (!(n instanceof LinkNode link)) continue;
            ServerBlockEntity target = enabled && link.peerId() != null
                    ? find(owner, link.targetName(), link.targetPassword()) : null;
            LinkNode peer = target == null ? null : validPeer(owner, link, target);
            if (peer == null) {
                link.clearOutputs();
                // Preserve an actionable connection error until the next user attempt.
                if (link.peerId() != null && link.status("offline")) sync(owner, link);
                continue;
            }
            reconcile(owner, link, target, peer);
            if (link.status("connected")) sync(owner, link);
            for (int i = 0; i < link.getNumOutputs(); i++)
                link.setOutputValue(i, peer.getInputArray().get(i).convertTo(link.getOutputType(i)));
        }
    }
    public static void prune(ServerBlockEntity owner) {
        Map<UUID, Node> nodes = new HashMap<>();
        owner.getNodes().forEach(n -> nodes.put(n.getId(), n));
        owner.getConnections().getAllConnections().removeIf(c ->
                (nodes.get(c.fromId()) instanceof LinkNode || nodes.get(c.toId()) instanceof LinkNode)
                        && !com.froggyy277353.aeronet.widgets.Connections.isValid(c, nodes));
        owner.setChanged();
    }
    private static void report(ServerBlockEntity owner, LinkNode link, String status) {
        link.status(status); sync(owner, link);
    }
    public static void sync(ServerBlockEntity owner, LinkNode link) {
        owner.setChanged();
        if (!(owner.getLevel() instanceof ServerLevel level)) return;
        NodesData data = new NodesData(link.getId(), link.getX(), link.getY(), link.getWidth(), link.getHeight(),
               link.getNumInputs(), link.getNumOutputs(), link.getInputArray(), link.getOutputArray(),
                link.getExtraData(), NodeType.LINK);
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(owner.getBlockPos()),
                new LinkSyncPayload(new UpdateNodesPayload(owner.getBlockPos(), data)));
    }
    public static void applyClient(List<Node> nodes, com.froggyy277353.aeronet.widgets.Connections connections, NodesData data) {
        LinkNode link = nodes.stream().filter(n -> n.getId().equals(data.id()) && n instanceof LinkNode)
                .map(n -> (LinkNode) n).findFirst().orElse(null);
        if (link == null) {
            link = new LinkNode(data.id(), data.x(), data.y(), 0, 0, 210, 145, List.of(), List.of());
            nodes.add(link);
        }
        var oldInputs = link.inputKeys(); var oldOutputs = link.outputKeys();
        link.setExtraData(data.extraData());
        pruneChangedPorts(connections, link, oldInputs, oldOutputs);
        link.setInputArray(data.inputArray()); link.setOutputArray(data.outputArray());
    }
}
