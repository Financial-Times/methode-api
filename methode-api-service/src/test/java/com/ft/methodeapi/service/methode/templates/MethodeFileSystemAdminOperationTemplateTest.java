package com.ft.methodeapi.service.methode.templates;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import EOM.FileSystemAdmin;
import EOM.Repository;
import EOM.Session;

import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;


@RunWith(MockitoJUnitRunner.class)
public class MethodeFileSystemAdminOperationTemplateTest {

    @Mock
    private ORB mockOrb;

    @Mock
    private NamingContextExt mockNamingService;

    @Mock
    private Repository mockRepository;

    @Mock
    private Session mockSession;

    @Mock
    private MethodeObjectFactory mockMethodeObjectFactory;
    

    @Before
    public void setupMocks() {
        when(mockMethodeObjectFactory.createOrb()).thenReturn(mockOrb);
        when(mockMethodeObjectFactory.createNamingService(mockOrb)).thenReturn(mockNamingService);
        when(mockMethodeObjectFactory.createRepository(mockNamingService)).thenReturn(mockRepository);
        when(mockMethodeObjectFactory.createSession(mockRepository)).thenReturn(mockSession);
    }

    @Test
    public void shouldMaybeCloseAllObjectsAfterSuccessfulOperation() {
        MethodeFileSystemAdminOperationTemplate<Object> sut = new MethodeFileSystemAdminOperationTemplate<>(mockMethodeObjectFactory);

        sut.doOperation(new MethodeFileSystemAdminOperationTemplate.FileSystemAdminCallback<Object>() {
            @Override
            public Object doOperation(FileSystemAdmin session) {
                return null;
            }
        });

        verify(mockMethodeObjectFactory).maybeCloseSession(mockSession);
        verify(mockMethodeObjectFactory).maybeCloseRepository(mockRepository);
        verify(mockMethodeObjectFactory).maybeCloseNamingService(mockNamingService);
        verify(mockMethodeObjectFactory).maybeCloseOrb(mockOrb);

    }

    @Test
    public void shouldMaybeCloseAllObjectsAfterFailedOperation() {
        MethodeFileSystemAdminOperationTemplate<Object> sut = new MethodeFileSystemAdminOperationTemplate<>(mockMethodeObjectFactory);

        try {
            sut.doOperation(new MethodeFileSystemAdminOperationTemplate.FileSystemAdminCallback<Object>() {
                @Override
                public Object doOperation(FileSystemAdmin session) {
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
