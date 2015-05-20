package com.ft.methodeapi.smoke;

import com.ft.methodeapi.acceptance.ReferenceLists;
import com.ft.methodeapi.model.EomFile;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ReferenceListsTest {
    @Test
    public void shouldMakePublishedListAvailable() {
        assertThat(ReferenceLists.publishedList(), notNullValue());
    }

    @Test
    public void shouldNotBorkMultibyteCharacters() {
        EomFile result = ReferenceLists.publishedList().build().getEomFile();

        assertThat(result.getAttributes(),containsString("Home\u002DU\u004B \u0054op \u0053tories \u0045dit"));
    }

}
