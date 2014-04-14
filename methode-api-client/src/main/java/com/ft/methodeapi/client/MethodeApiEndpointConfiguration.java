package com.ft.methodeapi.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.yammer.dropwizard.client.JerseyClientConfiguration;

public class MethodeApiEndpointConfiguration extends EndpointConfiguration {

	private final int numberOfAssetIdsPerAssetTypeRequest;
	private final int numberOfParallelAssetTypeRequests;
	
	/**
     * Creates a simple endpoint configuration for a test host (e.g. WireMock)
     * with GZip disabled.
     * @param host the test server
     * @param port the test port
     * @return a simple configuration
     */
    public static MethodeApiEndpointConfiguration forTesting(String host, int port, int numberOfAssetIdsPerAssetTypeRequest, 
    		int numberOfParallelAssetTypeRequests) {

        JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();
        clientConfig.setGzipEnabled(false);
        clientConfig.setGzipEnabledForRequests(false);

        return new MethodeApiEndpointConfiguration(
                Optional.of(String.format("test-%s-%s",host,port)),
                Optional.of(clientConfig),
                Optional.<String>absent(),
				Arrays.asList(String.format("%s:%d:%d", host, port, port + 1)),
				Collections.<String>emptyList(),
				numberOfAssetIdsPerAssetTypeRequest,
				numberOfParallelAssetTypeRequests
            );
    }


	public MethodeApiEndpointConfiguration(@JsonProperty("shortName") Optional<String> shortName,
            		@JsonProperty("jerseyClient") Optional<JerseyClientConfiguration> jerseyClientConfiguration,
            		@JsonProperty("path") Optional<String> path,
            		@JsonProperty("primaryNodes") List<String> primaryNodesRaw,
            		@JsonProperty("secondaryNodes") List<String> secondaryNodesRaw,
            		@JsonProperty("numberOfAssetIdsPerAssetTypeRequest") int numberOfAssetIdsPerAssetTypeRequest,
            		@JsonProperty("numberOfParallelAssetTypeRequests") int numberOfParallelAssetTypeRequests) {
		super(shortName, jerseyClientConfiguration, path, primaryNodesRaw, secondaryNodesRaw);
		this.numberOfAssetIdsPerAssetTypeRequest = numberOfAssetIdsPerAssetTypeRequest;
		this.numberOfParallelAssetTypeRequests = numberOfParallelAssetTypeRequests;
		
	}

	public int getNumberOfAssetIdsPerAssetTypeRequest() {
		return numberOfAssetIdsPerAssetTypeRequest;
	}

	public int getNumberOfParallelAssetTypeRequests() {
		return numberOfParallelAssetTypeRequests;
	}
	
	protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("numberOfAssetIdsPerAssetTypeRequest", numberOfAssetIdsPerAssetTypeRequest)
                .add("numberOfParallelAssetTypeRequests", numberOfParallelAssetTypeRequests);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

}
