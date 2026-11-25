package com.froggyy277353.aeronet.network;

import com.froggyy277353.aeronet.widgets.*;
import com.froggyy277353.aeronet.widgets.booleans.*;
import com.froggyy277353.aeronet.widgets.logical.*;
import com.froggyy277353.aeronet.widgets.math.*;
import com.froggyy277353.aeronet.widgets.physics.*;
import com.froggyy277353.aeronet.widgets.physics.display.*;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.ScannerPixelNode;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.ScannerStats;
import com.froggyy277353.aeronet.widgets.physics.surface_scanner.SurfaceScannerNode;
import com.froggyy277353.aeronet.widgets.strings.ConcatStringNode;
import com.froggyy277353.aeronet.widgets.strings.ItemConstantNode;
import com.froggyy277353.aeronet.widgets.strings.StringNode;
import com.froggyy277353.aeronet.widgets.strings.ViewStringNode;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;

import java.util.List;
import java.util.UUID;

public enum NodeType {
    INT(IntNode::new),
    DISPLAY(DisplayNode::new),
    ANALOG_LINK(AnalogLinkNode::new),
    ADD(AddNode::new),
    SUBTRACT(SubtractNode::new),
    MULTIPLY(MultiplyNode::new),
    DIVIDE(DivideNode::new),
    MAP(MapNode::new),
    IMPULSE(ImpulseNode::new),
    EQUALS(EqualsNode::new),
    LESS(LessNode::new),
    GREATER(GreaterNode::new),
    EQUALS_LESS(EqualsLessNode::new),
    EQUALS_GREATER(EqualsGreaterNode::new),
    GEARSHIFT(GearshiftNode::new),
    ALTITUDE(AnalogAltitudeNode::new),
    GYRO(AnalogGyroNode::new),
    COMPASS(CompassNode::new),
    SERVO(ServoNode::new),
    // Для нод, которым в конструктор дополнительно передаётся сам NodeType:
    SIN(TrigonometryNode::new),
    COS(TrigonometryNode::new),
    TAN(TrigonometryNode::new),
    ASIN(TrigonometryNode::new),
    ACOS(TrigonometryNode::new),
    ATAN(TrigonometryNode::new),
    TIMER(TimerNode::new),
    GPS(GpsNode::new),
    LINK(LinkNode::new),
    ARRAY_PACK(ArrayPackNode::new),
    ARRAY_UNPACK(ArrayUnpackNode::new),
    DISPLAY_VALUE(DisplayValueNode::new),
    DRAW_PIXEL(DrawPixelNode::new),
    DRAW_LINE(DrawLineNode::new),
    DRAW_RECT(DrawRectNode::new),
    DRAW_CIRCLE(DrawCircleNode::new),
    CANVAS_SIZE(CanvasSizeNode::new),
    SAVE(SaveNode::new),
    SCANNER(SurfaceScannerNode::new),
    SCANNER_PIXEL(ScannerPixelNode::new),
    SCANNER_STATS(ScannerStats::new),
    DRAW_SCAN(DrawScanNode::new),
    STRING(StringNode::new),
    CONCAT(ConcatStringNode::new),
    VIEW_STRING(ViewStringNode::new),
    STRING_EQUALS(StringEqualsNode::new),
    BOOLEAN_FLOAT(BooleanFloatNode::new),
    DISTANCE_BRIGHTNESS(DistanceBrightnessNode::new),
    ID_CONSTANT(ItemConstantNode::new),
    NOT(NotNode::new),
    BUTTON(ButtonNode::new),
    AND(AndNode::new),
    OR(OrNode::new),
    XOR(XorNode::new);

    @FunctionalInterface
    public interface StandardNodeFactory {
        Node create(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                    List<NodeValue> inputArray, List<NodeValue> outputArray);
    }

    @FunctionalInterface
    public interface TypedNodeFactory {
        Node create(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                    List<NodeValue> inputArray, List<NodeValue> outputArray, NodeType type);
    }

    private final TypedNodeFactory factory;

    NodeType(StandardNodeFactory simpleFactory) {
        this.factory = (id, x, y, in, out, w, h, inArr, outArr, type) ->
                simpleFactory.create(id, x, y, in, out, w, h, inArr, outArr);
    }

    NodeType(TypedNodeFactory typedFactory) {
        this.factory = typedFactory;
    }

    public Node create(NodesData data) {
        return factory.create(
                data.id(), data.x(), data.y(),
                data.inputs(), data.outputs(),
                data.width(), data.height(),
                data.inputArray(), data.outputArray(),
                this
        );
    }
}