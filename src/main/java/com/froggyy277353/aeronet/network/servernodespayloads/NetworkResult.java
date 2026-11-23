package com.froggyy277353.aeronet.network.servernodespayloads;

public enum NetworkResult {
    CONNECTED("aeronet.network.result.connected"),
    DISCONNECTED("aeronet.network.result.disconnected"),
    NETWORK_EXISTS("aeronet.network.result.network_exists"),
    DEVICE_EXISTS("aeronet.network.result.device_exists"),
    NETWORK_NOT_FOUND("aeronet.network.result.not_found"),
    MISSING_NETWORK_DATA("aeronet.network.result.missing_network_data"),
    MISSING_DEVICE_NAME("aeronet.network.result.missing_device_name");

    private final String translationKey;

    NetworkResult(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
