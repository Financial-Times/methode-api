package com.ft.methodeapi.client;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.yammer.dropwizard.client.JerseyClientConfiguration;

public class MethodeApiClientConfiguration {

    private final String methodeApiHost;
    private final int methodeApiPort;
    private final JerseyClientConfiguration jerseyClientConfiguration;

    public MethodeApiClientConfiguration(@JsonProperty("apiHost") String methodeApiHost,
                                         @JsonProperty("apiPort") int methodeApiPort,
                                         @JsonProperty("jerseyClient") Optional<JerseyClientConfiguration> jerseyClientConfiguration) {
        this.methodeApiHost = methodeApiHost;
        this.methodeApiPort = methodeApiPort;
        this.jerseyClientConfiguration = jerseyClientConfiguration.or(new JerseyClientConfiguration());
    }

    public String getMethodeApiHost() {
        return methodeApiHost;
    }

    public int getMethodeApiPort() {
        return methodeApiPort;
    }

    @Valid
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }
}
