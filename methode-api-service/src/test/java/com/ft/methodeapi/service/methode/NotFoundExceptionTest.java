package com.ft.methodeapi.service.methode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NotFoundExceptionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void uuidIsRequired() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("uuid must not be null"));
        new NotFoundException(null);
    }

    @Test
    public void uuidIsAvailable() {
        final String uuid = UUID.randomUUID().toString();
        final NotFoundException notFoundException = new NotFoundException(uuid);
        assertThat("not found exception", notFoundException, hasProperty("uuid", equalTo(uuid)));
    }

    @Test
    public void messageIsGenerated() {
        final String uuid = UUID.randomUUID().toString();
        final NotFoundException notFoundException = new NotFoundException(uuid);
        assertThat("not found exception", notFoundException, hasProperty("message", equalTo(uuid + " not found")));
    }
}
