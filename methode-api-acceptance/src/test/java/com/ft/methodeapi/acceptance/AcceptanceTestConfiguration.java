package com.ft.methodeapi.acceptance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptanceTestConfiguration {


    private final String methodeApiServiceUrl;
    private final String methodeApiHealthcheckUrl;
    private final ApiConfig methodeApiConfig;


    public AcceptanceTestConfiguration(@JsonProperty("methodeApi") ApiConfig methodeApiConfig) {
        this.methodeApiConfig = methodeApiConfig;
        methodeApiServiceUrl = String.format("http://%s:%s/eom-file/", methodeApiConfig.getHost(), methodeApiConfig.getPort());
        methodeApiHealthcheckUrl = String.format("http://%s:%s/healthcheck", methodeApiConfig.getHost(), methodeApiConfig.getAdminPort());
	}

	public String getMethodeApiHealthcheckUrl() {
		return methodeApiHealthcheckUrl;
	}

	public String getMethodeApiServiceUrl() {
		return methodeApiServiceUrl;
	}

    public ApiConfig getMethodeApiConfig() {
        return methodeApiConfig;
    }
}
