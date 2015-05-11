package com.ft.methodeapi.acceptance;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

public class ReferenceArticles {

    private static final String exampleArticleXmlTemplate = readFromFile("ArticleWithEverything.xml");
    private static final String exampleAttributesXml = readFromFile("ArticleWithEverythingAttributes.xml");
    private static final String exampleWebChannelXml = readFromFile("ArticleWithEverythingSystemAttributes.xml");

    public static MethodeContent.Builder publishedKitchenSinkArticle() {
        return MethodeContent.builder(exampleArticleXmlTemplate, exampleAttributesXml, MethodeContent.WEB_READY, exampleWebChannelXml).published();
    }

    private static String readFromFile(String resourceName) {
        String bodyFromFile = "";
        try {
            bodyFromFile = Resources.toString(ReferenceArticles.class.getResource(resourceName), Charsets.UTF_8);

            // because what we get back from the API uses UNIX line encodings, but when working locally on Windows, the expected file will have \r\n
            if (System.getProperty("line.separator").equals("\r\n")) {
                bodyFromFile = bodyFromFile.replace("\r", "");
            }

        } catch (IOException e) {
            throw new RuntimeException("Unexpected error reading from content in JAR file",e);
        }

        return bodyFromFile;
    }
}
