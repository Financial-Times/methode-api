package com.ft.methodeapi.service.methode.templates;

import EOM.Repository;
import EOM.Session;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

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
    }

    @Test
    public void shouldMaybeCloseAllObjectsAfterSuccessfulOperation() {
        MethodeRepositoryOperationTemplate<Object> sut = new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory);

        sut.doOperation(new MethodeRepositoryOperationTemplate.RepositoryCallback<Object>() {
            @Override
            public Object doOperation(Repository repository) {
                return null;
            }
        });

        verify(mockMethodeObjectFactory).maybeCloseRepository(mockRepository);
        verify(mockMethodeObjectFactory).maybeCloseNamingService(mockNamingService);
        verify(mockMethodeObjectFactory).maybeCloseOrb(mockOrb);

    }

    @Test
    public void shouldMaybeCloseAllObjectsAfterFailedOperation() {
        MethodeRepositoryOperationTemplate<Object> sut = new MethodeRepositoryOperationTemplate<>(mockMethodeObjectFactory);

        try {
            sut.doOperation(new MethodeRepositoryOperationTemplate.RepositoryCallback<Object>() {
                @Override
                public Object doOperation(Repository repository) {
                    throw new RuntimeException("Simulated exception");
                }
            });
            fail("Expected an exception");
        } catch(RuntimeException re) {
            // good.
        }

        verify(mockMethodeObjectFactory).maybeCloseRepository(mockRepository);
        verify(mockMethodeObjectFactory).maybeCloseNamingService(mockNamingService);
        verify(mockMethodeObjectFactory).maybeCloseOrb(mockOrb);

    }



}
