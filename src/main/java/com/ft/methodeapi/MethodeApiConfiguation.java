package com.ft.methodeapi;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class MethodeApiConfiguation extends Configuration {

    private final MethodeConnectionConfiguration methodeConnectionConfiguration;
    private final long maxPingMillis;

    @JsonCreator
    public MethodeApiConfiguation(
            @Valid @JsonProperty("methodeConnection") MethodeConnectionConfiguration methodeConnectionConfiguration,
            @Valid @JsonProperty("maxPingMillis") @Min(1L) long maxPingMillis
    ) {
        this.methodeConnectionConfiguration = methodeConnectionConfiguration;
        this.maxPingMillis = maxPingMillis;
    }

    public MethodeConnectionConfiguration getMethodeConnectionConfiguration() {
        return methodeConnectionConfiguration;
    }

    public long getMaxPingMillis() {
        return maxPingMillis;
    }
}
