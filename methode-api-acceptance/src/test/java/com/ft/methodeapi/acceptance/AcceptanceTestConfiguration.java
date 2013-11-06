package com.ft.methodeapi.acceptance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptanceTestConfiguration {

    private final String readServiceUrl;
    private final String readHealthcheckUrl;

    private final String writeServiceUrl;
    private final String writeHealthcheckUrl;

    private final String methodeApiServiceUrl;
    private final String methodeApiHealthcheckUrl;

    private final String methodeBridgeServiceUrl;
    private final String methodeBridgeHealthcheckUrl;

	public AcceptanceTestConfiguration(@JsonProperty("readApi") ApiConfig readApiConfig,
                                       @JsonProperty("writeApi") ApiConfig writeApiConfig,
                                       @JsonProperty("methodeApi") ApiConfig methodeApiConfig,
                                       @JsonProperty("methodeBridge") ApiConfig methodeBridgeConfig) {

        readServiceUrl = String.format("http://%s:%s/content/items/", readApiConfig.getHost(), readApiConfig.getPort());
        readHealthcheckUrl = String.format("http://%s:%s/healthcheck", readApiConfig.getHost(), readApiConfig.getAdminPort());

        writeServiceUrl = String.format("http://%s:%s/", writeApiConfig.getHost(), writeApiConfig.getPort());
        writeHealthcheckUrl = String.format("http://%s:%s/healthcheck", writeApiConfig.getHost(), writeApiConfig.getAdminPort());

        methodeApiServiceUrl = String.format("http://%s:%s/eom-file/", methodeApiConfig.getHost(), methodeApiConfig.getPort());
        methodeApiHealthcheckUrl = String.format("http://%s:%s/healthcheck", methodeApiConfig.getHost(), methodeApiConfig.getAdminPort());

        methodeBridgeServiceUrl = String.format("http://%s:%s/publish/", methodeBridgeConfig.getHost(), methodeBridgeConfig.getPort());
        methodeBridgeHealthcheckUrl = String.format("http://%s:%s/healthcheck", methodeBridgeConfig.getHost(), methodeBridgeConfig.getAdminPort());
	}

	public String getWriteHealthcheckUrl() {
		return writeHealthcheckUrl;
	}

	public String getMethodeApiHealthcheckUrl() {
		return methodeApiHealthcheckUrl;
	}

	public String getMethodeBridgeHealthcheckUrl() {
		return methodeBridgeHealthcheckUrl;
	}

	public String getReadHealthcheckUrl() {
		return readHealthcheckUrl;
	}

	public String getWriteServiceUrl() {
		return writeServiceUrl;
	}

	public String getReadServiceUrl() {
		return readServiceUrl;
	}

	public String getMethodeApiServiceUrl() {
		return methodeApiServiceUrl;
	}

	public String getMethodeBridgeServiceUrl() {
		return methodeBridgeServiceUrl;
	}
}
