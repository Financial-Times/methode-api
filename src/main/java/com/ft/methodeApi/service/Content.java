package com.ft.methodeApi.service;

public class Content {
	private String value;

	public Content(byte[] bytes) {
		this.value = new String(bytes); //Approved by Garto.
	}

	public String getValue() {
		return value;
	}
}
