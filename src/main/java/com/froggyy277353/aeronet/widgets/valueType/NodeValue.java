package com.froggyy277353.aeronet.widgets.valueType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record NodeValue(ValueType type, Object value) {

    public static final NodeValue EMPTY = new NodeValue(ValueType.NULL, null);

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeValue> STREAM_CODEC =
            StreamCodec.of(NodeValue::encode, NodeValue::decode);

    public static void encode(RegistryFriendlyByteBuf buf, NodeValue nodeValue) {
        buf.writeEnum(nodeValue.type());

        switch (nodeValue.type()) {
            case NULL -> {
            }

            case INT -> buf.writeInt((Integer) nodeValue.value());

            case FLOAT -> buf.writeFloat((Float) nodeValue.value());

            case BOOLEAN -> buf.writeBoolean((Boolean) nodeValue.value());

            case STRING -> buf.writeUtf((String) nodeValue.value());

            case LIST -> {
                List<NodeValue> list = nodeValue.asList();

                buf.writeVarInt(list.size());

                for (NodeValue value : list) {
                    STREAM_CODEC.encode(buf, value);
                }
            }
        }
    }

    public static NodeValue decode(RegistryFriendlyByteBuf buf) {
        ValueType type = buf.readEnum(ValueType.class);

        return switch (type) {
            case NULL -> EMPTY;

            case INT -> NodeValue.of(buf.readInt());

            case FLOAT -> NodeValue.of(buf.readFloat());

            case BOOLEAN -> NodeValue.of(buf.readBoolean());

            case STRING -> NodeValue.of(buf.readUtf());

            case LIST -> {
                int size = buf.readVarInt();
                List<NodeValue> values = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    values.add(STREAM_CODEC.decode(buf));
                }

                yield new NodeValue(ValueType.LIST, values);
            }
        };
    }

    public static NodeValue of(Object value) {
        if (value == null) {
            return EMPTY;
        }

        if (value instanceof NodeValue nodeValue) {
            return nodeValue;
        }

        if (value instanceof Integer) {
            return new NodeValue(ValueType.INT, value);
        }

        if (value instanceof Float) {
            return new NodeValue(ValueType.FLOAT, value);
        }

        if (value instanceof Boolean) {
            return new NodeValue(ValueType.BOOLEAN, value);
        }

        if (value instanceof String) {
            return new NodeValue(ValueType.STRING, value);
        }

        if (value instanceof List<?> list) {
            List<NodeValue> converted = new ArrayList<>(list.size());

            for (Object object : list) {
                converted.add(NodeValue.of(object));
            }

            return new NodeValue(ValueType.LIST, converted);
        }

        throw new IllegalArgumentException(
                "Unsupported NodeValue type: " + value.getClass().getName()
        );
    }
    public ValueType getType()
    {
        return type;
    }
    public boolean isEmpty() {
        return type == ValueType.NULL;
    }

    public int asInt() {
        return switch (type) {
            case NULL -> 0;
            case INT -> (Integer) value;
            case FLOAT -> ((Float) value).intValue();
            case BOOLEAN -> (Boolean) value ? 1 : 0;
            default -> throw new IllegalArgumentException("Cannot convert " + type + " to INT");
        };
    }

    public float asFloat() {
        return switch (type) {
            case NULL -> 0.0F;
            case INT -> ((Integer) value).floatValue();
            case FLOAT -> (Float) value;
            case BOOLEAN -> (Boolean) value ? 1.0F : 0.0F;
            default -> throw new IllegalArgumentException("Cannot convert " + type + " to FLOAT");
        };
    }

    public boolean asBoolean() {
        return switch (type) {
            case NULL -> false;
            case INT -> (Integer) value != 0;
            case FLOAT -> (Float) value != 0.0F;
            case BOOLEAN -> (Boolean) value;
            default -> throw new IllegalArgumentException("Cannot convert " + type + " to BOOLEAN");
        };
    }

    public NodeValue convertTo(ValueType target) {
        if (isEmpty() || !type.canConvertTo(target)) {
            return EMPTY;
        }
        if (type == target) {
            return this;
        }
        return switch (target) {
            case INT -> of(asInt());
            case FLOAT -> of(asFloat());
            case BOOLEAN -> of(asBoolean());
            default -> EMPTY;
        };
    }

    public String asString() {
        if (value == null)
        {
            return "";
        }
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    public List<NodeValue> asList() {
        if (value == null)
        {
            return List.of(NodeValue.EMPTY);
        }
        return (List<NodeValue>) value;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString("type", type.name());

        switch (type) {
            case NULL -> {
            }

            case INT -> tag.putInt("value", asInt());

            case FLOAT -> tag.putFloat("value", asFloat());

            case BOOLEAN -> tag.putBoolean("value", asBoolean());

            case STRING -> tag.putString("value", asString());

            case LIST -> {
                ListTag listTag = new ListTag();

                for (NodeValue nodeValue : asList()) {
                    listTag.add(nodeValue.toTag());
                }

                tag.put("value", listTag);
            }
        }

        return tag;
    }

    public static NodeValue fromTag(CompoundTag tag) {
        ValueType type = ValueType.valueOf(tag.getString("type"));

        return switch (type) {
            case NULL -> EMPTY;

            case INT -> NodeValue.of(tag.getInt("value"));

            case FLOAT -> NodeValue.of(tag.getFloat("value"));

            case BOOLEAN -> NodeValue.of(tag.getBoolean("value"));

            case STRING -> NodeValue.of(tag.getString("value"));

            case LIST -> {
                ListTag listTag = tag.getList(
                        "value",
                        CompoundTag.TAG_COMPOUND
                );

                List<NodeValue> values = new ArrayList<>(listTag.size());

                for (int i = 0; i < listTag.size(); i++) {
                    values.add(
                            NodeValue.fromTag(listTag.getCompound(i))
                    );
                }

                yield new NodeValue(ValueType.LIST, values);
            }
        };
    }
}
