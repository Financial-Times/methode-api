package com.ft.methodeapi.atc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * WhereIsMethodeResponse
 *
 * @author Simon.Gibbs
 */
public class WhereIsMethodeResponse {

    private final DateTime timestamp;
    private final DataCentre active;
    private final boolean amIActive;
    private final Map<DataCentre,String> methodeIps;

    public WhereIsMethodeResponse(boolean amIActive, DataCentre active, Map<DataCentre, String> methodeIps) {
        this.amIActive = amIActive;
        this.active = active;
        this.methodeIps = methodeIps;
        this.timestamp = DateTime.now();
    }

    @JsonProperty
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

    @JsonProperty @JsonSerialize(using=ISO8601DateFormatter.class)
    public DateTime getTimestamp() {
        return timestamp;
    }
}
