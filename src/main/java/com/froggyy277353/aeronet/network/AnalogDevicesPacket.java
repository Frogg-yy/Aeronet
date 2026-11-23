package com.froggyy277353.aeronet.network;

public class AnalogDevicesPacket {
    private final String senderId;
    private final PacketType type;
    private final Object value;

    public AnalogDevicesPacket(String senderId, PacketType type, Object value) {
        this.senderId = senderId;
        this.type = type;
        this.value = value;
    }

    public String getSenderId() { return senderId; }
    public PacketType getType() { return type; }
    public Object getValue() { return value; }
}
