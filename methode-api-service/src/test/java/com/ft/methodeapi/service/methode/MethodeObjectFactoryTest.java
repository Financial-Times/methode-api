package com.ft.methodeapi.service.methode;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * MethodeObjectFactoryTest
 *
 * @author Simon.Gibbs
 */
public class MethodeObje ctFactoryTest {

    private MethodeObjectFactory methodeObjectFactory;

    @Before
    public void setupFactory() {
        methodeObjectFactory = MethodeObjectFactory.builder().build();
    }

    @Test
    public void failingTest() {
        assertTrue(false);
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
