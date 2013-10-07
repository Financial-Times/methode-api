package com.ft.methodeapi.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.yammer.dropwizard.testing.ResourceTest;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Test;

import java.net.SocketException;
import java.net.SocketTimeoutException;

public class MethodeApiClientTest extends ResourceTest {

    private MethodeFileRepository methodeFileRepository;

    @Override
    protected void setUpResources() throws Exception {
        methodeFileRepository = mock(MethodeFileRepository.class);
        addResource(new EomFileResource(methodeFileRepository));
    }

    @Test
    public void canRetrieveEomFile() {

        final byte[] fileBytes = "blah, blah, blah".getBytes();
        when(methodeFileRepository.findFileByUuid(any(String.class))).thenReturn(Optional.of(new EomFile("asdf", "someType", fileBytes, "some attributes")));

        EomFile eomFile = new MethodeApiClient(client(), "localhost", 1234).findFileByUuid("asdsfgdg");

        assertArrayEquals(fileBytes, eomFile.getValue());
    }

    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForSocketTimeout() {
        Client mockClient = primeClientToExperienceExceptionWithSpecificRootCause(new SocketTimeoutException());
        excerciseClient(mockClient);
    }

    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForAnyOtherIssueWithTheTcpSocket() {

        Client mockClient = primeClientToExperienceExceptionWithSpecificRootCause(new SocketException());
        excerciseClient(mockClient);

    }

    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForConnectionTimeout() {

        Client mockClient = primeClientToExperienceExceptionWithSpecificRootCause(new ConnectTimeoutException());
        excerciseClient(mockClient);

    }

    private void excerciseClient(Client mockClient) {
        (new MethodeApiClient(mockClient, "localhost", 1234)).findFileByUuid("035a2fa0-d988-11e2-bce1-002128161462");
    }
    private Client primeClientToExperienceExceptionWithSpecificRootCause(Exception rootCause) {
        ClientHandler handler = mock(ClientHandler.class);
        Client mockClient = new Client(handler);

        when(handler.handle(any(ClientRequest.class))).thenThrow( new ClientHandlerException(rootCause));
        return mockClient;
    }

}
