package com.ft.methodeapi.service.http;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.methodeapi.service.methode.ActionNotPermittedException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.NotFoundException;

import org.junit.Test;


public class EomFileResourceDeleteTest {
    private MethodeFileRepository methodeFileRepository = mock(MethodeFileRepository.class);
    private EomFileResource eomFileResource = new EomFileResource(methodeFileRepository);
    
    @Test
    public void deleteShouldReturn404WhenNotFound() {
        final String uuid = UUID.randomUUID().toString();
        doThrow(new NotFoundException(uuid)).when(methodeFileRepository).deleteTestFileByUuid(uuid);

        try {
            eomFileResource.deleteByUuid(uuid);
            fail("expected exception was not thrown");
        }
        catch (WebApplicationClientException e) {
            assertThat("response status", e.getResponse().getStatus(), equalTo(404));
        }
    }

    @Test
    public void deleteShouldReturn403WhenNotPermitted() {
        final String uuid = UUID.randomUUID().toString();
        doThrow(new ActionNotPermittedException("synthetic permission error")).when(methodeFileRepository).deleteTestFileByUuid(uuid);
        
        try {
            eomFileResource.deleteByUuid(uuid);
            fail("expected exception was not thrown");
        }
        catch (WebApplicationException e) {
            assertThat("response status", e.getResponse().getStatus(), equalTo(403));
        }
    }
}
