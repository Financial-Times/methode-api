package com.ft.methodeapi.acceptance.util;


import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class FileUtil {
    public static String loadFile(String resourceName) {
        String bodyFromFile = "";
        try {
            bodyFromFile = IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName), "UTF-8");

            // because what we get back from the API uses UNIX line encodings, but when working locally on Windows, the expected file will have \r\n
            if (System.getProperty("line.separator").equals("\r\n")) {
                bodyFromFile = bodyFromFile.replace("\r", "");
            }

        } catch (IOException e) {
            throw new RuntimeException("Unexpected error reading from content in JAR file", e);
        }

        return bodyFromFile;
    }
}
