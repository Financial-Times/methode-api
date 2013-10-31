package com.ft.methodeapi;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.methodeapi.service.methode.MethodeConnectionConfiguration;
import com.google.common.base.Objects;
import com.yammer.dropwizard.config.Configuration;

public class MethodeApiConfiguration extends Configuration {

    private final MethodeConnectionConfiguration methodeConnectionConfiguration;
    private final long maxPingMillis;

    public MethodeApiConfiguration(@JsonProperty("methodeConnection") MethodeConnectionConfiguration methodeConnectionConfiguration,
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
    
    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("methodeConnectionConfiguration", methodeConnectionConfiguration)
                .add("maxPingMillis", maxPingMillis);
    }
    
    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
