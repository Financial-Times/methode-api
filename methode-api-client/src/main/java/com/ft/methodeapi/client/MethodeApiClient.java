package com.ft.methodeapi.client;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.ft.methodeapi.model.EomFile;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodeApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeApiClient.class);

    private final Client jerseyClient;
    private final String apiHost;
    private final int apiPort;

    public MethodeApiClient(Client jerseyClient, String apiHost, int apiPort) {
        this.jerseyClient = jerseyClient;
        this.apiHost = apiHost;
        this.apiPort = apiPort;

    }

    /** It looks like build(...) isn't safe for concurrent use
    /* so this method can be used to create fresh instances for
     * use in a single thread.
     */
    private UriBuilder fileUrlBuilder() {

        return UriBuilder.fromPath("eom-file")
                .path("{uuid}")
                .scheme("http")
                .host(apiHost)
                .port(apiPort);
    }

    public EomFile findFileByUuid(String uuid) {
        final URI fileByUuidUri = fileUrlBuilder().build(uuid);
        LOGGER.debug("making GET request to methode api {}", fileByUuidUri);
        return jerseyClient
                .resource(fileByUuidUri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(EomFile.class);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MethodeApiClientConfiguration configuration;
        private JerseyClientBuilder jerseyClientBuilder = new JerseyClientBuilder();

        public Builder using(MethodeApiClientConfiguration configuration) {
            this.configuration = configuration;
            jerseyClientBuilder.using(configuration.getJerseyClientConfiguration());
            return this;
        }

        public Builder using(Environment environment) {
            jerseyClientBuilder.using(environment);
            return this;
        }

        public MethodeApiClient build() {
            return new MethodeApiClient(jerseyClientBuilder.build(), configuration.getMethodeApiHost(), configuration.getMethodeApiPort());
        }
    }

}
