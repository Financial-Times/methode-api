package com.ft.methodeapi.service.methode;

import EOM.PermissionDenied;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.http.EomFileResource;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MethodeOutageTest
 *
 * @author Simon.Gibbs
 */
public class MethodeOutageTest extends ResourceTest {

    private MethodeFileRepository methodeFileRepository;

    @Override
    protected void setUpResources() throws Exception {
        methodeFileRepository = mock(MethodeFileRepository.class);
        addResource(new EomFileResource(methodeFileRepository));
    }


    @Test
    public void shouldReturn503WhenMethodeIsDown() {
        when(methodeFileRepository.findFileByUuid(anyString())).thenThrow(new org.omg.CORBA.TRANSIENT("Synthetic exception"));

        // avoid dependency cycles by NOT using the official client
        try {
            client().resource("/eom-file/").path(UUID.randomUUID().toString()).accept(MediaType.APPLICATION_JSON_TYPE).get(EomFile.class);
        } catch(UniformInterfaceException uie) {
            assertThat(uie.getResponse().getStatus(),is(503));
        }

    }

    @Test
    public void shouldReturn503WhenMethodeCheckedExceptionOccurs() {
        when(methodeFileRepository.findFileByUuid(anyString())).thenThrow(new MethodeException("Synthetic exception", new PermissionDenied()));

        // avoid dependency cycles by NOT using the official client
        try {
            client().resource("/eom-file/").path(UUID.randomUUID().toString()).accept(MediaType.APPLICATION_JSON_TYPE).get(EomFile.class);
        } catch(UniformInterfaceException uie) {
            assertThat(uie.getResponse().getStatus(),is(503));
        }

    }

}
