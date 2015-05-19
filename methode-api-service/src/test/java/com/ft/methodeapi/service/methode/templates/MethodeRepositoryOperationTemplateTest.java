package com.ft.methodeapi.service.methode.templates;

import EOM.Repository;
import EOM.Session;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MethodeRepositoryOperationTemplateTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodeRepositoryOperationTemplateTest {
    private static final MethodeRepositoryOperationTemplate.RepositoryCallback<Object> NOP =
            new MethodeRepositoryOperationTemplate.RepositoryCallback<Object>() {
                @Override
                public Object doOperation(Repository repository) {
                    return null;
                }
            };
    
    private static final MethodeRepositoryOperationTemplate.RepositoryCallback<Object> ERR =
            new MethodeRepositoryOperationTemplate.RepositoryCallback<Object>() {
                @Override
                public Object doOperation(Repository repository) {
                    throw new RuntimeException("Simulated exception");
                }
            };
    
    @Mock
    ORB mockOrb;

    @Mock
    NamingContextExt mockNamingService;

    @Mock
    Repository mockRepository;

    @Mock
    Session mockSession;

    @Mock
    MethodeObjectFactory mockMethodeObjectFactory;

    @Before
    public void setupMocks() {
        when(mockMethodeObjectFactory.createOrb()).thenReturn(mockOrb);
        when(mockMethodeObjectFactory.createNamingService(mockOrb)).thenReturn(mockNamingService);
        when(mockMethodeObjectFactory.createRepository(mockNamingService)).thenReturn(mockRepository);

        when(mockMethodeObjectFactory.createSession(mockRepository)).thenThrow(new RuntimeException("unexpected call"));
        
        when(mockMethodeObjectFactory.refreshMethodeLocation()).thenReturn("127.0.0.1");
    }

    @Test
    public void shouldMaybeCloseAllObjectsAfterSuccessfulOperation() {
        MethodeRepositoryOperationTemplate<Object> sut = new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory);

        sut.doOperation(NOP);

        verify(mockMethodeObjectFactory).maybeCloseRepository(mockRepository);
        verify(mockMethodeObjectFactory).maybeCloseNamingService(mockNamingService);
        verify(mockMethodeObjectFactory).maybeCloseOrb(mockOrb);

    }

    @Test
    public void shouldMaybeCloseAllObjectsAfterFailedOperation() {
        MethodeRepositoryOperationTemplate<Object> sut = new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory);

        try {
            sut.doOperation(ERR);
            fail("Expected an exception");
        } catch(RuntimeException re) {
            // good.
        }

        verify(mockMethodeObjectFactory).maybeCloseRepository(mockRepository);
        verify(mockMethodeObjectFactory).maybeCloseNamingService(mockNamingService);
        verify(mockMethodeObjectFactory).maybeCloseOrb(mockOrb);

    }
    
    @Test
    public void thatDoOperationRecordsMetrics() {
        String timerName = "thatDoOperationRecordsMetrics";
        MethodeRepositoryOperationTemplate<Object> template =
                new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory,
                        MethodeRepositoryOperationTemplateTest.class, timerName);
        
        template.doOperation(NOP);
        
        String metricName = timerName + "@127.0.0.1";
        Timer opTimer = Metrics.defaultRegistry().newTimer(MethodeRepositoryOperationTemplateTest.class, metricName);
        
        assertThat("expected operation count", (Long)opTimer.count(), equalTo(1L));
    }
    
    @Test
    public void thatDoOperationWithExceptionRecordsMetrics() {
        String timerName = "thatDoOperationWithExceptionRecordsMetrics";
        MethodeRepositoryOperationTemplate<Object> template =
                new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory,
                        MethodeRepositoryOperationTemplateTest.class, timerName);
        
        try {
            template.doOperation(ERR);
            fail("Expected an exception");
        } catch(RuntimeException re) {
            // good.
        }
        finally {
            String metricName = timerName + "@127.0.0.1";
            Timer opTimer = Metrics.defaultRegistry().newTimer(MethodeRepositoryOperationTemplateTest.class, metricName);
            
            assertThat("expected operation count", (Long)opTimer.count(), equalTo(1L));
        }
    }
    
    @Test
    public void thatDoOperationRecordsMetricsPerIP() {
        when(mockMethodeObjectFactory.refreshMethodeLocation())
            .thenReturn("127.0.0.1")
            .thenReturn("127.0.0.2")
            .thenReturn("127.0.0.1");
        
        String timerName = "thatDoOperationRecordsMetricsPerIP";
        MethodeRepositoryOperationTemplate<Object> template =
                new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory,
                        MethodeRepositoryOperationTemplateTest.class, timerName);
        
        template.doOperation(NOP); // calling 127.0.0.1
        template.doOperation(NOP); // calling 127.0.0.2
        template.doOperation(NOP); // calling 127.0.0.1 again
        
        String metricName = timerName + "@127.0.0.1";
        Timer opTimer = Metrics.defaultRegistry().newTimer(MethodeRepositoryOperationTemplateTest.class, metricName);
        
        assertThat("expected operation count for 127.0.0.1", (Long)opTimer.count(), equalTo(2L));
        
        metricName = timerName + "@127.0.0.2";
        opTimer = Metrics.defaultRegistry().newTimer(MethodeRepositoryOperationTemplateTest.class, metricName);
        
        assertThat("expected operation count for 127.0.0.2", (Long)opTimer.count(), equalTo(1L));
    }
}
