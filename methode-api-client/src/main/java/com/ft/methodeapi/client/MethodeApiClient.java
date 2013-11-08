package com.ft.methodeapi.client;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.api.jaxrs.client.exceptions.RemoteApiException;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.methodeapi.model.EomFile;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
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

        ClientResponse clientResponse;

        try {
            clientResponse = jerseyClient
                    .resource(fileByUuidUri)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(ClientResponse.class);
        } catch (ClientHandlerException che) {
            Throwable cause = che.getCause();
            if(cause instanceof IOException) {
                throw new ApiNetworkingException(fileByUuidUri,"GET",che);
            }
            throw che;
        }

        int responseStatusCode = clientResponse.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {
            // SUCCESS!
            return clientResponse.getEntity(EomFile.class);
        }

        LOGGER.error("received a {} status code when making a GET request to {}", responseStatusCode, fileByUuidUri);
        ErrorEntity entity = null;
        try {
            entity = clientResponse.getEntity(ErrorEntity.class);
        } catch (Throwable t) {
            LOGGER.warn("Failed to parse ErrorEntity when handling API transaction failure",t);
        }
        throw new RemoteApiException(fileByUuidUri,"GET",responseStatusCode,entity);

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
