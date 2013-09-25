package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EomFile {

    private final String uuid;
    private final String type;
    private final byte[] value;


    public EomFile(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type,
                   @JsonProperty("value") byte[] bytes) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes;
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

    public static class Builder {
        private String uuid;
        private String type;
        private byte[] value;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withValue(byte[] value) {
            this.value = value;
            return this;
        }

        public EomFile build() {
            return new EomFile(uuid, type, value);
        }
    }
}
