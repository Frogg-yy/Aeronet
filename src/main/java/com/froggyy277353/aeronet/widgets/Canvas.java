package com.froggyy277353.aeronet.widgets;

import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;

import java.util.ArrayList;
import java.util.List;

public final class Canvas {
    public static boolean[] from(NodeValue v) {
        boolean[] px = new boolean[64];
        if (v != null && v.type() == ValueType.LIST) {
            List<NodeValue> l = v.asList();
            for (int i = 0; i < 64 && i < l.size(); i++)
                px[i] = l.get(i).type() == ValueType.BOOLEAN && l.get(i).asBoolean();
        }
        return px;
    }

    public static NodeValue toValue(boolean[] px) {
        List<NodeValue> l = new ArrayList<>(64);
        for (boolean b : px) l.add(NodeValue.of(b));
        return NodeValue.of(l);
    }

    public static void set(boolean[] px, int x, int y, boolean on) {
        if (x >= 0 && x < 8 && y >= 0 && y < 8) px[y * 8 + x] = on;
    }

    public static void line(boolean[] px, int x0, int y0, int x1, int y1, boolean on) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        while (true) {
            set(px, x0, y0, on);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    public static void rect(boolean[] px, int x, int y, int w, int h, boolean fill, boolean on) {
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                if (fill || i == 0 || j == 0 || i == w - 1 || j == h - 1)
                    set(px, x + i, y + j, on);
    }

    public static void circle(boolean[] px, int cx, int cy, int r, boolean fill, boolean on) {
        for (int y = -r; y <= r; y++)
            for (int x = -r; x <= r; x++) {
                int d = x * x + y * y;
                if (fill ? d <= r * r + r : (d <= r * r + r && d >= r * r - r))
                    set(px, cx + x, cy + y, on);
            }
    }
}