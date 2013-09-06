package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EomFile {

    private String uuid;
    private String type;
    private byte[] value;


    public EomFile(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type,
                   @JsonProperty("value") byte[] bytes) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes; // yes. really.
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
}
