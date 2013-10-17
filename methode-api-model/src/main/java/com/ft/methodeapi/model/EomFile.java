package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class EomFile {

    /**
     * Marks reason for controversial design choices.
     */
    private static final String JUSTIFICATION_MEMORY_CHURN = "Likelihood of memory churn outweighs risk of security issue";

    private final String uuid;
    private final String type;

    @SuppressWarnings(value = "EI_EXPOSE_REP2", justification = JUSTIFICATION_MEMORY_CHURN)
    private final byte[] value;
    private final String attributes;

    public EomFile(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type,
                   @JsonProperty("value") byte[] bytes,
                   @JsonProperty("attributes") String attributes) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes;
        this.attributes = attributes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings(value = "EI_EXPOSE_REP", justification = JUSTIFICATION_MEMORY_CHURN)
    public byte[] getValue() {
        return value;
    }

    public String getAttributes() {
        return attributes;
    }

    public static class Builder {
        private String uuid;
        private String type;
        private byte[] value;
        private String attributes;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        @SuppressWarnings(value = "EI_EXPOSE_REP", justification = JUSTIFICATION_MEMORY_CHURN)
        public Builder withValue(byte[] value) {
            this.value = value;
            return this;
        }

        public Builder withAttributes(String attributes) {
            this.attributes = attributes;
            return this;
        }

        public EomFile build() {
            return new EomFile(uuid, type, value, attributes);
        }
    }
}
