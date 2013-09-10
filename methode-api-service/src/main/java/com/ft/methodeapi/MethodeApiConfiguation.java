package com.ft.methodeapi;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class MethodeApiConfiguation extends Configuration {

    private final MethodeConnectionConfiguration methodeConnectionConfiguration;
    private final long maxPingMillis;

    public MethodeApiConfiguation(@JsonProperty("methodeConnection") MethodeConnectionConfiguration methodeConnectionConfiguration,
                                  @JsonProperty("maxPingMillis") long maxPingMillis) {
        this.methodeConnectionConfiguration = methodeConnectionConfiguration;
        this.maxPingMillis = maxPingMillis;
    }

    @Valid
    @NotEmpty
    public MethodeConnectionConfiguration getMethodeConnectionConfiguration() {
        return methodeConnectionConfiguration;
    }

    @Min(1L)
    public long getMaxPingMillis() {
        return maxPingMillis;
    }
}
