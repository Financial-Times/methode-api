package com.ft.methodeapi.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EomFileTest {

    @Test
    public void shouldUseValueFromBuilder() {
        final EomFile eomFile = new EomFile.Builder()
                .withValue(new byte[]{1, 2, 3})
                .build();
        assertArrayEquals(new byte[]{1, 2, 3}, eomFile.getValue());
    }

    @Test
    public void shouldUseAttributesFromBuilder() {
        final EomFile eomFile = new EomFile.Builder()
                .withAttributes("some attributes")
                .build();
        assertThat("eomFile", eomFile, hasProperty("attributes", equalTo("some attributes")));
    }
}
