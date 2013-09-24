package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EomFile {

    private final String uuid;
    private final String type;
    private final byte[] value;
    private final String attributes;

    public EomFile(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type,
                   @JsonProperty("value") byte[] bytes,
                   @JsonProperty("attributes") String attributes) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes; // yes. really.
        this.attributes = attributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public byte[] getValue() {
        return value;
    }

    public String getAttributes() {
        return attributes;
    }
}
