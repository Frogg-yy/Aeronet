package com.froggyy277353.aeronet.widgets.physics.display;

import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;

import java.util.ArrayList;
import java.util.List;

public final class Canvas {
    public static final int SIZE = 8;
    public static final int PIXELS = SIZE * SIZE;

    private static final int LIMIT = 4096;


    private static final int[][] FONT = {
            {0b111, 0b101, 0b101, 0b101, 0b111},
            {0b010, 0b110, 0b010, 0b010, 0b111},
            {0b111, 0b001, 0b111, 0b100, 0b111},
            {0b111, 0b001, 0b111, 0b001, 0b111},
            {0b101, 0b101, 0b111, 0b001, 0b001},
            {0b111, 0b100, 0b111, 0b001, 0b111},
            {0b111, 0b100, 0b111, 0b101, 0b111},
            {0b111, 0b001, 0b001, 0b001, 0b001},
            {0b111, 0b101, 0b111, 0b101, 0b111},
            {0b111, 0b101, 0b111, 0b001, 0b111},
            {0b000, 0b000, 0b111, 0b000, 0b000}
    };

    private Canvas() {
    }

    public static final class Data {
        private final int width;
        private final int height;
        private final int[] pixels;

        public Data(int width, int height) {
            if (width < 1 || height < 1
                    || (long) width * height > LIMIT) {
                throw new IllegalArgumentException(
                        "Invalid canvas size: " + width + "x" + height
                );
            }

            this.width = width;
            this.height = height;
            this.pixels = new int[width * height];
        }

        private Data(int width, int height, int[] pixels) {
            this(width, height);
            System.arraycopy(
                    pixels, 0, this.pixels, 0,
                    Math.min(pixels.length, this.pixels.length)
            );
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int[] pixels() {
            return pixels;
        }
    }

    public static Data from(NodeValue value) {
        if (value != null && value.type() == ValueType.LIST) {
            List<NodeValue> list = value.asList();

            if (list.size() >= 3
                    && list.get(0).type() == ValueType.INT
                    && list.get(1).type() == ValueType.INT
                    && list.get(2).type() == ValueType.LIST) {

                int width = list.get(0).asInt();
                int height = list.get(1).asInt();

                Data canvas = new Data(width, height);
                List<NodeValue> values = list.get(2).asList();

                for (int i = 0; i < canvas.pixels.length && i < values.size(); i++)
                {
                    NodeValue pixel = values.get(i);
                    canvas.pixels[i] = pixel.asInt();
                }

                return canvas;
            }

            Data canvas = new Data(SIZE, SIZE);

            for (int i = 0; i < PIXELS && i < list.size(); i++)
            {
                NodeValue pixel = list.get(i);
                canvas.pixels[i] = pixel.asInt();
            }

            return canvas;
        }

        return new Data(SIZE, SIZE);
    }

    public static NodeValue toValue(Data canvas) {
        List<NodeValue> pixels = new ArrayList<>(canvas.pixels.length);

        for (int pixel : canvas.pixels) {
            pixels.add(NodeValue.of(pixel));
        }

        List<NodeValue> result = List.of(
                NodeValue.of(canvas.width),
                NodeValue.of(canvas.height),
                NodeValue.of(pixels)
        );

        return NodeValue.of(result);
    }

    public static void set(Data canvas, int x, int y, int on) {
        if (x >= 0 && x < canvas.width && y >= 0 && y < canvas.height)
        {
            canvas.pixels[y * canvas.width + x] = Math.clamp(on, 0, 255);

        }
    }

    public static void line(
            Data canvas,
            int x0, int y0, int x1, int y1,
            int on
    ) {
        x0 = clamp(x0, canvas.width);
        x1 = clamp(x1, canvas.width);
        y0 = clamp(y0, canvas.height);
        y1 = clamp(y1, canvas.height);

        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;

        while (true) {
            set(canvas, x0, y0, on);

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int e2 = 2 * err;

            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }

            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public static void rect(
            Data canvas,
            int x, int y, int w, int h,
            boolean fill, int on
    ) {
        w = Math.max(0, Math.min(canvas.width, w));
        h = Math.max(0, Math.min(canvas.height, h));

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (fill || i == 0 || j == 0
                        || i == w - 1 || j == h - 1) {
                    set(canvas, x + i, y + j, on);
                }
            }
        }
    }

    public static void circle(
            Data canvas,
            int cx, int cy, int r,
            boolean fill, int on
    ) {
        if (r < 0) {
            return;
        }

        r = Math.min(r, Math.max(canvas.width, canvas.height));

        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                int d = x * x + y * y;
                boolean inside = d <= r * r + r;
                boolean edge = inside && d >= r * r - r;

                if (fill ? inside : edge) {
                    set(canvas, cx + x, cy + y, on);
                }
            }
        }
    }

    public static void number(
            Data canvas, int x, int y, int value, int on
    ) {
        String text = Integer.toString(value);

        for (int i = 0; i < text.length(); i++) {
            glyph(canvas, x + i * 4, y, text.charAt(i), on);
        }
    }

    public static void bar(
            Data canvas,
            float value, float min, float max,
            int pos, int thick, boolean vertical, int on
    ) {
        float range = max - min;
        int dimension = vertical ? canvas.height : canvas.width;

        int length = range == 0 ? 0 : Math.max(
                0,
                Math.min(dimension,
                        Math.round((value - min) / range * dimension))
        );

        if (vertical) {
            rect(canvas, pos, canvas.height - length,
                    thick, length, true, on);
        } else {
            rect(canvas, 0, pos, length, thick, true, on);
        }
    }

    private static void glyph(
            Data canvas, int x, int y, char ch, int on
    ) {
        int index = ch == '-' ? 10 : ch - '0';

        if (index < 0 || index >= FONT.length) {
            return;
        }

        int[] rows = FONT[index];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 3; col++) {
                if (((rows[row] >> (2 - col)) & 1) != 0) {
                    set(canvas, x + col, y + row, on);
                }
            }
        }
    }

    private static int clamp(int value, int dimension) {
        return Math.max(-dimension, Math.min(dimension, value));
    }
}
