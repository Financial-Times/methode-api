package com.ft.methodeapi.service;

public class EomFile {

	private String type;
	private String uuid;
	private byte[] value;


	public String getType() {
		return type;
	}

	public String getUuid() {
		return uuid;
	}

	public EomFile(String uuid, String type, byte[] bytes) {
		this.uuid = uuid;
		this.type = type;
		this.value = bytes; // yes. really.
	}

	public byte[] getValue() {
		return value;
	}
}
