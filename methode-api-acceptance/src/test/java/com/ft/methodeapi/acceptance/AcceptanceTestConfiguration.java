package com.ft.methodeapi.acceptance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptanceTestConfiguration {


    private final String methodeApiServiceUrl;
    private final String methodeApiHealthcheckUrl;


	public AcceptanceTestConfiguration(@JsonProperty("methodeApi") ApiConfig methodeApiConfig) {
        methodeApiServiceUrl = String.format("http://%s:%s/eom-file/", methodeApiConfig.getHost(), methodeApiConfig.getPort());
        methodeApiHealthcheckUrl = String.format("http://%s:%s/healthcheck", methodeApiConfig.getHost(), methodeApiConfig.getAdminPort());
	}

	public String getMethodeApiHealthcheckUrl() {
		return methodeApiHealthcheckUrl;
	}

	public String getMethodeApiServiceUrl() {
		return methodeApiServiceUrl;
	}

}
