package com.ft.methodeapi.service.methode;

import org.junit.Before;
import org.junit.Test;

/**
 * MethodeObjectFactoryTest
 *
 * @author Simon.Gibbs
 */
public class MethodeObjectFactoryTest {

    private MethodeObjectFactory methodeObjectFactory;

    @Before
    public void setupFactory() {
        methodeObjectFactory = MethodeObjectFactory.builder().build();
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

}
