package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * LinkedObject
 *
 * @author Simon.Gibbs
 */
public class LinkedObject {

    private String uuid;
    private String type;
    private String attributes = null;
    private String systemAttributes = null;
    private String status = null;

    public LinkedObject() {}

    public LinkedObject(String uuid, String type) {
        this.uuid = uuid;
        this.type = type;
    }

    public LinkedObject(@JsonProperty("uuid")String uuid,
                        @JsonProperty("type")String type_name,
                        @JsonProperty("attributes")String attributes,
                        @JsonProperty("workflowStatus") String status_name,
                        @JsonProperty("systemAttributes") String system_attributes) {
        this.uuid = uuid;
        this.type = type_name;
        this.attributes = attributes;
        this.systemAttributes = system_attributes;
        this.status = status_name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getSystemAttributes() {
        return systemAttributes;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", getUuid())
                .add("type", getType())
                .add("attributes", getAttributes())
                .add("workflowStatus", getStatus())
                .add("systemAttributes", getSystemAttributes())
                .toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final LinkedObject other = (LinkedObject) obj;
        return Objects.equal(this.getUuid(), other.getUuid())
                && Objects.equal(this.getType(), other.getType())
                && Objects.equal(this.getAttributes(), other.getAttributes())
                && Objects.equal(this.getStatus(), other.getStatus())
                && Objects.equal(this.getSystemAttributes(), other.getSystemAttributes());
   }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                this.getUuid(), this.getType(), this.getAttributes(), this.getStatus(), getSystemAttributes());

    }
}
