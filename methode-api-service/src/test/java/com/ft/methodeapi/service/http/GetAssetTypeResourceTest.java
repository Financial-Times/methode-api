package com.ft.methodeapi.service.http;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.yammer.dropwizard.testing.ResourceTest;


public class GetAssetTypeResourceTest extends ResourceTest {
	
	private static final String VALID_UUID1 = "54307a12-37fa-11e3-8f44-002128161462";
	private static final String VALID_UUID2 = "64307a12-37fa-11e3-8f44-002128161462";
	private static final String MISSING_UUID = "74307a12-37fa-11e3-8f44-002128161462";
	private MethodeFileRepository methodeFileRepository;

	@Override
	protected void setUpResources() throws Exception {
		methodeFileRepository = mock(MethodeFileRepository.class);
        addResource(new GetAssetTypeResource(methodeFileRepository));
	}
	
	@Test
	public void shouldReturn200AndReturnTypeInformationForSuccessPath () {
		Set<String> assetIdentifiers = Sets.newHashSet(VALID_UUID1, VALID_UUID2);
		EomAssetType eomAssetType1 = new EomAssetType.Builder().uuid(VALID_UUID1).type("EOM:CompoundStory").build();
		EomAssetType eomAssetType2 = new EomAssetType.Builder().uuid(VALID_UUID2).type("EOM:CompoundStory").build();
		Map<String, EomAssetType> methodeFileRepositoryResult = ImmutableMap.of(VALID_UUID1, eomAssetType1, VALID_UUID2, eomAssetType2);
		when(methodeFileRepository.getAssetTypes(assetIdentifiers)).thenReturn(methodeFileRepositoryResult );
		
		final ClientResponse clientResponse = client().resource("/asset-type").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, assetIdentifiers);
		
		assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
		Map<String, EomAssetType> result = clientResponse.getEntity(new GenericType<Map<String, EomAssetType>>() { });
		assertThat("result1", result.get(VALID_UUID1), equalTo(eomAssetType1));
		assertThat("result2", result.get(VALID_UUID2), equalTo(eomAssetType2));
	}
	
	@Test
	public void shouldReturn200EvenIfSomeLookupsFail () {
		Set<String> assetIdentifiers = Sets.newHashSet(VALID_UUID1, MISSING_UUID);
		EomAssetType eomAssetType1 = new EomAssetType.Builder().uuid(VALID_UUID1).type("EOM:CompoundStory").build();
		EomAssetType eomAssetType2 = new EomAssetType.Builder().uuid(MISSING_UUID).error("Invalid URI").build();
		Map<String, EomAssetType> methodeFileRepositoryResult = ImmutableMap.of(VALID_UUID1, eomAssetType1, MISSING_UUID, eomAssetType2);
		when(methodeFileRepository.getAssetTypes(assetIdentifiers)).thenReturn(methodeFileRepositoryResult );
		
		final ClientResponse clientResponse = client().resource("/asset-type").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, assetIdentifiers);
		
		assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
		Map<String, EomAssetType> result = clientResponse.getEntity(new GenericType<Map<String, EomAssetType>>() { });
		assertThat("result1", result.get(VALID_UUID1), equalTo(eomAssetType1));
		assertThat("result2", result.get(MISSING_UUID), equalTo(eomAssetType2));
	}
	
	@Test
	public void shouldReturn400ForNoBodySupplied () {
		Set<String> assetIdentifiers = null;
		final ClientResponse clientResponse = client().resource("/asset-type/").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, assetIdentifiers);
		
		assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
	}
}
