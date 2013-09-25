package com.ft.methodeapi.client;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.base.Optional;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

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

}
