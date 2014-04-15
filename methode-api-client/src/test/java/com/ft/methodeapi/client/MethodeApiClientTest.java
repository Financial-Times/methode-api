package com.ft.methodeapi.client;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.conn.ConnectTimeoutException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.util.collections.Sets;

import com.ft.api.jaxrs.client.exceptions.ApiNetworkingException;
import com.ft.api.jaxrs.client.exceptions.RemoteApiException;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.service.http.EomFileResource;
import com.ft.methodeapi.service.http.GetAssetTypeResource;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.yammer.dropwizard.testing.ResourceTest;

public class MethodeApiClientTest extends ResourceTest {

	private static final String TRANSACTION_ID = "tid_test";


    private MethodeFileRepository methodeFileRepository;

	private final String SYSTEM_ATTRIBUTES = "<props><productInfo><name>FTcom</name>\n" +
			"<issueDate>20131219</issueDate>\n" +
			"</productInfo>\n" +
			"<workFolder>/FT/Companies</workFolder>\n" +
			"<templateName>/SysConfig/Templates/FT/Base-Story.xml</templateName>\n" +
			"<summary>t text text text text text text text text text text text text text text text text\n" +
			" text text text text te...</summary><wordCount>417</wordCount></props>";

    @Override
    protected void setUpResources() throws Exception {
        methodeFileRepository = mock(MethodeFileRepository.class);
        addResource(new EomFileResource(methodeFileRepository));
        addResource(new GetAssetTypeResource(methodeFileRepository));
    }



    @Test
    public void canRetrieveEomFile() {

        final byte[] fileBytes = "blah, blah, blah".getBytes();
        when(methodeFileRepository.findFileByUuid(any(String.class))).thenReturn(Optional.of(new EomFile("asdf", "someType", fileBytes, "some attributes", "WebRevise", SYSTEM_ATTRIBUTES)));

        EomFile eomFile = getMethodeApiClientForMockJerseyClient(client()).findFileByUuid("asdsfgdg", TRANSACTION_ID);

        assertArrayEquals(fileBytes, eomFile.getValue());
    }

    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForSocketTimeout() {
        Client mockClient = primeClientToExperienceExceptionWithSpecificRootCause(new SocketTimeoutException());
        exerciseClientForGetEomFile(mockClient);
    }

    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForAnyOtherIssueWithTheTcpSocket() {

        Client mockClient = primeClientToExperienceExceptionWithSpecificRootCause(new SocketException());
        exerciseClientForGetEomFile(mockClient);
    }

    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForConnectionTimeout() {

        Client mockClient = primeClientToExperienceExceptionWithSpecificRootCause(new ConnectTimeoutException());
        exerciseClientForGetEomFile(mockClient);
    }

    @Test(expected = RemoteApiException.class) 
    public void shouldThrowRemoteApiExceptionWhenRequestForEomFileFails() {
        
        ClientHandler handler = mock(ClientHandler.class);
    	Client mockClient = new Client(handler);
        
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(503);
        when(clientResponse.getEntity(ErrorEntity.class)).thenReturn(new ErrorEntity("Got error"));
       
        when(handler.handle(any(ClientRequest.class))).thenReturn(clientResponse);
   
        getMethodeApiClientForMockJerseyClient(mockClient).findFileByUuid("asdsfgdg", TRANSACTION_ID);
    }
    
    @Test
    public void canGetAssetTypesInSingleRequest() { 
    	Set<String> assetIds = Sets.newSet("test1");
    	
    	int numberOfAssetIdsPerRequest = 3;
    	int numberOfThreads = 4;
    	
    	Map<String, EomAssetType> expectedOutput = primeMethodeFileRepositoryAndGetExpectedOutputForGetAssetTypes(assetIds, numberOfAssetIdsPerRequest);
   	
		Map<String, EomAssetType> assetTypes = getMethodeApiClientForMockJerseyClient(client(), numberOfAssetIdsPerRequest, numberOfThreads).findAssetTypes(assetIds, TRANSACTION_ID);

		assertThat(assetTypes.entrySet(), everyItem(isIn(expectedOutput.entrySet())));
		assertThat(expectedOutput.entrySet(), everyItem(isIn(assetTypes.entrySet())));
    }
    
    @Test
    public void canGetAssetTypesSplitBetweenFewerRequestsThanNumberOfThreads() { 
    	Set<String> assetIds = Sets.newSet("test1", "test2", "test3", "test4", "test5");
    	
    	int numberOfAssetIdsPerRequest = 3;
    	int numberOfThreads = 4;
    	
    	Map<String, EomAssetType> expectedOutput = primeMethodeFileRepositoryAndGetExpectedOutputForGetAssetTypes(assetIds, numberOfAssetIdsPerRequest);
   	
		Map<String, EomAssetType> assetTypes = getMethodeApiClientForMockJerseyClient(client(), numberOfAssetIdsPerRequest, numberOfThreads).findAssetTypes(assetIds, TRANSACTION_ID);

		assertThat(assetTypes.entrySet(), everyItem(isIn(expectedOutput.entrySet())));
		assertThat(expectedOutput.entrySet(), everyItem(isIn(assetTypes.entrySet())));
    }

    @Test
    public void canGetAssetTypesSplitBetweenMoreRequestsThanNumberOfThreads() { 
    	Set<String> assetIds = Sets.newSet("test1", "test2", "test3", "test4", "test5");
    	
    	int numberOfAssetIdsPerRequest = 1;
    	int numberOfThreads = 2;
    	
    	Map<String, EomAssetType> expectedOutput = primeMethodeFileRepositoryAndGetExpectedOutputForGetAssetTypes(assetIds, numberOfAssetIdsPerRequest);
   	
		Map<String, EomAssetType> assetTypes = getMethodeApiClientForMockJerseyClient(client(), numberOfAssetIdsPerRequest, numberOfThreads).findAssetTypes(assetIds, TRANSACTION_ID);

		assertThat(assetTypes.entrySet(), everyItem(isIn(expectedOutput.entrySet())));
		assertThat(expectedOutput.entrySet(), everyItem(isIn(assetTypes.entrySet())));
    }
    
    //one of the requests fails with socket timeout, get ApiNetworkingException 
    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowApiNetworkingExceptionForGetAssetTypesWhenOneRequestFailsWithSocketTimeout() {
    	
    	Client mockClient = primeClientSoOneRequestInThreeResultsInExceptionWithSpecificRootCauseForGetAssetTypes(new SocketTimeoutException("socket timeout"));
        getMethodeApiClientForMockJerseyClient(mockClient, 1, 3).findAssetTypes(Sets.newSet("test1", "test2", "test3"), TRANSACTION_ID);
    }
    
    //one of the requests fails with connect timeout, get ApiNetworkingException
    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForGetAssetTypesWhenOneRequestFailsWithConnectTimeout() {
        Client mockClient = primeClientSoOneRequestInThreeResultsInExceptionWithSpecificRootCauseForGetAssetTypes(new ConnectTimeoutException("connect timeout"));
        getMethodeApiClientForMockJerseyClient(mockClient, 1, 3).findAssetTypes(Sets.newSet("test1", "test2", "test3"), TRANSACTION_ID);
    }
    
    //two requests fail with different exceptions, get ApiNetworkingException for one of the failures
    @Test(expected = ApiNetworkingException.class)
    public void shouldThrowDistinctExceptionForGetAssetTypesWhenTwoRequestsFail() {
        Client mockClient = primeClientSoTwoRequestsInThreeResultInExceptionWithSpecificRootCausesForGetAssetTypes(new ConnectTimeoutException("connect timeout"), new SocketTimeoutException("socket timeout"));
        getMethodeApiClientForMockJerseyClient(mockClient, 1, 3).findAssetTypes(Sets.newSet("test1", "test2", "test3"), TRANSACTION_ID);
    }
    


    @Test(expected = RemoteApiException.class) 
    public void shouldThrowRemoteApiExceptionWhenRequestForGetAssetTypesFails() {
        
        ClientHandler handler = mock(ClientHandler.class);
    	Client mockClient = new Client(handler);
        
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(503);
        when(clientResponse.getEntity(ErrorEntity.class)).thenReturn(new ErrorEntity("Got error"));
       
        when(handler.handle(any(ClientRequest.class))).thenReturn(clientResponse);
   
        getMethodeApiClientForMockJerseyClient(mockClient).findAssetTypes(Sets.newSet("test1", "test2", "test3"), TRANSACTION_ID);
    }

    private void exerciseClientForGetEomFile(Client mockClient) {
        getMethodeApiClientForMockJerseyClient(mockClient).findFileByUuid("035a2fa0-d988-11e2-bce1-002128161462", TRANSACTION_ID);
    }

    private MethodeApiClient getMethodeApiClientForMockJerseyClient(Client mockClient) {
        return getMethodeApiClientForMockJerseyClient(mockClient, 3, 4);
    }
    
    private MethodeApiClient getMethodeApiClientForMockJerseyClient(Client mockClient, int numberOfAssetIdsPerRequest, 
    		int numberOfParallelAssetTypeRequests) {

        MethodeApiEndpointConfiguration config = MethodeApiEndpointConfiguration.forTesting("localhost", 1234,
                new AssetTypeRequestConfiguration(numberOfAssetIdsPerRequest, numberOfParallelAssetTypeRequests));

        return MethodeApiClient.forTesting(mockClient,config);

    }

    private Client primeClientToExperienceExceptionWithSpecificRootCause(Exception rootCause) {
        ClientHandler handler = mock(ClientHandler.class);
        Client mockClient = new Client(handler);
        when(handler.handle(any(ClientRequest.class))).thenThrow( new ClientHandlerException(rootCause));
        return mockClient;
    }
    
    private Client primeClientSoOneRequestInThreeResultsInExceptionWithSpecificRootCauseForGetAssetTypes(Exception rootCause) {
        ClientHandler handler = mock(ClientHandler.class);
        Client mockClient = new Client(handler);

        ClientResponse clientResponse = mockOkResponseWithEmptyMap();
        
        when(handler.handle(any(ClientRequest.class))).thenReturn(clientResponse).thenReturn(clientResponse).thenThrow( new ClientHandlerException(rootCause));
        return mockClient;
    }

    private Client primeClientSoTwoRequestsInThreeResultInExceptionWithSpecificRootCausesForGetAssetTypes(Exception rootCause1, Exception rootCause2) {
        ClientHandler handler = mock(ClientHandler.class);
        Client mockClient = new Client(handler);

        ClientResponse clientResponse = mockOkResponseWithEmptyMap();

        when(handler.handle(any(ClientRequest.class))).thenThrow( new ClientHandlerException(rootCause1)).thenReturn(clientResponse).thenThrow( new ClientHandlerException(rootCause2));
        return mockClient;
    }

    private ClientResponse mockOkResponseWithEmptyMap() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(Matchers.<GenericType<Map<String, EomAssetType>>>any())).thenReturn(new HashMap<String, EomAssetType>());
        return clientResponse;
    }

    private Map<String, EomAssetType> primeMethodeFileRepositoryAndGetExpectedOutputForGetAssetTypes(Set<String> assetIds,
    		int numberOfPartitions) {
    	List<List<String>> partitionedAssetIdentifiers = Lists.partition(Lists.newArrayList(assetIds), numberOfPartitions);
    	
    	Map<String, EomAssetType> expectedOutput = Maps.newHashMap();
    	
    	for (List<String> slice: partitionedAssetIdentifiers) {
    		Map<String, EomAssetType> expectedOutputForSlice = getExpectedAssetTypesForSlice(slice);
    		expectedOutput.putAll(expectedOutputForSlice);
        	when(methodeFileRepository.getAssetTypes(new HashSet<String>(slice))).thenReturn(expectedOutputForSlice);
    	}
    	
    	return expectedOutput;
    }

	private Map<String, EomAssetType> getExpectedAssetTypesForSlice(List<String> slice) {
		Map<String, EomAssetType> expectedAssetTypesForSlice = Maps.newHashMap();
		for(String uuid: slice) {
			expectedAssetTypesForSlice.put(uuid, getEomAssetTypeForUuid(uuid));
		}
		return expectedAssetTypesForSlice;
	}

	private EomAssetType getEomAssetTypeForUuid(String uuid) {
		return new EomAssetType.Builder().uuid(uuid).type("EOM:CompoundStory").build();
	}
}
