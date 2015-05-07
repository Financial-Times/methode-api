package com.ft.methodeapi.model;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;


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

	@Test
	public void shouldUseSystemAttributesFromBuilder() {
		final EomFile eomFile = new EomFile.Builder()
				.withSystemAttributes("some attributes")
				.build();
		assertThat("eomFile", eomFile, hasProperty("systemAttributes", equalTo("some attributes")));
	}

	@Test
	public void shouldUseWorkflowStatusFromBuilder() {
		final EomFile eomFile = new EomFile.Builder()
				.withWorkflowStatus("Stories/WebReady")
				.build();
		assertThat("eomFile", eomFile, hasProperty("workflowStatus", equalTo("Stories/WebReady")));
	}
	
    @Test
    public void thatJsonOmitsNullLinkedObjects()
            throws Exception {
        
        String uuid = UUID.randomUUID().toString();
        String type = "junittype";
        byte[] value = "junitvalue".getBytes();
        String attributes = "junitattrs";
        String workflowStatus = "junitstatus";
        String systemAttributes = "junitsysattrs";
        String usageTickets = "junitusagetickets";
        
        final EomFile eomFile =
                new EomFile(uuid, type, value, attributes, workflowStatus, systemAttributes, usageTickets);
        
        String json = (new ObjectMapper()).writer().writeValueAsString(eomFile);
        assertThat("eomFile without linked objects", json, not(containsString("\"linkedObjects\"")));
    }
    
    @Test
    public void thatJsonIncludesEmptyLinkedObjects()
            throws Exception {
        
        String uuid = UUID.randomUUID().toString();
        String type = "junittype";
        byte[] value = "junitvalue".getBytes();
        String attributes = "junitattrs";
        String workflowStatus = "junitstatus";
        String systemAttributes = "junitsysattrs";
        String usageTickets = "junitusagetickets";
        List<LinkedObject> linkedObjects = Collections.emptyList();
        final EomFile eomFile = new EomFile(
                uuid, type, value, attributes, workflowStatus, systemAttributes, usageTickets, linkedObjects);
        
        String json = (new ObjectMapper()).writer().writeValueAsString(eomFile);
        assertThat("eomFile with empty linked objects", json, containsString("\"linkedObjects\""));
    }
}
