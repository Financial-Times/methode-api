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

        assertThat(result.getAttributes(),containsString("U\u004B Elec\u0074ion 2\u00301\u0037"));
    }

}
