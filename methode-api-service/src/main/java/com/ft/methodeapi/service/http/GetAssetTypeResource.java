package com.ft.methodeapi.service.http;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.omg.CORBA.SystemException;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.service.methode.MethodeException;
import com.ft.methodeapi.service.methode.MethodeFileRepository;
import com.ft.methodeapi.service.methode.NotFoundException;
import com.yammer.metrics.annotation.Timed;

@Path("asset-type")
public class GetAssetTypeResource {
	
	
	
	private final MethodeFileRepository methodeFileRepository;
	
	
	public GetAssetTypeResource(MethodeFileRepository methodeFileRepository) {
		this.methodeFileRepository = methodeFileRepository;
    }
	
	@POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Map<String, EomAssetType> getByUuid(final Set<UUID> assetIdentifiers) {
        try {
            return methodeFileRepository.getAssetTypes(assetIdentifiers);
        } catch(NotFoundException e){
        	throw ClientError.status(404).error("Not Found").exception(e);
        }catch(MethodeException | SystemException ex) {
            throw ServerError.status(503).error("error accessing upstream system").exception(ex);
        }
    }
	
	

}
