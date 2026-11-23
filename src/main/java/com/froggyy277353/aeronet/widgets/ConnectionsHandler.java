package com.froggyy277353.aeronet.widgets;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.widgets.Connections;
import com.froggyy277353.aeronet.widgets.Node;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionsHandler {

    private static final int LINE_COLOR = 0xFF646464;
    public static final int INVALID_LINE_COLOR = 0xFFFF4040;

    public static void render(GuiGraphics graphics, Connections connections, List<Node> nodes,
                              int canvasLeft, int canvasTop, int canvasRight, int canvasBottom) {
        Map<UUID, Node> nodesById = nodes.stream()
                .collect(java.util.stream.Collectors.toMap(Node::getId, n -> n));

        for (Connections.Connection connection : connections.getAllConnections()) {
            Node from = nodesById.get(connection.fromId());
            Node to = nodesById.get(connection.toId());

            if (from == null || to == null) {
                continue;
            }

            // Keep a connection visible when either endpoint is in the canvas.
            if (!isVisible(from, canvasLeft, canvasTop, canvasRight, canvasBottom)
                    && !isVisible(to, canvasLeft, canvasTop, canvasRight, canvasBottom)) {
                continue;
            }

            int x0 = from.getScreenX() + from.getWidth();
            int y0 = from.getScreenY() + Node.PORT_START_OFFSET + connection.fromPort() * Node.PORT_SPACING;

            int x1 = to.getScreenX();
            int y1 = to.getScreenY() + Node.PORT_START_OFFSET + connection.toPort() * Node.PORT_SPACING;

            int color = Connections.isValid(connection, nodesById) ? LINE_COLOR : INVALID_LINE_COLOR;
            GuiUtils.drawLine(graphics, x0, y0+2, x1, y1+2, color);
        }
    }

    private static boolean isVisible(Node node, int canvasLeft, int canvasTop, int canvasRight, int canvasBottom) {
        int left = node.getScreenX();
        int top = node.getScreenY();
        int right = left + node.getWidth();
        int bottom = top + node.getHeight();
        return right >= canvasLeft && left <= canvasRight
                && bottom >= canvasTop && top <= canvasBottom;
    }
}
