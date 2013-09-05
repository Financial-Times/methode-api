package com.ft.methodeapi.service;

public class Content {

    private final byte[] fileContent;

    public Content(byte[] fileContent) {
//        this.fileContent = Arrays.copyOf(fileContent, fileContent.length);
        this.fileContent = fileContent;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
