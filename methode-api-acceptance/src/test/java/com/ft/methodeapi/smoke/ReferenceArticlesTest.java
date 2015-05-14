package com.ft.methodeapi.smoke;

import com.ft.methodeapi.acceptance.ReferenceArticles;
import com.ft.methodeapi.model.EomFile;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * ReferenceArticlesTest
 *
 * @author Simon.Gibbs
 */
public class ReferenceArticlesTest {

    @Test
    public void shouldMakeKitchenSinkArticleAvailable() {
        assertThat(ReferenceArticles.publishedKitchenSinkArticle(), notNullValue());
    }

    @Test
    public void shouldNotBorkMultibyteCharacters() {
        EomFile result = ReferenceArticles.publishedKitchenSinkArticle().build().getEomFile();

        assertThat(result.getAttributes(),containsString("Lead headline \u00a342m for S&amp;P\u2019s \u201cup 79%\u201d"));
    }
}
