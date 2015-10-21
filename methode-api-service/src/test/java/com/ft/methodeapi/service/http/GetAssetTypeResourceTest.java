package com.ft.methodeapi.service.http;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;


public class GetAssetTypeResourceTest {
	private static final String VALID_UUID1 = "54307a12-37fa-11e3-8f44-002128161462";
	private static final String VALID_UUID2 = "64307a12-37fa-11e3-8f44-002128161462";
	private static final String MISSING_UUID = "74307a12-37fa-11e3-8f44-002128161462";

    private MethodeFileRepository methodeFileRepository = mock(MethodeFileRepository.class);
    private GetAssetTypeResource resource = new GetAssetTypeResource(methodeFileRepository);
    
	@Test
	public void shouldReturn200AndReturnTypeInformationForSuccessPath() {
		Set<String> assetIdentifiers = Sets.newHashSet(VALID_UUID1, VALID_UUID2);

		EomAssetType eomAssetType1 = buildExampleCompoundStoryWith(VALID_UUID1);
		EomAssetType eomAssetType2 = buildExampleCompoundStoryWith(VALID_UUID2);

		Map<String, EomAssetType> methodeFileRepositoryResult = ImmutableMap.of(VALID_UUID1, eomAssetType1, VALID_UUID2, eomAssetType2);
		when(methodeFileRepository.getAssetTypes(assetIdentifiers)).thenReturn(methodeFileRepositoryResult );
		
		Map<String,EomAssetType> result = resource.getByUuid(assetIdentifiers);
		assertThat("result1", result.get(VALID_UUID1), equalTo(eomAssetType1));
		assertThat("result2", result.get(VALID_UUID2), equalTo(eomAssetType2));
	}


    @Test
	public void shouldReturn200EvenIfSomeLookupsFail () {
		Set<String> assetIdentifiers = Sets.newHashSet(VALID_UUID1, MISSING_UUID);

		EomAssetType eomAssetType1 = buildExampleCompoundStoryWith(VALID_UUID1);
		EomAssetType eomAssetType2 = new EomAssetType.Builder().uuid(MISSING_UUID).error("Invalid URI").build();

		Map<String, EomAssetType> methodeFileRepositoryResult = ImmutableMap.of(VALID_UUID1, eomAssetType1, MISSING_UUID, eomAssetType2);
		when(methodeFileRepository.getAssetTypes(assetIdentifiers)).thenReturn(methodeFileRepositoryResult );
		
        Map<String,EomAssetType> result = resource.getByUuid(assetIdentifiers);
		assertThat("result1", result.get(VALID_UUID1), equalTo(eomAssetType1));
		assertThat("result2", result.get(MISSING_UUID), equalTo(eomAssetType2));
	}
	
	@Test
	public void shouldReturn400ForNoBodySupplied () {
	    try {
	        resource.getByUuid(null);
	        fail("expected exception was not thrown");
	    }
	    catch (WebApplicationClientException e) {
	        assertThat("response status", e.getResponse().getStatus(), equalTo(400));
	    }
	}

    private EomAssetType buildExampleCompoundStoryWith(String uuid) {
        return new EomAssetType.Builder().uuid(uuid).type("EOM:CompoundStory").build();
    }

}
