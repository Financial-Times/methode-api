package com.ft.methodeapi.service.http;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Joiner;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.omg.CORBA.SystemException;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("asset-type")
@Api(value = "/asset-type", description = "Resource for requesting asset types.")
public class GetAssetTypeResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GetAssetTypeResource.class);
	
	private final MethodeFileRepository methodeFileRepository;
	
	
	public GetAssetTypeResource(MethodeFileRepository methodeFileRepository) {
		this.methodeFileRepository = methodeFileRepository;
    }

	@ApiOperation(
	            value = "Returns asset types.",
	            notes = "Returns asset types by given UUIDs.",
	            response = EomAssetType.class,
	            responseContainer = "Map",
	            produces = MediaType.APPLICATION_JSON,
				consumes = MediaType.APPLICATION_JSON)
	@POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Map<String, EomAssetType> getByUuid(final Set<String> assetIdentifiers) {
        try {
        	if (assetIdentifiers != null) {
            	LOGGER.debug("Asset identifiers: {}", Joiner.on(",").join(assetIdentifiers));
                Map<String, EomAssetType> assetTypes = methodeFileRepository.getAssetTypes(assetIdentifiers);
                LOGGER.info("message=\"Identified asset types successfully.\" uuids=\"{}\".", assetTypes.keySet());
                return assetTypes;
        	} else {
        		throw ClientError.status(400).error("No asset identifiers supplied").exception();
        	}
        } catch(MethodeException | SystemException ex) {
            throw ServerError.status(503).error("error accessing upstream system").exception(ex);
        }
    }
	
	

}
