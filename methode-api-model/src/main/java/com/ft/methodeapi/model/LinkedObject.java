package com.ft.methodeapi.model;

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
    private String workflowStatus = null;

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
        this.workflowStatus = status_name;
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

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("type", type)
                .add("attributes", attributes)
                .add("workflowStatus", workflowStatus)
                .add("systemAttributes", systemAttributes)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final LinkedObject other = (LinkedObject) obj;
        return Objects.equal(uuid, other.uuid)
                && Objects.equal(type, other.type)
                && Objects.equal(attributes, other.attributes)
                && Objects.equal(workflowStatus, other.workflowStatus)
                && Objects.equal(systemAttributes, other.systemAttributes);
   }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid, type, attributes, workflowStatus, systemAttributes);
    }
}
