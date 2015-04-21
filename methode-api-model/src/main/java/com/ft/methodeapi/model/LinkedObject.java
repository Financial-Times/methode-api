package com.ft.methodeapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
