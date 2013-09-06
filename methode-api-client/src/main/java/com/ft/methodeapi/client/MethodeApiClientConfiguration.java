package com.ft.methodeapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MethodeApiClientConfiguration {

    private final String methodeApiHost;
    private final int methodeApiPort;

    public MethodeApiClientConfiguration(@JsonProperty("apiHost") String methodeApiHost,
                                         @JsonProperty("apiPort") int methodeApiPort) {
        this.methodeApiHost = methodeApiHost;
        this.methodeApiPort = methodeApiPort;
    }

    public String getMethodeApiHost() {
        return methodeApiHost;
    }

    public int getMethodeApiPort() {
        return methodeApiPort;
    }
}
