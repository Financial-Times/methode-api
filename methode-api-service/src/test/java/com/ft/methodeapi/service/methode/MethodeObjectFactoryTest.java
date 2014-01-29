package com.ft.methodeapi.service.methode;

import EOM.PermissionDenied;
import EOM.Session;
import com.ft.methodeapi.service.methode.connection.DefaultMethodeObjectFactory;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * MethodeObjectFactoryTest
 *
 * @author Simon.Gibbs
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodeObjectFactoryTest {

    private MethodeObjectFactory methodeObjectFactory;

    @Mock
    Session mockEomSession;


    @Before
    public void setupFactory() {
        methodeObjectFactory = DefaultMethodeObjectFactory.builder().build();
    }

    @Test
    public void shouldNotFailWhenMaybeClosingNullOrb() {
        methodeObjectFactory.maybeCloseOrb(null);
    }

    @Test
    public void shouldNotFailWhenMaybeClosingNullNamingService() {
        methodeObjectFactory.maybeCloseNamingService(null);
    }

    @Test
    public void shouldNotFailWhenMaybeClosingNullRepository() {
        methodeObjectFactory.maybeCloseRepository(null);
    }

    @Test
    public void shouldNotFailWhenMaybeClosingNullSession() {
        methodeObjectFactory.maybeCloseSession(null);
    }

    /**
     * Trying to close a session that already has fundamental problems should not cause a new failure condition
     */
    @Test
    public void shouldSwallowPermissionDeniedExceptionOnClosing() {

        givenASessionPrimedToThrow(new PermissionDenied());

        methodeObjectFactory.maybeCloseSession(mockEomSession);

        try {
            verify(mockEomSession).destroy();
        } catch (Throwable t) {
            assertThat(t, instanceOf(PermissionDenied.class));
        }

    }

    private void givenASessionPrimedToThrow(Exception exception) {
        try {
            doThrow(exception).when(mockEomSession).destroy();
        } catch (Throwable objectLocked) {
            fail("Exception during mock object setup");
        }
    }


}
