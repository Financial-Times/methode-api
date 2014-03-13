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
    boolean amIActive;
    Map<DataCentre,String> methodeIps;

    public WhereIsItResponse(boolean amIActive, DataCentre active, Map<DataCentre, String> methodeIps) {
        this.amIActive = amIActive;
        this.active = active;
        this.methodeIps = methodeIps;
    }

    public boolean isAmIActive() {
        return amIActive;
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
