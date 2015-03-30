package com.ft.methodeapi.service.http;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.methodeapi.atc.LastKnownLocation;
import com.ft.methodeapi.service.methode.ActionNotPermittedException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.NotFoundException;
import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

public class EomFileResourceDeleteTest extends ResourceTest {

    private MethodeFileRepository methodeFileRepository;

    LastKnownLocation location = mock(LastKnownLocation.class);

    @Override
    protected void setUpResources() throws Exception {
        methodeFileRepository = mock(MethodeFileRepository.class);
        addResource(new EomFileResource(methodeFileRepository,location));
    }

    @Test
    public void deleteShouldReturn404WhenNotFound() {

        final String uuid = UUID.randomUUID().toString();
        doThrow(new NotFoundException(uuid)).when(methodeFileRepository).deleteTestFileByUuid(uuid);

        final ClientResponse clientResponse = client().resource("/eom-file/").path(uuid).header(TransactionIdUtils.TRANSACTION_ID_HEADER, "tid_test").delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }


    @Test
    public void deleteShouldReturn403WhenNotPermitted() {

        final String uuid = UUID.randomUUID().toString();
        doThrow(new ActionNotPermittedException("synthetic permission error")).when(methodeFileRepository).deleteTestFileByUuid(uuid);

        final ClientResponse clientResponse = client().resource("/eom-file/").path(uuid).header(TransactionIdUtils.TRANSACTION_ID_HEADER, "tid_test").delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(403)));
    }
}
