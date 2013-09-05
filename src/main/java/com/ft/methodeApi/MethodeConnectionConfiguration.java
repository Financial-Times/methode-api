package com.ft.methodeApi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class MethodeConnectionConfiguration {

	private final String methodeHostName;
	private final int methodePort;
	private final String methodeUserName;
	private final String methodePassword;

	@JsonCreator
	public MethodeConnectionConfiguration(@JsonProperty("hostName") @NotEmpty String methodeHostName,
										  @JsonProperty("nsPort") @Min(1) @Max(65535) int methodePort,
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

	public int getMethodePort() {
		return methodePort;
	}

	public String getMethodeUserName() {
		return methodeUserName;
	}

	public String getMethodePassword() {
		return methodePassword;
	}
}
