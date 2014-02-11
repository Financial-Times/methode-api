package com.ft.methodeapi.smoke;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.methodeapi.acceptance.ApiConfig;

import java.util.Collections;
import java.util.List;

public class MethodeApiSmokeTestConfiguration {

    private final List<ApiConfig> methodeApiConfigs;


	public MethodeApiSmokeTestConfiguration(@JsonProperty("methodeApiConfigs") List<ApiConfig> methodeApiConfigs) {
        this.methodeApiConfigs = methodeApiConfigs;
	}

	public List<ApiConfig> getMethodeApiConfigs() {
		return Collections.unmodifiableList(methodeApiConfigs);
	}

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }
}
