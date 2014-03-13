package com.ft.methodeapi.atc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * WhereIsItResponse
 *
 * @author Simon.Gibbs
 */
public class WhereIsItResponse {

    DataCentre active;
    Map<DataCentre,String> methodeIps;

    public WhereIsItResponse(DataCentre active, Map<DataCentre, String> methodeIps) {
        this.active = active;
        this.methodeIps = methodeIps;
    }

    @JsonProperty
    public DataCentre getActive() {
        return active;
    }

    @JsonProperty
    public Map<DataCentre, String> getMethodeIps() {
        return methodeIps;
    }
}
