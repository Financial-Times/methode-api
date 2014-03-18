package com.ft.methodeapi.atc;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * AtcConfiguration
 *
 * @author Simon.Gibbs
 */
public class AtcConfiguration {

    private Map<DataCentre, String> hostnames;
    private DataCentre iAm;

    public AtcConfiguration(@JsonProperty("iAm") DataCentre iAm, @JsonProperty("hostname") Map<DataCentre, String> hostnames) {
        this.hostnames = hostnames;
        this.iAm = iAm;
    }

    @NotNull
    public DataCentre getiAm() {
        return iAm;
    }

    public Map<DataCentre, String> getHostnames() {
        return hostnames;
    }

}
