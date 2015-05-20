package com.ft.methodeapi.smoke;

import com.ft.methodeapi.acceptance.xml.Xml;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * XmlTest
 *
 * @author Simon.Gibbs
 */
public class XmlTest {

    @Test
    public void  shouldBeAbleToIdentifyGoodXml() throws Exception {
        String goodXml = "<xml />";
        Xml.assertParseable(goodXml);

    }

    @Test(expected = AssertionError.class)
    public void  shouldBeAbleToIdentifyBadXml() throws Exception {
        String badXml = "adrgbjlafbgj,sfbgjldf";
        Xml.assertParseable(badXml);

    }

    @Test
    public void shouldConvertEntitiesToUTF8Characters() {
        String expected = "Lead headline \u00a342m for S&amp;P\u2019s \u201cup 79%\u201d";
        String source = "Lead headline £42m for S&amp;P&#8217;s &#8220;up 79%&#8221;";

        String result = Xml.resolveEntities(source);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldRemoveInsignificantXml()  throws Exception {
        String[] insignificantPaths = {"/nonMatchingPath"};
        Xml.removeInsignificantXml("<xml />", insignificantPaths);
    }

}
