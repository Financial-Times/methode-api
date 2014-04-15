package com.ft.methodeapi.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.api.jaxrs.client.exceptions.RemoteApiException;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.jerseyhttpwrapper.ResilientClientBuilder;
import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.model.EomFile;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.yammer.dropwizard.config.Environment;

public class MethodeApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeApiClient.class);

    private final Client jerseyClient;
    private final String apiHost;
    private final int apiPort;
    
    private final int numberOfAssetIdsPerAssetTypeRequest;
    private final int numberOfParallelAssetTypeRequests;

    public MethodeApiClient(Environment environment, MethodeApiEndpointConfiguration methodeApiConfiguration) {
        this(
				ResilientClientBuilder.in(environment).using(methodeApiConfiguration.getEndpointConfiguration()).build(),
				methodeApiConfiguration
            );
    }

    public MethodeApiClient(Client client, MethodeApiEndpointConfiguration methodeApiConfiguration) {
        this.jerseyClient = client;
        this.apiHost = methodeApiConfiguration.getEndpointConfiguration().getHost();
        this.apiPort = methodeApiConfiguration.getEndpointConfiguration().getPort();
        AssetTypeRequestConfiguration assetTypeRequestConfiguration = methodeApiConfiguration.getAssetTypeRequestConfiguration();
        if (assetTypeRequestConfiguration != null) {
            this.numberOfAssetIdsPerAssetTypeRequest = assetTypeRequestConfiguration.getNumberOfAssetIdsPerAssetTypeRequest();
            this.numberOfParallelAssetTypeRequests = assetTypeRequestConfiguration.getNumberOfParallelAssetTypeRequests();
        } else { // choose sensible defaults
        	this.numberOfAssetIdsPerAssetTypeRequest = 2;
        	this.numberOfParallelAssetTypeRequests = 4;
        }
    }

    /**
     * It looks like build(...) isn't safe for concurrent use
     * so this method can be used to create fresh instances for
     * use in a single thread.
     */
    private UriBuilder fileUrlBuilder() {

        return UriBuilder.fromPath("eom-file")
                .path("{uuid}")
                .scheme("http")
                .host(apiHost)
                .port(apiPort);
    }

    public EomFile findFileByUuid(String uuid, String transactionId) {
        final URI fileByUuidUri = fileUrlBuilder().build(uuid);
        LOGGER.debug("making GET request to methode api {}", fileByUuidUri);

        ClientResponse clientResponse;

        try {
            clientResponse = jerseyClient
                    .resource(fileByUuidUri)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
					.header(TransactionIdUtils.TRANSACTION_ID_HEADER, transactionId)
                    .get(ClientResponse.class);
        } catch (ClientHandlerException che) {
            Throwable cause = che.getCause();
            if(cause instanceof IOException) {
                throw new ApiNetworkingException(fileByUuidUri,"GET",che);
            }
            throw che;
        }

        int responseStatusCode = clientResponse.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {
            // SUCCESS!
            return clientResponse.getEntity(new GenericType<EomFile>(){});
        }

        LOGGER.error("received a {} status code when making a GET request to {}", responseStatusCode, fileByUuidUri);
        ErrorEntity entity = null;
        try {
            entity = clientResponse.getEntity(ErrorEntity.class);
        } catch (Throwable t) {
            LOGGER.warn("Failed to parse ErrorEntity when handling API transaction failure",t);
        }
        throw new RemoteApiException(fileByUuidUri,"GET",responseStatusCode,entity);

    }
    
    public Map<String, EomAssetType> findAssetTypes(Set<String> assetIdentifiers, String transactionId) {
        final URI assetTypeUri = UriBuilder.fromPath("asset-type")
        		.scheme("http")
        		.host(apiHost)
        		.port(apiPort)
        		.build();
        
        Map<String, EomAssetType> results = Maps.newHashMap();
    
        List<List<String>> partitionedAssetIdentifiers = Lists.partition(Lists.newArrayList(assetIdentifiers), numberOfAssetIdsPerAssetTypeRequest);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfParallelAssetTypeRequests);
        List<Future<ClientResponse>> futures = Lists.newArrayList();
		for (List<String> slice: partitionedAssetIdentifiers) {
			futures.add((Future<ClientResponse>) executorService.submit(new MakeAssetTypeRequestTask(slice, transactionId, assetTypeUri)));
		}
		
		// All tasks have been submitted, we can begin the shutdown of our executor
        System.out.println("All requests queued, starting shutdown...");
        executorService.shutdown();
		
		// Make sure all the requests have been processed 
		// Every ten seconds we print our progress
		int numberSeconds = 0;
        while (!executorService.isTerminated()) {
        	try {
        		executorService.awaitTermination(10, TimeUnit.MILLISECONDS);
        		numberSeconds++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	System.out.println("Been processing for " + numberSeconds + "0 seconds");
        }
        
        System.out.println("ExecutorService has terminated");
        
        for (Future<ClientResponse> future: futures) {
			try {
				Map<String, EomAssetType> resultsFromFuture = processResponse(future.get(), assetTypeUri);
				results.putAll(resultsFromFuture);			
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
		        Throwable cause = e.getCause();
		        if(cause instanceof ClientHandlerException) {
		        	 if (cause.getCause() instanceof IOException) {
		        		 throw new ApiNetworkingException(assetTypeUri,"POST", cause);
		        	 }
		        	 throw (ClientHandlerException) cause;
		        }
			}
		}
        
        return results;

    }

	
	private Map<String, EomAssetType> processResponse(ClientResponse clientResponse, URI assetTypeUri) {
        int responseStatusCode = clientResponse.getStatus();
        int responseStatusFamily = responseStatusCode / 100;

        if (responseStatusFamily == 2) {
            return clientResponse.getEntity(new GenericType<Map<String, EomAssetType>>(){});
        }

        LOGGER.error("received a {} status code when making a POST request to {}", responseStatusCode, assetTypeUri);
        ErrorEntity entity = null;
        try {
            entity = clientResponse.getEntity(ErrorEntity.class);
        } catch (Throwable t) {
            LOGGER.warn("Failed to parse ErrorEntity when handling API transaction failure",t);
        }
        throw new RemoteApiException(assetTypeUri,"GET",responseStatusCode,entity);

	}


	private class MakeAssetTypeRequestTask implements Callable<ClientResponse> {

		private List<String> assetIdentifiers;
		private String transactionId;
		private URI assetTypeUri;

		public MakeAssetTypeRequestTask(List<String> assetIdentifiers, String transactionId,
			final URI assetTypeUri) {
			this.assetIdentifiers = assetIdentifiers;
			this.transactionId = transactionId;
			this.assetTypeUri = assetTypeUri;
		}
		
		@Override
		public ClientResponse call() throws Exception {
			LOGGER.debug("making POST request to methode api {}", assetTypeUri);

	        return jerseyClient
	                    .resource(assetTypeUri)
	                    .accept(MediaType.APPLICATION_JSON_TYPE)
	                    .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
						.header(TransactionIdUtils.TRANSACTION_ID_HEADER, transactionId)
	                    .post(ClientResponse.class, assetIdentifiers);
		}
	
	}

}
