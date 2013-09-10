package com.ft.methodeapi;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class MethodeApiConfiguation extends Configuration {

    private final MethodeConnectionConfiguration methodeConnectionConfiguration;
    private final long maxPingMillis;

    public MethodeApiConfiguation(@JsonProperty("methodeConnection") MethodeConnectionConfiguration methodeConnectionConfiguration,
                                  @JsonProperty("maxPingMillis") long maxPingMillis) {
        this.methodeConnectionConfiguration = methodeConnectionConfiguration;
        this.maxPingMillis = maxPingMillis;
    }

    @Valid
    @NotNull
    public MethodeConnectionConfiguration getMethodeConnectionConfiguration() {
        return methodeConnectionConfiguration;
    }

    @Min(1L)
    public long getMaxPingMillis() {
        return maxPingMillis;
    }
}
