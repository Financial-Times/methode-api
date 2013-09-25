package com.ft.methodeapi.model;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class EomFileTest {

    @Test
    public void shouldUseValueFromBuilder() {
        final EomFile eomFile = new EomFile.Builder()
                .withValue(new byte[]{1, 2, 3})
                .build();
        assertArrayEquals(new byte[]{1, 2, 3}, eomFile.getValue());
    }
}
