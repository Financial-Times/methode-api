package com.ft.methodeapi.client;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.validation.PortRange;
import org.hibernate.validator.constraints.NotEmpty;

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

    @NotEmpty
    public String getMethodeApiHost() {
        return methodeApiHost;
    }

    @PortRange
    public int getMethodeApiPort() {
        return methodeApiPort;
    }

    @Valid
    @NotNull
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }
}
