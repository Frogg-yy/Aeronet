package com.froggyy277353.aeronet.widgets;

import com.froggyy277353.aeronet.widgets.valueType.NodeValue;

import java.util.*;

public class Connections {

    public record Connection(UUID fromId, int fromPort, UUID toId, int toPort) {}

    private final List<Connection> connections = new ArrayList<>();

    public static boolean isValid(Connection connection, Map<UUID, Node> nodes) {
        Node from = nodes.get(connection.fromId());
        Node to = nodes.get(connection.toId());
        if (from == null || to == null
                || connection.fromPort() < 0 || connection.fromPort() >= from.getNumOutputs()
                || connection.toPort() < 0 || connection.toPort() >= to.getNumInputs()
                || connection.fromPort() >= from.getOutputArray().size()
                || connection.toPort() >= to.getInputArray().size()) {
            return false;
        }
        NodeValue value = from.getOutputArray().get(connection.fromPort());
        return to.acceptsInputType(connection.toPort(), from.getOutputType(connection.fromPort()))
                && (value.isEmpty() || to.acceptsInputType(connection.toPort(), value.type()));
    }

    public void updateConnectedPorts(Map<UUID, Node> nodes) {
        Map<UUID, Set<Integer>> inputs = new HashMap<>();
        Map<UUID, Set<Integer>> outputs = new HashMap<>();
        for (Connection connection : connections) {
            if (!isValid(connection, nodes)) {
                continue;
            }
            inputs.computeIfAbsent(connection.toId(), id -> new HashSet<>()).add(connection.toPort());
            outputs.computeIfAbsent(connection.fromId(), id -> new HashSet<>()).add(connection.fromPort());
        }
        for (Node node : nodes.values()) {
            Set<Integer> connectedInputs = inputs.getOrDefault(node.getId(), Set.of());
            node.setConnectedPorts(connectedInputs, outputs.getOrDefault(node.getId(), Set.of()));
            for (int port = 0; port < node.getNumInputs(); port++) {
                // Disconnected or invalid wires must not leave the previous signal behind.
                node.setInputValue(port, connectedInputs.contains(port)
                        ? node.convertInputValue(port, node.getInputArray().get(port)) : NodeValue.EMPTY);
            }
        }
    }

    public void transferValues(Map<UUID, Node> nodes) {
        updateConnectedPorts(nodes);
        for (Connection connection : connections) {
            if (!isValid(connection, nodes)) {
                continue;
            }
            Node from = nodes.get(connection.fromId());
            Node to = nodes.get(connection.toId());
            to.setInputValue(connection.toPort(), to.convertInputValue(connection.toPort(),
                    from.getOutputArray().get(connection.fromPort())));
        }
    }

    public void addConnection(UUID fromId, int fromPort, UUID toId, int toPort) {
        connections.add(new Connection(fromId, fromPort, toId, toPort));
    }

    public void removeConnection(UUID fromId, int fromPort, UUID toId, int toPort) {
        connections.removeIf(c ->
                c.fromId().equals(fromId) && c.fromPort() == fromPort &&
                        c.toId().equals(toId) && c.toPort() == toPort
        );
    }

    public void removeAllForNode(UUID nodeId) {
        connections.removeIf(c -> c.fromId().equals(nodeId) || c.toId().equals(nodeId));
    }

    public List<Connection> getConnectionsFrom(UUID fromId) {
        return connections.stream()
                .filter(c -> c.fromId().equals(fromId))
                .toList();
    }

    public List<Connection> getConnectionsTo(UUID toId) {
        return connections.stream()
                .filter(c -> c.toId().equals(toId))
                .toList();
    }

    public List<Connection> getAllConnections() {
        return connections;
    }
    public void setAllConnections(List<Connection> newConnections) {
        connections.clear();
        connections.addAll(newConnections);
    }
}
