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

    public LinkedObject(@JsonProperty("uuid") String uuid, @JsonProperty("type") String type) {
        this.uuid = uuid;
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("type", type)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final LinkedObject other = (LinkedObject) obj;
        return Objects.equal(this.uuid, other.uuid)
                && Objects.equal(this.type, other.type);

    }

    @Override
    public int hashCode() {

        return Objects.hashCode(
                this.uuid, this.type);

    }
}
