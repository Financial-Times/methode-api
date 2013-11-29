package com.ft.methodeapi.service.methode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.omg.CORBA.BAD_PARAM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EOM.File;
import EOM.FileSystemAdmin;
import EOM.FileSystemObject;
import EOM.InvalidURI;
import EOM.PermissionDenied;
import EOM.RepositoryError;

import com.ft.methodeapi.model.EomAssetType;
import com.ft.methodeapi.service.methode.templates.MethodeFileSystemAdminOperationTemplate.FileSystemAdminCallback;
import com.google.common.base.Optional;

final class GetAssetTypeFileSystemAdminCallback implements FileSystemAdminCallback<Map<String, EomAssetType>>{

	private final Set<String> assetIdentifiers;
	private final Map<String, EomAssetType> assetTypes = new HashMap<>();
	
	private static final Logger logger = LoggerFactory.getLogger(MethodeFileRepository.class);

	GetAssetTypeFileSystemAdminCallback(Set<String> assetIdentifiers) {
		this.assetIdentifiers = assetIdentifiers;
	}

	@Override
	public Map<String, EomAssetType>  doOperation(final FileSystemAdmin fileSystemAdmin) {
	    for(String assetId : assetIdentifiers){
	    	EomAssetType eomAssetType;
	    	if(!assetTypes.containsKey(assetId)){
	    		final String uri = getAssetURI(assetId);

	            final EomAssetType.Builder assetTypeBuilder = new EomAssetType.Builder();
	            try{
	            	final FileSystemObject fso = fileSystemAdmin.get_object_with_uri(uri);
	                try {
	                    final File eomFile = EOM.FileHelper.narrow(fso);
	                    final String typeName = eomFile.get_type_name();
	                    Optional<String> sourceCode = new MethodeSourceCodeExtractor(eomFile.get_attributes()).extract();
	                    eomAssetType = assetTypeBuilder.uuid(eomFile.get_uuid_string()).type(typeName).sourceCode(sourceCode).build();
	                } catch (XMLStreamException e) {
	                	eomAssetType = assetTypeBuilder.uuid(assetId).error("Error when parsing attributes for asset").build();
						logger.debug("Error when parsing attributes for asset : {}", assetId);
					}finally{
						fso._release();
					}
                } catch (BAD_PARAM e) {
                    logger.error("Internal error", e);
                    eomAssetType = assetTypeBuilder.uuid(assetId).error("Internal error").build();
                } catch (InvalidURI e) {
	            	logger.debug("Uri: {} for asset with identifier: {} is invalid", uri, assetId);
	            	eomAssetType = assetTypeBuilder.uuid(assetId).error("Invalid URI").build();
	            } catch (PermissionDenied e) {
	            	logger.debug("Permission denied for asset with identifier: {} is invalid", assetId);
	            	eomAssetType = assetTypeBuilder.uuid(assetId).error("Permission Denied").build();
				} catch (RepositoryError e) {
					logger.debug("EOM Repository error when getting asset with identifier: {}", assetId);
					eomAssetType = assetTypeBuilder.uuid(assetId).error("EOM Repository error when getting asset ").build();
				}
	            assetTypes.put(assetId, eomAssetType);
	    	}
	    }
	    
	    
	    logger.debug("Successfully resolved type for {} assets :" + assetTypes.size());
	    return assetTypes;
	}
	
	private String getAssetURI(String assetId) {
		if(isUUID(assetId)){
			return "eom:/uuids/" + assetId;
		}else{
			return assetId;
		}
	}
    
    private boolean isUUID(String assetId) {
		try{
			UUID.fromString(assetId);
			return true;
		}catch(Exception e){
			return false;
		}
	}
}