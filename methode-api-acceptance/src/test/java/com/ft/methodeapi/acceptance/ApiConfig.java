package com.ft.methodeapi.acceptance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class ApiConfig {

    private final String host;
    private final int port;
    private final int adminPort;

    public ApiConfig(@JsonProperty("host") String host,
                     @JsonProperty("port") int port,
                     @JsonProperty("adminPort") int adminPort) {
        this.host = host;
        this.port = port;
        this.adminPort = adminPort;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getAdminPort() {
        return adminPort;
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("port", port)
                .add("adminPort", adminPort);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
