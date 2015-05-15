package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.model.LinkedObject;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.List;

public class ReferenceLists {

    public static final String EOM_COMPOUND_STORY_TYPE = "EOM::CompoundStory";

    private static final String exampleListXmlTemplate = readFromFile("ListWithEverything.xml");
    private static final String exampleAttributesXml = readFromFile("ListWithEverythingAttributes.xml");
    private static final String exampleWebChannelXml = readFromFile("ListWithEverythingSystemAttributes.xml");


    public static MethodeContent.ContentBuilder publishedList() {
        List<LinkedObject> linkedObjects = ImmutableList.of(new LinkedObject("bd527556-d7c5-11e4-849b-00144feab7de", EOM_COMPOUND_STORY_TYPE));
//               new LinkedObject("fc2b257a-d90f-11e4-b907-00144feab7de", EOM_COMPOUND_STORY_TYPE));// TODO - set these to sensible values, add a second linkedObject
        return new MethodeContent.ListBuilder()
                .withArticleXml(exampleListXmlTemplate)
                .withAttributes(exampleAttributesXml)
                .withWorkflowStatus(MethodeContent.CLEARED)
                .withChannel(exampleWebChannelXml)
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
