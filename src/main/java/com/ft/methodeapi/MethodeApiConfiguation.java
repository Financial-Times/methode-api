package com.ft.methodeapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class MethodeApiConfiguation extends Configuration {

	private final MethodeConnectionConfiguration methodeConnectionConfiguration;

	@JsonCreator
	public MethodeApiConfiguation(@JsonProperty("methodeConnection") MethodeConnectionConfiguration methodeConnectionConfiguration) {
		this.methodeConnectionConfiguration = methodeConnectionConfiguration;
	}

	public MethodeConnectionConfiguration getMethodeConnectionConfiguration() {
		return methodeConnectionConfiguration;
	}
}
