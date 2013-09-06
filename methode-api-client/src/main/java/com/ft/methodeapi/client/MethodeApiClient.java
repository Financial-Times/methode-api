package com.ft.methodeapi.client;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.ft.methodeapi.model.EomFile;
import com.sun.jersey.api.client.Client;

public class MethodeApiClient {

    private final Client jerseyClient;
    private final String apiHost;
    private final int apiPort;

    private final UriBuilder findFileByUuidUriBuilder;

    public MethodeApiClient(Client jerseyClient, String apiHost, int apiPort) {
        this.jerseyClient = jerseyClient;
        this.apiHost = apiHost;
        this.apiPort = apiPort;

        this.findFileByUuidUriBuilder = UriBuilder.fromPath("eom-file")
                .path("{uuid}")
                .scheme("http")
                .host(apiHost)
                .port(apiPort);
    }

    public EomFile findFileByUuid(String uuid) {
        URI findFileByUuidUri = findFileByUuidUri(uuid);
        return jerseyClient
                .resource(findFileByUuidUri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(EomFile.class);
    }

    private URI findFileByUuidUri(String uuid) {
        return findFileByUuidUriBuilder.clone().build(uuid);
    }

}
