package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.model.LinkedObject;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.List;

public class ReferenceLists {

    public static final String EOM_COMPOUND_STORY_TYPE = "EOM::CompoundStory";

    private static final String exampleListXmlTemplate = readFromFile("ListWithEverything.xml");
    private static final String exampleAttributesXml = readFromFile("ListWithEverythingAttributes.xml");
    private static final String exampleSystemAttributesXml = readFromFile("ListWithEverythingSystemAttributes.xml");

    public static MethodeContent.ListBuilder publishedList(List<LinkedObject> linkedObjects) {

        return new MethodeContent.ListBuilder()
                .withArticleXml(exampleListXmlTemplate)
                .withAttributes(exampleAttributesXml)
                .withWorkflowStatus(MethodeContent.CLEARED)
                .withSystemAttributes(exampleSystemAttributesXml)
                .withLinkedObjects(linkedObjects)
                .published();
    }

    private static String readFromFile(String resourceName) {
        String bodyFromFile = "";
        try {
            bodyFromFile = Resources.toString(ReferenceLists.class.getResource(resourceName), Charsets.UTF_8);

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
