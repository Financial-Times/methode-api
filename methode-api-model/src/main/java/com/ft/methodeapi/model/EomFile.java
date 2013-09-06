package com.ft.methodeapi.model;

public class EomFile {

    private String uuid;
    private String type;
	private byte[] value;


    public EomFile(String uuid, String type, byte[] bytes) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes; // yes. really.
    }

    public String getUuid() {
        return uuid;
    }

	public String getType() {
		return type;
	}

	public byte[] getValue() {
		return value;
	}
}
