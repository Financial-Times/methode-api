package com.ft.methodeapi.service.methode;

import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.codahale.metrics.health.HealthCheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MethodeHealthCheckTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodeHealthCheckTest {

    @Mock
    MethodeObjectFactory mof;

    @Mock
    Repository repository;

    @Mock
    MethodeFileRepository fileRepository;

    @Before
    public void setUp() {
        when(mof.createRepository(any(NamingContextExt.class))).thenReturn(repository);
        when(mof.createOrb()).thenReturn(mock(ORB.class));
        when(mof.createNamingService(any(ORB.class))).thenReturn(mock(NamingContextExt.class));
        when(mof.createSession(any(Repository.class))).thenReturn(mock(Session.class));
    }

    @Test
     public void shouldPassIfPingRespondsInstantly() throws Exception {

        MethodePingHealthCheck check = new MethodePingHealthCheck(mof,100);

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy(),is(true));
    }

    @Test
    public void shouldFailIfPingRespondsSlowly() throws Exception {

        MethodePingHealthCheck check = new MethodePingHealthCheck(mof,100);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // ignore
                }
                return null;
            }
        }).when(repository).ping();

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy(),is(false));
    }

    @Test
    public void shouldPassIfSearchRespondsWithoutException() throws Exception {

        MethodeContentRetrievalHealthCheck check = new MethodeContentRetrievalHealthCheck(fileRepository);

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy(),is(true));
    }

    @Test
    public void shouldFailIfSearchRespondsWithException() throws Exception {

        when(fileRepository.findFileByUuid(anyString())).thenThrow(new RuntimeException("Synthetic Exception"));

        MethodeContentRetrievalHealthCheck check = new MethodeContentRetrievalHealthCheck(fileRepository);

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy(),is(false));
    }

}
