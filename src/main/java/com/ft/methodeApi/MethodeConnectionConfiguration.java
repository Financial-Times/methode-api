package com.ft.methodeApi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class MethodeConnectionConfiguration {

	private final String methodeHostName;
	private final String methodePort;
	private final String methodeUserName;
	private final String methodePassword;

	@JsonCreator
	public MethodeConnectionConfiguration(@JsonProperty("hostName") @NotEmpty String methodeHostName,
										  @JsonProperty("nsPort") @NotEmpty String methodePort,
										  @JsonProperty("userName") @NotEmpty String methodeUserName,
										  @JsonProperty("password") @NotEmpty String methodePassword) {
		this.methodeHostName = methodeHostName;
		this.methodePort = methodePort;
		this.methodeUserName = methodeUserName;
		this.methodePassword = methodePassword;
	}

	public String getMethodeHostName() {
		return methodeHostName;
	}

	public String getMethodePort() {
		return methodePort;
	}

	public String getMethodeUserName() {
		return methodeUserName;
	}

	public String getMethodePassword() {
		return methodePassword;
	}
}
